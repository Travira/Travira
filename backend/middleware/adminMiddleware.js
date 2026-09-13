const User = require("../models/user");
if (process.env.NODE_ENV !== "production") require("dotenv").config();

/**
 * Allows role: admin or superadmin (seeded main admin).
 * No pending-admin approval flow.
 */
const adminMiddleware = async (req, res, next) => {
  try {
    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    const isAdmin =
      user.role === "admin" ||
      user.role === "superadmin" ||
      user.email === process.env.ROOT_ADMIN_EMAIL;

    if (!isAdmin) {
      return res.status(403).json({ message: "Access denied. Admin only." });
    }

    req.adminUser = user;
    next();
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = adminMiddleware;
