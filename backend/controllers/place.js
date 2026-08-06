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
      .sort({ createdAt: -1 });

    res.json({
      success: true,
      data: places
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
    }).populate("addedBy", "name email");

    if (!place) {
      return res.status(404).json({
        success: false,
        message: "Place not found"
      });
    }

    res.json({
      success: true,
      place
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
};







// ================= Add Place =================


exports.addPlace = async(req,res)=>{

try{


const place =
new Place({

...req.body,

addedBy:req.user.id,

approvalStatus:"pending"

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

success:true,

message:"Place submitted for approval",

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


exports.ratePlace = async(req,res)=>{

try{


const {
value
}=req.body;



const place =
await Place.findById(req.params.id);



if(!place){

return res.status(404).json({

message:"Place not found"

});

}



const existing =
place.ratings.find(

r=>r.user.toString()
===req.user.id

);



if(existing){

existing.value=value;

}

else{


place.ratings.push({

user:req.user.id,

value

});


await User.findByIdAndUpdate(

req.user.id,

{

$addToSet:{

visitedPlaces:{

place:place._id

}

}

}

);


}



const total =
place.ratings.reduce(

(sum,r)=>sum+r.value,

0

);



place.averageRating =
total/place.ratings.length;


place.visitorsCount =
place.ratings.length;



await place.save();



res.json({

success:true,

message:"Rating submitted",

averageRating:place.averageRating

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};