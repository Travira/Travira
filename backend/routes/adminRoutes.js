const express = require("express");
const router = express.Router();

const authMiddleware = require("../middleware/authMiddleware");
const adminMiddleware = require("../middleware/adminMiddleware");

const {
  getAllPlaces,
  getPlaceAdminDetail,
  getPendingPlaces,
  approvePlace,
  rejectPlace,
  setPlaceStatus,
  updateAnyPlace,
  deleteAnyPlace,
  adminAddPlace,
  getUsers,
  getUserDetail,
  adminCreateUser,
  getAdmins,
  setAdminStatus
} = require("../controllers/admin");

// All routes require logged-in admin (or superadmin)
router.use(authMiddleware, adminMiddleware);

// ── Places ──
router.get("/places", getAllPlaces);
router.get("/places/pending", getPendingPlaces);
router.get("/places/:id", getPlaceAdminDetail);
router.put("/places/:id/approve", approvePlace);
router.put("/places/:id/reject", rejectPlace);
router.put("/places/:id/status", setPlaceStatus);
router.put("/places/:id", updateAnyPlace);
router.delete("/places/:id", deleteAnyPlace);
router.post("/places", adminAddPlace);

// ── Users ──
router.get("/users", getUsers);
router.get("/users/:id", getUserDetail);
router.post("/users", adminCreateUser);

// ── Admins (Preet / superadmin only – enforced in controller) ──
router.get("/admins", getAdmins);
router.put("/admins/:id/status", setAdminStatus);

module.exports = router;
