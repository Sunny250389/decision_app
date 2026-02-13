import json
import asyncio
from typing import AsyncGenerator, List, Dict, Any
from openai import AsyncOpenAI

from app.core.config import settings
from app.core.prompts import SYSTEM_PROMPT


client = AsyncOpenAI(
    api_key=settings.PERPLEXITY_API_KEY,
    base_url=settings.PERPLEXITY_BASE_URL,
)

# -------------------------------------------------------
# REQUIRED DIMENSIONS
# -------------------------------------------------------

REQUIRED_DIMENSIONS = [
    "UPSIDE",
    "STABILITY",
    "FLEXIBILITY",
    "LEARNING_VALUE",
    "EFFORT",
    "EMOTIONAL_COST",
]


# -------------------------------------------------------
# NORMALIZATION HELPERS
# -------------------------------------------------------

def normalize_dimensions(dimensions: Dict[str, Any]) -> Dict[str, float]:
    normalized = {}

    for key in REQUIRED_DIMENSIONS:
        value = dimensions.get(key, 0.5)

        try:
            value = float(value)
        except Exception:
            value = 0.5

        normalized[key] = max(0.0, min(1.0, value))

    return normalized


def enforce_option_schema(obj: Dict[str, Any]) -> Dict[str, Any]:

    dimensions = obj.get("dimensions", {})
    if not isinstance(dimensions, dict):
        dimensions = {}

    obj["dimensions"] = normalize_dimensions(dimensions)

    obj.setdefault("id", "")
    obj.setdefault("description", "")
    obj.setdefault("pros", [])
    obj.setdefault("cons", [])
    obj.setdefault("risk", "medium")

    return obj


# -------------------------------------------------------
# MAIN STREAM FUNCTION
# -------------------------------------------------------

async def stream_decision_analysis(
        decision_text: str,
        constraints: List[str],
) -> AsyncGenerator[dict, None]:

    yield {
        "event": "status",
        "data": {"message": "Analyzing decision using structured intelligence model"},
    }

    await asyncio.sleep(0.1)

    # 🔥 STRONGER PROMPT ENFORCEMENT
    enhanced_prompt = f"""
You must respond ONLY in valid JSON objects.

Every option must include numeric dimension scores (0.0–1.0 floats).

Dimensions required:
UPSIDE, STABILITY, FLEXIBILITY, LEARNING_VALUE, EFFORT, EMOTIONAL_COST

Never omit dimensions.
Never output text outside JSON.
    """

    stream = await client.chat.completions.create(
        model=settings.PERPLEXITY_MODEL,
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT + "\n" + enhanced_prompt},
            {
                "role": "user",
                "content": f"Decision:\n{decision_text}\n\nConstraints:\n{constraints}",
            },
        ],
        temperature=0.2,
        stream=True,
    )

    buffer = ""

    async for chunk in stream:
        delta = chunk.choices[0].delta.content
        if not delta:
            continue

        buffer += delta

        # Only attempt parse when JSON seems complete
        if buffer.count("{") != buffer.count("}"):
            continue

        try:
            obj = json.loads(buffer)
            buffer = ""
        except json.JSONDecodeError:
            continue

        event_type = obj.get("type")

        # --------------------------------------------------
        # OPTION EVENT (UPGRADED)
        # --------------------------------------------------
        if event_type == "option":

            option = enforce_option_schema(obj)

            yield {
                "event": "option",
                "data": option,
            }

        # --------------------------------------------------
        # RECOMMENDATION
        # --------------------------------------------------
        elif event_type == "recommendation":

            yield {
                "event": "recommendation",
                "data": obj.get("data", ""),
            }

        # --------------------------------------------------
        # STATUS
        # --------------------------------------------------
        elif event_type == "status":

            yield {
                "event": "status",
                "data": {"message": obj.get("message", "")},
            }

        # --------------------------------------------------
        # FINAL AUDIT
        # --------------------------------------------------
        elif event_type == "decision_audit":

            yield {
                "event": "audit:recommendation",
                "data": obj.get("final_recommendation", ""),
            }

            yield {
                "event": "audit:confidence",
                "data": float(obj.get("confidence", 0.5)),
            }

            yield {
                "event": "audit:key_factors",
                "data": obj.get("key_factors", []),
            }

            yield {
                "event": "audit:assumptions",
                "data": obj.get("assumptions", []),
            }

            yield {
                "event": "audit:reversal_triggers",
                "data": obj.get("reversal_triggers", []),
            }

            yield {"event": "done", "data": {}}
            return

    yield {"event": "done", "data": {}}
