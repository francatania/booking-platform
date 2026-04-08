const translations = {
  en: {
    booking_created: {
      subject: 'New booking received',
      greeting: 'Hello',
      body: (serviceName, date, startTime) =>
        `A new booking has been made for <strong>${serviceName}</strong> on <strong>${date}</strong> at <strong>${startTime}</strong>.`,
      footer: 'Log in to manage your bookings.',
    },
    booking_confirmed: {
      subject: 'Booking confirmed',
      greeting: 'Hello',
      body: (serviceName, date, startTime) =>
        `Your booking for <strong>${serviceName}</strong> on <strong>${date}</strong> at <strong>${startTime}</strong> has been confirmed.`,
      footer: 'Thank you for choosing us!',
    },
  },
  es: {
    booking_created: {
      subject: 'Nueva reserva recibida',
      greeting: 'Hola',
      body: (serviceName, date, startTime) =>
        `Se ha realizado una nueva reserva: <strong>${serviceName}</strong> para el día <strong>${date}</strong> a las <strong>${startTime}</strong>.`,
      footer: 'Iniciá sesión para gestionar tus reservas.',
    },
    booking_confirmed: {
      subject: 'Reserva confirmada',
      greeting: 'Hola',
      body: (serviceName, date, startTime) =>
        `Tu reserva para <strong>${serviceName}</strong> del día <strong>${date}</strong> a las <strong>${startTime}</strong> ha sido confirmada.`,
      footer: '¡Gracias por elegirnos!',
    },
  },
};

function getLang(acceptLanguage) {
  if (!acceptLanguage) return 'en';
  const primary = acceptLanguage.split(',')[0].split('-')[0].toLowerCase().trim();
  return translations[primary] ? primary : 'en';
}

function buildEmail(type, { serviceName, date, startTime, language, userName }) {
  const lang = getLang(language);
  const t = translations[lang][type];
  if (!t) return null;

  const greeting = userName ? `${t.greeting}, ${userName}` : t.greeting;

  const html = `
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; background: #f9f9f9;">
      <div style="background: #ffffff; border-radius: 8px; padding: 32px; box-shadow: 0 1px 4px rgba(0,0,0,0.08);">
        <h2 style="color: #1a1a2e; margin-top: 0;">${t.subject}</h2>
        <p style="color: #333; font-size: 15px;">${greeting},</p>
        <p style="color: #333; font-size: 15px;">${t.body(serviceName, date, startTime)}</p>
        <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />
        <p style="color: #888; font-size: 13px;">${t.footer}</p>
      </div>
    </div>
  `;

  return { subject: t.subject, html };
}

module.exports = { buildEmail };
