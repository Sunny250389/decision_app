from pydantic import BaseModel
from typing import List


class DecisionRequest(BaseModel):
    decision_text: str
    constraints: List[str] = []