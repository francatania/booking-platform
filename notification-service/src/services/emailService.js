const nodemailer = require('nodemailer');
const config = require('../config');

let transporter = null;

function getTransporter() {
  if (!transporter && config.smtp.host) {
    transporter = nodemailer.createTransport({
      host: config.smtp.host,
      port: config.smtp.port,
      secure: config.smtp.port === 465,
      auth: {
        user: config.smtp.user,
        pass: config.smtp.pass,
      },
    });
  }
  return transporter;
}

async function sendEmail(to, subject, html) {
  const t = getTransporter();
  if (!t) {
    console.log(`[Email skipped] No SMTP configured — to: ${to}, subject: ${subject}`);
    return null;
  }

  const info = await t.sendMail({
    from: config.smtp.from,
    to,
    subject,
    html,
  });
  console.log(`[Email sent] to: ${to}, messageId: ${info.messageId}`);
  return info;
}

module.exports = { sendEmail };
