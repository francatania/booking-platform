FROM python:3.12-slim AS build
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app ./app
COPY tests ./tests
RUN pytest tests/
RUN echo "tests passed" > /tmp/tests_ok

FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app ./app
COPY --from=build /tmp/tests_ok /tmp/tests_ok
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]