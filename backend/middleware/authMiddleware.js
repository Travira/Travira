const jwt = require("jsonwebtoken");

const authMiddleware = (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader) return res.status(401).json({ message: "No token provided" });

    const parts = authHeader.split(" ");
    if (parts.length !== 2) return res.status(401).json({ message: "Invalid token format" });

    const token = parts[1];
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    req.user = { id: decoded.userId || decoded.id, email: decoded.email };
    next();
  } catch (error) {
    return res.status(401).json({ message: "Unauthorized user" });
  }
};

module.exports = authMiddleware;