const amqplib = require('amqplib');
const config = require('./config');

const EXCHANGE = 'booking_events';
const QUEUE = 'notification_queue';
const ROUTING_KEYS = ['booking.created', 'booking.confirmed', 'booking.cancelled'];

let connection = null;
let channel = null;

async function connect() {
  connection = await amqplib.connect(config.rabbitmqUrl);
  channel = await connection.createChannel();

  await channel.assertExchange(EXCHANGE, 'topic', { durable: true });
  await channel.assertQueue(QUEUE, { durable: true });

  for (const key of ROUTING_KEYS) {
    await channel.bindQueue(QUEUE, EXCHANGE, key);
  }

  console.log(`RabbitMQ connected — queue "${QUEUE}" bound to exchange "${EXCHANGE}"`);
  return channel;
}

function getChannel() {
  return channel;
}

module.exports = { connect, getChannel, QUEUE };
