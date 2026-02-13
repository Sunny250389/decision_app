from fastapi import APIRouter
from fastapi.responses import StreamingResponse
import json

from app.api.models import DecisionRequest
from app.services.llm_service import stream_decision_analysis

router = APIRouter(prefix="/decision", tags=["decision"])


@router.post("/evaluate")
async def evaluate_decision(request: DecisionRequest):

    async def event_generator():
        try:
            async for event in stream_decision_analysis(
                    decision_text=request.decision_text,
                    constraints=request.constraints,
            ):
                # ---- AUDIT DETECTION ----
                if event.get("type") == "decision_audit":
                    yield format_sse("audit:key_factors", event["key_factors"])
                    yield format_sse("audit:assumptions", event["assumptions"])
                    yield format_sse("audit:reversal_triggers", event["reversal_triggers"])
                    yield format_sse("audit:recommendation", event["recommendation"])
                    yield format_sse("audit:confidence", event["confidence"])
                    yield format_sse("done", True)
                    return

                # ---- PASS THROUGH EXISTING EVENTS ----
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


def format_sse(event_name: str, data) -> str:
    """
    Standard SSE formatter.
    All events (including audit) pass through here.
    """
    return f"event: {event_name}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"