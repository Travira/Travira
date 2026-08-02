const Place = require("../models/place");
const User = require("../models/user");




// ================= Get Pending Places =================


exports.getPendingPlaces = async(req,res)=>{

try{


const places =
await Place.find({

approvalStatus:"pending"

})
.populate(
"addedBy",
"name email"
);



res.json({

success:true,

places

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};









// ================= Approve Place =================


exports.approvePlace = async(req,res)=>{

try{


const {
message
}=req.body;



const place =
await Place.findById(req.params.id);



if(!place){

return res.status(404).json({

message:"Place not found"

});

}




place.approvalStatus="approved";


place.reviewedBy=req.user.id;


place.reviewedAt=new Date();


place.adminFeedback =
message || "Place approved";



await place.save();






// Send notification to user

await User.findByIdAndUpdate(

place.addedBy,

{

$push:{

notifications:{

title:"Place Approved",

message:
message ||
"Your place has been approved and is now visible."

}

}

}

);





res.json({

success:true,

message:"Place approved successfully",

place

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};









// ================= Reject Place =================


exports.rejectPlace = async(req,res)=>{

try{


const {
feedback
}=req.body;



if(!feedback){

return res.status(400).json({

message:"Feedback is required"

});

}



const place =
await Place.findById(req.params.id);



if(!place){

return res.status(404).json({

message:"Place not found"

});

}




place.approvalStatus="rejected";


place.adminFeedback=feedback;


place.reviewedBy=req.user.id;


place.reviewedAt=new Date();



await place.save();







// Notify User

await User.findByIdAndUpdate(

place.addedBy,

{

$push:{

notifications:{

title:"Place Rejected",

message:feedback

}

}

}

);





res.json({

success:true,

message:"Place rejected",

place

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};









// ================= Get All Users =================


exports.getUsers = async(req,res)=>{

try{


const users =
await User.find()

.select("-password -refreshTokens");



res.json({

success:true,

users

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};









// ================= Delete Any Place =================


exports.deleteAnyPlace = async(req,res)=>{

try{


const place =
await Place.findById(req.params.id);



if(!place){

return res.status(404).json({

message:"Place not found"

});

}



await Place.findByIdAndDelete(req.params.id);



res.json({

success:true,

message:"Place removed by admin"

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};