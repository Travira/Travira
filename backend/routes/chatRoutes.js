const express = require("express");
const router = express.Router();
const authMiddleware = require("../middleware/authMiddleware");
const { chat } = require("../controllers/chat");

// Logged-in users only — guests cannot use the chatbot
router.post("/", authMiddleware, chat);

module.exports = router;
