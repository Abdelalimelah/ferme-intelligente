FROM python:3.11-slim
WORKDIR /app

RUN pip install --no-cache-dir requests

COPY arduino_simulator.py .

# API URL overridable via env
ENV API_URL=http://backend:8080/api/iot/data

CMD ["python", "arduino_simulator.py", "--interval", "5"]
