const { QUEUE } = require('../rabbitmq');
const notificationService = require('../services/notificationService');
const emailService = require('../services/emailService');
const config = require('../config');

async function fetchUser(userId) {
  const res = await fetch(`${config.authServiceUrl}/internal/users/${userId}`);
  if (!res.ok) return null;
  return res.json();
}

function buildNotification(event, routingKey) {
  if (routingKey === 'booking.created') {
    return {
      userId: event.operatorId,
      type: 'BOOKING_CREATED',
      title: 'New booking received',
      message: `A new booking #${event.bookingId} has been created for ${event.date} at ${event.startTime}.`,
      bookingId: event.bookingId,
    };
  }

  if (routingKey === 'booking.confirmed') {
    return {
      userId: event.userId,
      type: 'BOOKING_CONFIRMED',
      title: 'Booking confirmed',
      message: `Your booking #${event.bookingId} for ${event.date} at ${event.startTime} has been confirmed.`,
      bookingId: event.bookingId,
    };
  }

  return null;
}

async function start(channel) {
  console.log('Booking consumer started');

  channel.consume(QUEUE, async (msg) => {
    if (!msg) return;

    try {
      const routingKey = msg.fields.routingKey;
      const event = JSON.parse(msg.content.toString());
      console.log(`[Event] ${routingKey}:`, event);

      const notification = buildNotification(event, routingKey);
      if (!notification) {
        channel.ack(msg);
        return;
      }

      await notificationService.create(notification);

      const user = await fetchUser(notification.userId);
      if (user && user.email) {
        await emailService.sendEmail(
          user.email,
          notification.title,
          `<p>${notification.message}</p>`,
        ).catch((err) => console.error('[Email error]', err.message));
      }

      channel.ack(msg);
    } catch (err) {
      console.error('[Consumer error]', err);
      channel.nack(msg, false, true);
    }
  });
}

module.exports = { start };
