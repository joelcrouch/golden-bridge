FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    && rm -rf /var/lib/apt/lists/*

COPY docker/python/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

RUN mkdir -p /app/python-scripts /app/data
COPY python-scripts/ /app/python-scripts/

CMD ["tail", "-f", "/dev/null"]