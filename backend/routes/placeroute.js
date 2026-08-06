const express = require("express");

const router = express.Router();

const authMiddleware = require("../middleware/authMiddleware");

const {
  getPlaces,
  getPlaceById,
  addPlace,
  getMyPlaces,
  updatePlace,
  deletePlace,
  addWishlist,
  removeWishlist,
  getWishlist,
  ratePlace
} = require("../controllers/place");

// ── Public ──────────────────────────────────────────
// List approved / legacy places
router.get("/", getPlaces);

// ── Authenticated user routes (MUST be before /:id) ─
// Otherwise Express treats "user" as an id and these never match.
router.get("/user/my-places", authMiddleware, getMyPlaces);
router.get("/user/wishlist", authMiddleware, getWishlist);

router.post("/add", authMiddleware, addPlace);

// ── Wishlist / rating (static path segments before param) ─
router.post("/:id/wishlist", authMiddleware, addWishlist);
router.delete("/:id/wishlist", authMiddleware, removeWishlist);
router.post("/:id/rating", authMiddleware, ratePlace);

// ── Single place + owner CRUD ───────────────────────
router.get("/:id", getPlaceById);
router.put("/:id", authMiddleware, updatePlace);
router.delete("/:id", authMiddleware, deletePlace);

module.exports = router;
