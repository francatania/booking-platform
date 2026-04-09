const db = require('../database');

async function create({ userId, type, message, bookingId }) {
  const result = await db.query(
    `INSERT INTO notification (user_id, type, message, booking_id)
     VALUES ($1, $2, $3, $4) RETURNING *`,
    [userId, type, message, bookingId || null],
  );
  return result.rows[0];
}

async function getByUser(userId, { limit = 20, offset = 0 } = {}) {
  const result = await db.query(
    `SELECT * FROM notification WHERE user_id = $1 ORDER BY created_at DESC LIMIT $2 OFFSET $3`,
    [userId, limit, offset],
  );
  return result.rows;
}

async function getUnreadCount(userId) {
  const result = await db.query(
    `SELECT COUNT(*)::int AS count FROM notification WHERE user_id = $1 AND is_read = false`,
    [userId],
  );
  return result.rows[0].count;
}

async function markAsRead(id, userId) {
  const result = await db.query(
    `UPDATE notification SET is_read = true WHERE id = $1 AND user_id = $2 RETURNING *`,
    [id, userId],
  );
  return result.rows[0] || null;
}

async function markAllAsRead(userId) {
  const result = await db.query(
    `UPDATE notification SET is_read = true WHERE user_id = $1 AND is_read = false`,
    [userId],
  );
  return result.rowCount;
}

module.exports = { create, getByUser, getUnreadCount, markAsRead, markAllAsRead };
