from fastapi import APIRouter
from fastapi.responses import StreamingResponse
import json

from app.api.models import DecisionRequest
from app.services.llm_service import stream_decision_analysis

router = APIRouter(prefix="/decision", tags=["decision"])


# -----------------------------------------------------
# DIMENSION NORMALIZATION HELPERS
# -----------------------------------------------------

REQUIRED_DIMENSIONS = [
    "UPSIDE",
    "STABILITY",
    "FLEXIBILITY",
    "LEARNING_VALUE",
    "EFFORT",
    "EMOTIONAL_COST",
]


def normalize_dimensions(dimensions: dict) -> dict:
    """
    Ensures:
    - All required keys exist
    - Values are floats
    - Values are clamped between 0.0 and 1.0
    """

    normalized = {}

    for key in REQUIRED_DIMENSIONS:
        value = dimensions.get(key, 0.5)

        try:
            value = float(value)
        except Exception:
            value = 0.5

        normalized[key] = max(0.0, min(1.0, value))

    return normalized


def ensure_option_dimensions(event: dict) -> dict:
    """
    Guarantees that every option event contains structured dimensions.
    """

    dimensions = event.get("dimensions")

    if not isinstance(dimensions, dict):
        dimensions = {}

    event["dimensions"] = normalize_dimensions(dimensions)

    return event


# -----------------------------------------------------
# ROUTE
# -----------------------------------------------------

@router.post("/evaluate")
async def evaluate_decision(request: DecisionRequest):

    async def event_generator():
        try:
            async for event in stream_decision_analysis(
                    decision_text=request.decision_text,
                    constraints=request.constraints,
            ):

                # --------------------------------------------------
                # OPTION EVENT STRUCTURE ENFORCEMENT
                # --------------------------------------------------

                if event.get("event") == "option":

                    option_data = ensure_option_dimensions(event.get("data", {}))

                    yield format_sse("option", option_data)
                    continue

                # --------------------------------------------------
                # AUDIT EVENT HANDLING
                # --------------------------------------------------

                if event.get("type") == "decision_audit":

                    yield format_sse("audit:key_factors", event["key_factors"])
                    yield format_sse("audit:assumptions", event["assumptions"])
                    yield format_sse("audit:reversal_triggers", event["reversal_triggers"])
                    yield format_sse("audit:recommendation", event["recommendation"])
                    yield format_sse("audit:confidence", event["confidence"])
                    yield format_sse("done", True)
                    return

                # --------------------------------------------------
                # PASS THROUGH OTHER EVENTS
                # --------------------------------------------------

                yield format_sse(
                    event.get("event", event.get("type", "message")),
                    event,
                )

        except Exception as e:
            yield format_sse("error", {"message": str(e)})
            yield format_sse("done", False)

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
        },
    )


# -----------------------------------------------------
# SSE FORMATTER
# -----------------------------------------------------

def format_sse(event_name: str, data) -> str:
    """
    Standard SSE formatter.
    All events (including audit) pass through here.
    """
    return f"event: {event_name}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"
