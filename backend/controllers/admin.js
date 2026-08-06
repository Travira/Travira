const Place = require("../models/place");
const User = require("../models/user");
const bcrypt = require("bcrypt");
if (process.env.NODE_ENV !== "production") require("dotenv").config();

// ── Helpers ──────────────────────────────────────────

function isSuperAdmin(user) {
  return user && (user.role === "superadmin" || user.email === process.env.ROOT_ADMIN_EMAIL);
}

async function notifyUser(userId, title, message) {
  if (!userId) return;
  await User.findByIdAndUpdate(userId, {
    $push: { notifications: { title, message } }
  });
}

// ================= All places (admin) =================

exports.getAllPlaces = async (req, res) => {
  try {
    const { status } = req.query; // pending | approved | rejected | all
    const filter = {};
    if (status && status !== "all") filter.approvalStatus = status;

    const places = await Place.find(filter)
      .populate("addedBy", "name email phone location")
      .populate("reviewedBy", "name email")
      .sort({ createdAt: -1 });

    const counts = {
      pending: await Place.countDocuments({ approvalStatus: "pending" }),
      approved: await Place.countDocuments({ approvalStatus: "approved" }),
      rejected: await Place.countDocuments({ approvalStatus: "rejected" }),
      total: await Place.countDocuments({})
    };

    res.json({ success: true, places, counts });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.getPlaceAdminDetail = async (req, res) => {
  try {
    const place = await Place.findById(req.params.id)
      .populate("addedBy", "name email phone location role")
      .populate("reviewedBy", "name email")
      .populate("ratings.user", "name email");

    if (!place) return res.status(404).json({ message: "Place not found" });

    // How many users wishlisted this place
    const wishlistCount = await User.countDocuments({ wishlist: place._id });

    res.json({
      success: true,
      place,
      stats: {
        visitorsCount: place.visitorsCount || 0,
        averageRating: place.averageRating || place.rating || 0,
        ratingsCount: (place.ratings || []).length,
        wishlistCount
      }
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.getPendingPlaces = async (req, res) => {
  try {
    const places = await Place.find({ approvalStatus: "pending" })
      .populate("addedBy", "name email")
      .sort({ createdAt: -1 });
    res.json({ success: true, places });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.approvePlace = async (req, res) => {
  try {
    const { message } = req.body;
    const place = await Place.findById(req.params.id);
    if (!place) return res.status(404).json({ message: "Place not found" });

    place.approvalStatus = "approved";
    place.reviewedBy = req.user.id;
    place.reviewedAt = new Date();
    place.adminFeedback = message || "Place approved";
    await place.save();

    await notifyUser(
      place.addedBy,
      "Place Approved",
      message || "Your place has been approved and is now visible on Travira."
    );

    res.json({ success: true, message: "Place approved successfully", place });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.rejectPlace = async (req, res) => {
  try {
    const { feedback, message } = req.body;
    const text = feedback || message;
    if (!text) return res.status(400).json({ message: "Feedback is required" });

    const place = await Place.findById(req.params.id);
    if (!place) return res.status(404).json({ message: "Place not found" });

    place.approvalStatus = "rejected";
    place.adminFeedback = text;
    place.reviewedBy = req.user.id;
    place.reviewedAt = new Date();
    await place.save();

    await notifyUser(place.addedBy, "Place Rejected", text);

    res.json({ success: true, message: "Place rejected", place });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

/** Set status: pending | approved | rejected (with optional feedback) */
exports.setPlaceStatus = async (req, res) => {
  try {
    const { status, feedback, message } = req.body;
    const allowed = ["pending", "approved", "rejected"];
    if (!allowed.includes(status)) {
      return res.status(400).json({ message: "Invalid status" });
    }

    const place = await Place.findById(req.params.id);
    if (!place) return res.status(404).json({ message: "Place not found" });

    place.approvalStatus = status;
    place.reviewedBy = req.user.id;
    place.reviewedAt = new Date();
    if (feedback || message) place.adminFeedback = feedback || message;
    await place.save();

    const title =
      status === "approved"
        ? "Place Approved"
        : status === "rejected"
        ? "Place Rejected"
        : "Place Status Updated";
    const msg =
      place.adminFeedback ||
      `Your place status was changed to ${status} by admin.`;

    await notifyUser(place.addedBy, title, msg);

    res.json({ success: true, message: `Place set to ${status}`, place });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.updateAnyPlace = async (req, res) => {
  try {
    const existing = await Place.findById(req.params.id);
    if (!existing) return res.status(404).json({ message: "Place not found" });

    const feedbackNote =
      typeof req.body.editNote === "string" ? req.body.editNote.trim() : "";
    const { editNote, ...fields } = req.body;

    const place = await Place.findByIdAndUpdate(
      req.params.id,
      {
        ...fields,
        ...(feedbackNote ? { adminFeedback: feedbackNote } : {})
      },
      { new: true }
    ).populate("addedBy", "name email");

    const msg = feedbackNote
      ? `An admin updated "${existing.name}". Feedback: ${feedbackNote}`
      : `An admin updated details of your place "${existing.name}".`;

    await notifyUser(existing.addedBy, "Place Updated by Admin", msg);

    res.json({ success: true, message: "Place updated", place });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.deleteAnyPlace = async (req, res) => {
  try {
    const place = await Place.findById(req.params.id);
    if (!place) return res.status(404).json({ message: "Place not found" });

    await Place.findByIdAndDelete(req.params.id);
    await notifyUser(
      place.addedBy,
      "Place Removed",
      "Your place was removed by an admin."
    );

    res.json({ success: true, message: "Place removed by admin" });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.adminAddPlace = async (req, res) => {
  try {
    const place = new Place({
      ...req.body,
      addedBy: req.user.id,
      approvalStatus: "approved", // admin-added places go live
      reviewedBy: req.user.id,
      reviewedAt: new Date()
    });
    await place.save();
    await User.findByIdAndUpdate(req.user.id, {
      $push: { addedPlaces: place._id }
    });
    const populated = await Place.findById(place._id).populate(
      "addedBy",
      "name email"
    );
    res.json({ success: true, message: "Place added", place: populated });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// ================= Users =================

exports.getUsers = async (req, res) => {
  try {
    // Omit array refs so clients don't get raw ObjectId strings in wishlist/addedPlaces
    const users = await User.find({ role: { $in: ["user", "admin"] } })
      .select("-password -refreshTokens -wishlist -addedPlaces -visitedPlaces -notifications")
      .sort({ createdAt: -1 });
    res.json({ success: true, users });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.getUserDetail = async (req, res) => {
  try {
    const user = await User.findById(req.params.id)
      .select("-password -refreshTokens")
      .populate("wishlist", "name city imageUrl averageRating rating")
      .populate("addedPlaces", "name city approvalStatus imageUrl")
      .populate("visitedPlaces.place", "name city imageUrl");

    if (!user) return res.status(404).json({ message: "User not found" });

    res.json({
      success: true,
      user,
      // Password is hashed – never return plain text
      passwordNote: "Password is hashed and cannot be viewed. Use reset if needed."
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.adminCreateUser = async (req, res) => {
  try {
    const { name, email, password, phone, location, role } = req.body;
    if (!name || !email || !password) {
      return res.status(400).json({ message: "name, email, password required" });
    }

    const exists = await User.findOne({ email });
    if (exists) return res.status(400).json({ message: "User already exists" });

    const hashed = await bcrypt.hash(password, 10);
    const user = await User.create({
      name,
      email,
      password: hashed,
      phone: phone || "",
      location: location || "",
      role: role === "admin" ? "admin" : "user",
      adminStatus: role === "admin" ? "approved" : "none"
    });

    res.json({
      success: true,
      message: "User created",
      user: { id: user._id, name: user.name, email: user.email, role: user.role }
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

/** Admin updates a user (name, email, phone, location, bio, password, role). */
exports.adminUpdateUser = async (req, res) => {
  try {
    const target = await User.findById(req.params.id);
    if (!target) return res.status(404).json({ message: "User not found" });
    if (target.role === "superadmin" || target.email === process.env.ROOT_ADMIN_EMAIL) {
      return res.status(403).json({ message: "Cannot modify main admin account" });
    }

    const { name, email, phone, location, bio, password, role } = req.body;
    if (name !== undefined) target.name = name;
    if (email !== undefined) {
      const clash = await User.findOne({ email, _id: { $ne: target._id } });
      if (clash) return res.status(400).json({ message: "Email already in use" });
      target.email = email;
    }
    if (phone !== undefined) target.phone = phone;
    if (location !== undefined) target.location = location;
    if (bio !== undefined) target.bio = bio;
    if (password && String(password).length >= 6) {
      target.password = await bcrypt.hash(String(password), 10);
    }
    if (role === "admin" || role === "user") {
      target.role = role;
      target.adminStatus = role === "admin" ? "approved" : "none";
    }

    await target.save();

    await notifyUser(
      target._id,
      "Account updated by admin",
      password
        ? "An admin updated your account details and password. Please log in with the new credentials if shared with you."
        : "An admin updated your account details."
    );

    const safe = await User.findById(target._id)
      .select("-password -refreshTokens")
      .populate("wishlist", "name city imageUrl averageRating")
      .populate("addedPlaces", "name city approvalStatus imageUrl")
      .populate("visitedPlaces.place", "name city imageUrl");

    res.json({ success: true, message: "User updated", user: safe });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.adminDeleteUser = async (req, res) => {
  try {
    const target = await User.findById(req.params.id);
    if (!target) return res.status(404).json({ message: "User not found" });
    if (target.role === "superadmin" || target.email === process.env.ROOT_ADMIN_EMAIL) {
      return res.status(403).json({ message: "Cannot delete main admin" });
    }
    if (target._id.toString() === req.user.id) {
      return res.status(403).json({ message: "Cannot delete yourself" });
    }

    await User.findByIdAndDelete(target._id);
    res.json({ success: true, message: "User deleted" });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// ================= Admin applications (superadmin only) =================

exports.getAdmins = async (req, res) => {
  try {
    const me = await User.findById(req.user.id);
    if (!isSuperAdmin(me)) {
      return res.status(403).json({ message: "Only main admin (Preet) can view admins" });
    }

    const admins = await User.find({
      $or: [{ role: "admin" }, { role: "superadmin" }, { adminStatus: { $ne: "none" } }]
    })
      .select("-password -refreshTokens -wishlist -addedPlaces -visitedPlaces -notifications")
      .sort({ createdAt: -1 });

    res.json({ success: true, admins });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

exports.setAdminStatus = async (req, res) => {
  try {
    const me = await User.findById(req.user.id);
    if (!isSuperAdmin(me)) {
      return res.status(403).json({ message: "Only main admin (Preet) can manage admins" });
    }

    const { status, feedback } = req.body; // pending | approved | rejected
    const allowed = ["pending", "approved", "rejected"];
    if (!allowed.includes(status)) {
      return res.status(400).json({ message: "Invalid status" });
    }

    const target = await User.findById(req.params.id);
    if (!target) return res.status(404).json({ message: "User not found" });
    if (target.role === "superadmin" || target.email === process.env.ROOT_ADMIN_EMAIL) {
      return res.status(403).json({ message: "Cannot change main admin" });
    }

    target.adminStatus = status;
    if (status === "approved") target.role = "admin";
    if (status === "rejected") target.role = "user";
    await target.save();

    const note =
      typeof feedback === "string" && feedback.trim()
        ? ` Feedback: ${feedback.trim()}`
        : "";

    await notifyUser(
      target._id,
      status === "approved"
        ? "Admin Access Approved"
        : status === "rejected"
          ? "Admin Access Rejected"
          : "Admin Access Update",
      status === "approved"
        ? `You are now an admin on Travira.${note}`
        : `Your admin application status: ${status}.${note}`
    );

    res.json({
      success: true,
      message: `Admin status set to ${status}`,
      user: {
        id: target._id,
        name: target.name,
        email: target.email,
        role: target.role,
        adminStatus: target.adminStatus
      }
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

/** Superadmin deletes an admin applicant / admin account */
exports.deleteAdmin = async (req, res) => {
  try {
    const me = await User.findById(req.user.id);
    if (!isSuperAdmin(me)) {
      return res.status(403).json({ message: "Only main admin (Preet) can delete admins" });
    }
    const target = await User.findById(req.params.id);
    if (!target) return res.status(404).json({ message: "User not found" });
    if (target.role === "superadmin" || target.email === process.env.ROOT_ADMIN_EMAIL) {
      return res.status(403).json({ message: "Cannot delete main admin" });
    }

    await notifyUser(
      target._id,
      "Admin access removed",
      "Your admin access was removed by the main admin."
    );

    await User.findByIdAndDelete(target._id);
    res.json({ success: true, message: "Admin removed" });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};
