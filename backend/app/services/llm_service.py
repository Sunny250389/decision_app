import json
import asyncio
from typing import AsyncGenerator, List
from openai import AsyncOpenAI

from app.core.config import settings
from app.core.prompts import SYSTEM_PROMPT

client = AsyncOpenAI(
    api_key=settings.PERPLEXITY_API_KEY,
    base_url=settings.PERPLEXITY_BASE_URL,
)


async def stream_decision_analysis(
        decision_text: str,
        constraints: List[str],
) -> AsyncGenerator[dict, None]:

    # ───────────────── STATUS ─────────────────
    yield {
        "event": "status",
        "data": {"message": "Analyzing decision using personal intelligence model"},
    }

    await asyncio.sleep(0.1)

    stream = await client.chat.completions.create(
        model=settings.PERPLEXITY_MODEL,
        messages=[
            {"role": "system", "content": SYSTEM_PROMPT},
            {
                "role": "user",
                "content": f"Decision:\n{decision_text}\n\nConstraints:\n{constraints}",
            },
        ],
        temperature=0.3,
        stream=True,
    )

    buffer = ""

    async for chunk in stream:
        delta = chunk.choices[0].delta.content
        if not delta:
            continue

        buffer += delta

        # Wait until we have a full JSON object
        if buffer.count("{") != buffer.count("}"):
            continue

        try:
            obj = json.loads(buffer)
            buffer = ""
        except json.JSONDecodeError:
            continue

        event_type = obj.get("type")

        # ───────── OPTIONS ─────────
        if event_type == "option":
            yield {"event": "option", "data": obj}

        elif event_type == "recommendation":
            yield {"event": "recommendation", "data": obj}

        # ───────── STATUS ─────────
        elif event_type == "status":
            yield {"event": "status", "data": {"message": obj.get("message", "")}}

        # ───────── FINAL AUDIT (CRITICAL FIX) ─────────
        elif event_type == "decision_audit":
            audit = obj

            # 🔥 explode audit into Android-native events
            if "final_recommendation" in audit:
                yield {
                    "event": "audit:recommendation",
                    "data": audit["final_recommendation"],
                }

            if "confidence" in audit:
                yield {
                    "event": "audit:confidence",
                    "data": audit["confidence"],
                }

            if "key_factors" in audit:
                yield {
                    "event": "audit:key_factors",
                    "data": audit["key_factors"],
                }

            if "assumptions" in audit:
                yield {
                    "event": "audit:assumptions",
                    "data": audit["assumptions"],
                }

            if "reversal_triggers" in audit:
                yield {
                    "event": "audit:reversal_triggers",
                    "data": audit["reversal_triggers"],
                }

            # ───────── DONE ─────────
            yield {"event": "done", "data": {}}
            return

    # Safety net
    yield {"event": "done", "data": {}}