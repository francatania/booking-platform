const express = require('express');
const config = require('./config');
const rabbitmq = require('./rabbitmq');
const bookingConsumer = require('./consumers/bookingConsumer');
const notificationsRouter = require('./routes/notifications');

const app = express();
app.use(express.json());

app.use('/api/notifications', notificationsRouter);

app.get('/health', (_req, res) => res.json({ status: 'ok' }));

async function start() {
  try {
    const channel = await rabbitmq.connect();
    await bookingConsumer.start(channel);
    console.log('RabbitMQ consumer started');
  } catch (err) {
    console.error('Failed to connect to RabbitMQ:', err.message);
    console.log('Server will start without RabbitMQ — retrying in 10s...');
    setTimeout(start, 10000);
  }

  app.listen(config.port, () => {
    console.log(`Notification service running on port ${config.port}`);
  });
}

start();
