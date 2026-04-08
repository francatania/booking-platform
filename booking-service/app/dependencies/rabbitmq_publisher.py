import json
import pika
from app.config import settings

EXCHANGE = "booking_events"

def publish_event(routing_key: str, payload: dict):
    try:
        connection = pika.BlockingConnection(pika.URLParameters(settings.rabbitmq_url))
        channel = connection.channel()
        channel.exchange_declare(exchange=EXCHANGE, exchange_type="topic", durable=True)

        channel.basic_publish(
            exchange=EXCHANGE,
            routing_key=routing_key,
            body=json.dumps(payload),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type="application/json",
            ),
        )
        connection.close()
    except Exception as e:
        print(f"[RabbitMQ] Failed to publish {routing_key}: {e}")
