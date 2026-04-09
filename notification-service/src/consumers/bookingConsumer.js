const { QUEUE } = require('../rabbitmq');
const notificationService = require('../services/notificationService');
const emailService = require('../services/emailService');
const { buildEmail } = require('../services/emailTemplates');
const config = require('../config');

async function fetchUser(userId) {
  const res = await fetch(`${config.authServiceUrl}/internal/users/${userId}`);
  if (!res.ok) return null;
  return res.json();
}

async function fetchUsersByCompanyAndRole(companyId, role) {
  const url = `${config.authServiceUrl}/internal/users/by-company?companyId=${companyId}&role=${role}`;
  const res = await fetch(url);
  if (!res.ok) {
    console.error(`[fetchUsersByCompanyAndRole] ${url} -> ${res.status}`);
    return [];
  }
  return res.json();
}

async function fetchCompanyStaff(companyId) {
  const [operators, admins] = await Promise.all([
    fetchUsersByCompanyAndRole(companyId, 'OPERATOR'),
    fetchUsersByCompanyAndRole(companyId, 'ADMIN'),
  ]);
  return [...operators, ...admins];
}

async function notifyUsers(users, { type, message, bookingId, emailType, emailData }) {
  await Promise.all(
    users.map(async (user) => {
      await notificationService.create({ userId: user.id, type, message, bookingId });
      if (user.email) {
        const template = buildEmail(emailType, { ...emailData, userName: user.firstName });
        if (template) {
          await emailService.sendEmail(user.email, template.subject, template.html)
            .catch((err) => console.error('[Email error]', err.message));
        }
      }
    }),
  );
}

const handlers = {
  'booking.created': async (event) => ({
    users: await fetchCompanyStaff(event.operatorId),
    type: 'BOOKING_CREATED',
    emailType: 'booking_created',
  }),

  'booking.confirmed': async (event) => {
    const user = await fetchUser(event.userId);
    return {
      users: user ? [user] : [],
      type: 'BOOKING_CONFIRMED',
      emailType: 'booking_confirmed',
    };
  },

  'booking.cancelled': async (event) => {
    const cancelledByStaff = event.cancelledBy !== 'USER';
    const users = cancelledByStaff
      ? [await fetchUser(event.userId)].filter(Boolean)
      : await fetchCompanyStaff(event.operatorId);
    return {
      users,
      type: 'BOOKING_CANCELLED',
      emailType: 'booking_cancelled',
    };
  },
};

async function start(channel) {
  console.log('Booking consumer started');

  channel.consume(QUEUE, async (msg) => {
    if (!msg) return;

    try {
      const routingKey = msg.fields.routingKey;
      const event = JSON.parse(msg.content.toString());
      console.log(`[Event] ${routingKey}:`, event);

      const handler = handlers[routingKey];
      if (!handler) {
        console.warn(`[Consumer] No handler for routing key: ${routingKey}`);
        channel.ack(msg);
        return;
      }

      const { users, type, emailType } = await handler(event);
      const message = `${event.serviceName};${event.date};${event.startTime}`;
      const emailData = {
        serviceName: event.serviceName,
        date: event.date,
        startTime: event.startTime,
        language: event.language,
      };

      await notifyUsers(users, { type, message, bookingId: event.bookingId, emailType, emailData });

      channel.ack(msg);
    } catch (err) {
      console.error('[Consumer error]', err);
      channel.nack(msg, false, true);
    }
  });
}

module.exports = { start };
