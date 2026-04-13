const db = require('../database');

/**
 * Creates a new notification record in the database.
 *
 * @param {object} params - Notification data.
 * @param {number} params.userId - The ID of the user to notify.
 * @param {string} params.type - The notification type (e.g. 'booking.created').
 * @param {string} params.message - The human-readable notification message.
 * @param {number|null} params.bookingId - The related booking ID, or null if not applicable.
 * @returns {Promise<object>} The newly created notification row.
 */
async function create({ userId, type, message, bookingId }) {
  const result = await db.query(
    `INSERT INTO notification (user_id, type, message, booking_id)
     VALUES ($1, $2, $3, $4) RETURNING *`,
    [userId, type, message, bookingId || null],
  );
  return result.rows[0];
}

/**
 * Returns a paginated list of notifications for a user, ordered by newest first.
 *
 * @param {number} userId - The user whose notifications to fetch.
 * @param {object} [options={}] - Pagination options.
 * @param {number} [options.limit=20] - Maximum number of records to return.
 * @param {number} [options.offset=0] - Number of records to skip.
 * @returns {Promise<object[]>} Array of notification rows.
 */
async function getByUser(userId, { limit = 20, offset = 0 } = {}) {
  const result = await db.query(
    `SELECT * FROM notification WHERE user_id = $1 ORDER BY created_at DESC LIMIT $2 OFFSET $3`,
    [userId, limit, offset],
  );
  return result.rows;
}

/**
 * Returns the number of unread notifications for a user.
 *
 * @param {number} userId - The user whose unread count to fetch.
 * @returns {Promise<number>} The count of unread notifications.
 */
async function getUnreadCount(userId) {
  const result = await db.query(
    `SELECT COUNT(*)::int AS count FROM notification WHERE user_id = $1 AND is_read = false`,
    [userId],
  );
  return result.rows[0].count;
}

/**
 * Marks a single notification as read, scoped to the owning user.
 *
 * @param {number} id - The notification ID to mark as read.
 * @param {number} userId - The user ID — ensures users can only mark their own notifications.
 * @returns {Promise<object|null>} The updated notification row, or null if not found.
 */
async function markAsRead(id, userId) {
  const result = await db.query(
    `UPDATE notification SET is_read = true WHERE id = $1 AND user_id = $2 RETURNING *`,
    [id, userId],
  );
  return result.rows[0] || null;
}

/**
 * Marks all unread notifications as read for a user.
 *
 * @param {number} userId - The user whose notifications to mark as read.
 * @returns {Promise<number>} The number of rows updated.
 */
async function markAllAsRead(userId) {
  const result = await db.query(
    `UPDATE notification SET is_read = true WHERE user_id = $1 AND is_read = false`,
    [userId],
  );
  return result.rowCount;
}

module.exports = { create, getByUser, getUnreadCount, markAsRead, markAllAsRead };
