# # backend/app/core/config.py
#
# import os
# from dotenv import load_dotenv
#
# load_dotenv()
#
#
# class Settings:
#     """
#     Central configuration for backend services.
#     """
#
#     # 🔹 Environment
#     ENV: str = os.getenv("ENV", "dev")
#
#     # 🔹 LLM Provider
#     OPENAI_API_KEY: str = os.getenv(
#         "OPENAI_API_KEY",
#         "sk-dummy-dev-key-not-for-production"
#     )
#
#     LLM_MODEL: str = os.getenv(
#         "LLM_MODEL",
#         "gpt-4o"
#     )
#
#     LLM_TEMPERATURE: float = float(
#         os.getenv("LLM_TEMPERATURE", "0.3")
#     )
#
#     # 🔹 Server
#     HOST: str = os.getenv("HOST", "0.0.0.0")
#     PORT: int = int(os.getenv("PORT", "8000"))
#
#     # 🔹 Streaming
#     STREAM_DELAY_MS: int = int(
#         os.getenv("STREAM_DELAY_MS", "0")
#     )
#
#
# settings = Settings()

# app/core/config.py
import os
from dotenv import load_dotenv
from pydantic import BaseModel

load_dotenv()
class Settings(BaseModel):
    # ---- Environment ----
    ENV: str = os.getenv("ENV", "dev")

    # ---- LLM Provider ----
    LLM_PROVIDER: str = os.getenv("LLM_PROVIDER", "perplexity")

    # ---- Perplexity ----
    PERPLEXITY_API_KEY: str | None = os.getenv("PERPLEXITY_API_KEY")
    PERPLEXITY_BASE_URL: str = "https://api.perplexity.ai"
    PERPLEXITY_MODEL: str = os.getenv(
        "PERPLEXITY_MODEL",
        "sonar"
    )

    # ---- Server ----
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))

    # ---- Streaming ----
    STREAM_DELAY_MS: int = int(os.getenv("STREAM_DELAY_MS", "500"))


settings = Settings()