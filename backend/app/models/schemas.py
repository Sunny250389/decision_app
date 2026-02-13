from pydantic import BaseModel
from typing import List, Literal, Optional
from typing_extensions import Annotated
from pydantic import Field


class DecisionRequest(BaseModel):
    decision_text: str
    constraints: List[str] = []
    context: dict = {}


class Option(BaseModel):
    title: str
    pros: List[str]
    cons: List[str]
    risk: str


class Recommendation(BaseModel):
    title: str
    risk: str

class DecisionAudit(BaseModel):
    type: Literal["decision_audit"]

    recommendation: str
    confidence: Annotated[float, Field(ge=0.0, le=1.0)]

    key_factors: List[str]
    assumptions: List[str]
    reversal_triggers: List[str]

class OptionDimensions(BaseModel):
    UPSIDE: float
    STABILITY: float
    FLEXIBILITY: float
    LEARNING_VALUE: float
    EFFORT: float
    EMOTIONAL_COST: float


class DecisionOption(BaseModel):
    id: str
    title: str
    description: Optional[str] = ""
    pros: List[str] = []
    cons: List[str] = []
    risk: str
    dimensions: OptionDimensions