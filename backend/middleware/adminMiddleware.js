const User = require("../models/user");
if (process.env.NODE_ENV !== "production") require("dotenv").config();

/**
 * Allows role: admin or superadmin.
 * Admin applicants with adminStatus !== approved are blocked.
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

    // Pending admin applications cannot use admin APIs yet
    if (
      user.role === "admin" &&
      user.adminStatus === "pending" &&
      user.email !== process.env.ROOT_ADMIN_EMAIL
    ) {
      return res.status(403).json({
        message: "Your admin account is pending approval by the main admin."
      });
    }

    req.adminUser = user;
    next();
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = adminMiddleware;
