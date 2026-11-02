"""
Backwards-compatibility shim for legacy gemini_client imports.
Transparently delegates to the unified AiGatewayClient (Oracle VM AI Gateway + Gemini failover).
"""

from worker.clients.ai_gateway_client import (
    AiGatewayClient,
    HighlightRerankResult,
    ai_gateway_client,
)

GeminiClient = AiGatewayClient
gemini_client = ai_gateway_client
