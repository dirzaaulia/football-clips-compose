FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PYTHONPATH=/app

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . /app/worker

# Cloud Run dynamic port injection
ENV PORT=8080
ENV WORKER_MODE=continuous

EXPOSE 8080

CMD ["sh", "-c", "uvicorn worker.server:app --host 0.0.0.0 --port ${PORT:-8080}"]
