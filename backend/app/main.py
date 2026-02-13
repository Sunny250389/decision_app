from fastapi import FastAPI
from app.api.decision import router as decision_router

app = FastAPI(title="Decision Intelligence API")
app.include_router(decision_router)