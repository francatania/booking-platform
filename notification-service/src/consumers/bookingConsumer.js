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

async function start(channel) {
  console.log('Booking consumer started');

  channel.consume(QUEUE, async (msg) => {
    if (!msg) return;

    try {
      const routingKey = msg.fields.routingKey;
      const event = JSON.parse(msg.content.toString());
      console.log(`[Event] ${routingKey}:`, event);

      if (routingKey === 'booking.created') {
        const staff = await fetchCompanyStaff(event.operatorId);
        await notifyUsers(staff, {
          type: 'BOOKING_CREATED',
          message: `${event.serviceName};${event.date};${event.startTime}`,
          bookingId: event.bookingId,
          emailType: 'booking_created',
          emailData: {
            serviceName: event.serviceName,
            date: event.date,
            startTime: event.startTime,
            language: event.language,
          },
        });

      } else if (routingKey === 'booking.confirmed') {
        const user = await fetchUser(event.userId);
        if (user) {
          await notifyUsers([user], {
            type: 'BOOKING_CONFIRMED',
            message: `${event.serviceName};${event.date};${event.startTime}`,
            bookingId: event.bookingId,
            emailType: 'booking_confirmed',
            emailData: {
              serviceName: event.serviceName,
              date: event.date,
              startTime: event.startTime,
              language: event.language,
            },
          });
        }
      }

      channel.ack(msg);
    } catch (err) {
      console.error('[Consumer error]', err);
      channel.nack(msg, false, true);
    }
  });
}

module.exports = { start };
