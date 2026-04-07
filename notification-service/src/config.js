module.exports = {
  port: process.env.PORT || 3000,
  db: {
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT) || 5432,
    database: process.env.DB_NAME || 'notification_db',
    user: process.env.DB_USER || 'franco',
    password: process.env.DB_PASSWORD || 'franco123',
  },
  jwtSecret: process.env.JWT_SECRET || 'mySecretKey1234567890mySecretKey1234567890',
  rabbitmqUrl: process.env.RABBITMQ_URL || 'amqp://franco:franco123@localhost:5672',
  authServiceUrl: process.env.AUTH_SERVICE_URL || 'http://localhost:8081/api',
  smtp: {
    host: process.env.SMTP_HOST || '',
    port: parseInt(process.env.SMTP_PORT) || 587,
    user: process.env.SMTP_USER || '',
    pass: process.env.SMTP_PASS || '',
    from: process.env.SMTP_FROM || 'noreply@bookly.com',
  },
};
