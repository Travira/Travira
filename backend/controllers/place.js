const Place = require("../models/place");
const User = require("../models/user");



// ================= Get Approved Places =================

exports.getPlaces = async (req, res) => {
  try {
    // Public home feed: approved places + legacy docs that predate approvalStatus
    const places = await Place.find({
      $or: [
        { approvalStatus: "approved" },
        { approvalStatus: { $exists: false } },
        { approvalStatus: null },
        { approvalStatus: "" }
      ]
    })
      .populate("addedBy", "name email")
      .sort({ createdAt: -1 })
      .lean();

    const data = places.map((p) => ({
      ...p,
      ratingsCount: Array.isArray(p.ratings) ? p.ratings.length : 0
    }));

    res.json({
      success: true,
      data
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};







// ================= Get Single Approved Place =================

exports.getPlaceById = async (req, res) => {
  try {
    const place = await Place.findOne({
      _id: req.params.id,
      $or: [
        { approvalStatus: "approved" },
        { approvalStatus: { $exists: false } },
        { approvalStatus: null },
        { approvalStatus: "" }
      ]
    })
      .populate("addedBy", "name email")
      .lean();

    if (!place) {
      return res.status(404).json({
        success: false,
        message: "Place not found"
      });
    }

    res.json({
      success: true,
      place: {
        ...place,
        ratingsCount: Array.isArray(place.ratings) ? place.ratings.length : 0
      }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};







// ================= Add Place =================
// Admins / superadmins skip approval and go live immediately.

exports.addPlace = async (req, res) => {
  try {
    const actor = await User.findById(req.user.id).select("role email");
    const isAdminActor =
      actor &&
      (actor.role === "admin" ||
        actor.role === "superadmin" ||
        actor.email === "preet@travira.app");

    const place = new Place({
      ...req.body,
      addedBy: req.user.id,
      approvalStatus: isAdminActor ? "approved" : "pending"
    });



await place.save();



await User.findByIdAndUpdate(

req.user.id,

{

$push:{

addedPlaces:place._id

}

}

);



res.json({
      success: true,
      message: isAdminActor
        ? "Place published"
        : "Place submitted for approval",
      place
    });



}catch(error){

res.status(500).json({

message:error.message

});

}

};









// ================= My Added Places =================


exports.getMyPlaces = async(req,res)=>{

try{


const places = await Place.find({
  addedBy: req.user.id
}).populate("addedBy", "name email");

res.json({
  success: true,
  places
});



}catch(error){

res.status(500).json({

message:error.message

});

}

};








// ================= Update Place (owner) =================
// When the owner edits, place goes back to pending and all admins get a notification.

exports.updatePlace = async (req, res) => {
  try {
    const place = await Place.findById(req.params.id);

    if (!place) {
      return res.status(404).json({ message: "Place not found" });
    }

    if (place.addedBy.toString() !== req.user.id) {
      return res.status(403).json({ message: "You cannot update this place" });
    }

    const feedbackNote =
      typeof req.body.editNote === "string" ? req.body.editNote.trim() : "";

    const { editNote, ...fields } = req.body;

    const updated = await Place.findByIdAndUpdate(
      req.params.id,
      {
        ...fields,
        approvalStatus: "pending",
        adminFeedback: feedbackNote || ""
      },
      { new: true }
    ).populate("addedBy", "name email");

    // Notify all admins / superadmins that a user edited a place
    const admins = await User.find({
      role: { $in: ["admin", "superadmin"] }
    }).select("_id");

    const title = "Place edited by user";
    const message = feedbackNote
      ? `${req.user.name || "A user"} edited "${place.name}". Note: ${feedbackNote}`
      : `${req.user.name || "A user"} edited "${place.name}" and resubmitted it for review.`;

    await Promise.all(
      admins.map((a) =>
        User.findByIdAndUpdate(a._id, {
          $push: { notifications: { title, message } }
        })
      )
    );

    res.json({
      success: true,
      message: "Place updated and sent for review",
      place: updated
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};









// ================= Delete Place =================


exports.deletePlace = async(req,res)=>{

try{


const place =
await Place.findById(req.params.id);



if(!place){

return res.status(404).json({

message:"Place not found"

});

}



if(
place.addedBy.toString()
!== req.user.id
){

return res.status(403).json({

message:"You cannot delete this place"

});

}



await Place.findByIdAndDelete(
req.params.id
);



await User.findByIdAndUpdate(

req.user.id,

{

$pull:{

addedPlaces:req.params.id

}

}

);



res.json({

success:true,

message:"Place deleted successfully"

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};










// ================= Wishlist =================


exports.addWishlist = async(req,res)=>{

try{


const user =
await User.findById(req.user.id);



if(user.wishlist.includes(req.params.id)){

return res.json({

message:"Already in wishlist"

});

}



user.wishlist.push(req.params.id);


await user.save();



res.json({

success:true,

message:"Added to wishlist"

});


}catch(error){

res.status(500).json({

message:error.message

});

}

};






exports.removeWishlist = async(req,res)=>{

try{


const user =
await User.findById(req.user.id);



user.wishlist =
user.wishlist.filter(

id=>id.toString()
!==req.params.id

);



await user.save();



res.json({

success:true,

message:"Removed from wishlist"

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};







exports.getWishlist = async (req, res) => {
  try {
    const user = await User.findById(req.user.id).populate(
      "wishlist",
      "name shortDescription description city state country location imageUrl averageRating visitorsCount approvalStatus"
    );

    res.json({
      success: true,
      wishlist: user?.wishlist || []
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};









// ================= Rating =================
// Body: { value: 1-5, feedback?: string }
// Updates averageRating from all ratings. Does NOT change visitorsCount
// (visitorsCount is owned by mark-visited). Optionally marks place visited.

exports.ratePlace = async (req, res) => {
  try {
    const raw = req.body?.value;
    const value = Number(raw);
    const feedback =
      typeof req.body?.feedback === "string" ? req.body.feedback.trim() : "";

    if (!Number.isFinite(value) || value < 1 || value > 5) {
      return res.status(400).json({
        success: false,
        message: "Rating must be a number between 1 and 5"
      });
    }

    const place = await Place.findById(req.params.id);
    if (!place) {
      return res.status(404).json({ success: false, message: "Place not found" });
    }

    const existing = place.ratings.find(
      (r) => r.user && r.user.toString() === req.user.id
    );

    if (existing) {
      existing.value = value;
      if (feedback) existing.feedback = feedback;
      existing.createdAt = new Date();
    } else {
      place.ratings.push({
        user: req.user.id,
        value,
        feedback: feedback || "",
        createdAt: new Date()
      });
    }

    const total = place.ratings.reduce((sum, r) => sum + (r.value || 0), 0);
    place.averageRating =
      place.ratings.length > 0
        ? Math.round((total / place.ratings.length) * 10) / 10
        : 0;

    await place.save();

    res.json({
      success: true,
      message: existing ? "Rating updated" : "Rating submitted",
      averageRating: place.averageRating,
      ratingsCount: place.ratings.length,
      visitorsCount: place.visitorsCount,
      place: {
        _id: place._id,
        averageRating: place.averageRating,
        visitorsCount: place.visitorsCount,
        ratingsCount: place.ratings.length
      }
    });



}catch(error){

res.status(500).json({

message:error.message

});

}

};