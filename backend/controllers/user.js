const User = require("../models/user");

const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");




// Generate Access Token

const generateAccessToken = (user)=>{

    return jwt.sign(

        {
            userId:user._id,
            email:user.email
        },

        process.env.JWT_SECRET,

        {
            expiresIn:"15m"
        }

    );

};





// Generate Refresh Token

const generateRefreshToken = (user)=>{

    return jwt.sign(

        {
            userId:user._id
        },

        process.env.JWT_REFRESH_SECRET,

        {
            expiresIn:"30d"
        }

    );

};







// ================= Register =================


exports.register = async(req,res)=>{

try{


const {
name,
email,
password
}=req.body;



const existingUser =
await User.findOne({email});



if(existingUser){

return res.status(400).json({

message:"User already exists"

});

}




const hashedPassword =
await bcrypt.hash(password,10);



const user =
await User.create({

name,
email,
password:hashedPassword

});




res.json({

message:"Registration successful",

user:{

id:user._id,

name:user.name,

email:user.email

}

});



}catch(error){

res.status(500).json({

message:error.message

});

}


};










// ================= Login =================


exports.login = async(req,res)=>{

try{


const {
email,
password
}=req.body;



const user =
await User.findOne({email});



if(!user){

return res.status(404).json({

message:"User not found"

});

}




const match =
await bcrypt.compare(

password,

user.password

);



if(!match){

return res.status(400).json({

message:"Invalid password"

});

}




const accessToken =
generateAccessToken(user);



const refreshToken =
generateRefreshToken(user);




user.refreshTokens.push({

token:refreshToken

});


await user.save();




res.json({

message:"Login successful",

accessToken,

refreshToken,


user:{

id:user._id,

name:user.name,

email:user.email,

role:user.role

}

});



}catch(error){

res.status(500).json({

message:error.message

});

}


};









// ================= Refresh Token =================


exports.refreshToken = async(req,res)=>{

try{


const {
refreshToken
}=req.body;



if(!refreshToken){

return res.status(401).json({

message:"Refresh token required"

});

}




const decoded =
jwt.verify(

refreshToken,

process.env.JWT_REFRESH_SECRET

);



const user =
await User.findById(
decoded.userId
);



if(!user){

return res.status(404).json({

message:"User not found"

});

}




const exists =
user.refreshTokens.some(

item=>item.token===refreshToken

);



if(!exists){

return res.status(403).json({

message:"Invalid refresh token"

});

}




const accessToken =
generateAccessToken(user);



res.json({

accessToken

});



}catch(error){

res.status(403).json({

message:"Invalid refresh token"

});

}

};









// ================= Profile =================


exports.profile = async(req,res)=>{

try{


const user =
await User.findById(req.user.id)

.select("-password -refreshTokens");



res.json({

success:true,

user

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};









// ================= Current User =================


exports.getCurrentUser = async(req,res)=>{

try{


const user =
await User.findById(req.user.id)

.select("-password -refreshTokens")

.populate(
"addedPlaces",
"name city state country imageUrl averageRating"
)

.populate(
"wishlist",
"name city state country imageUrl averageRating"
);



res.json({

success:true,

user

});


}catch(error){

res.status(500).json({

message:error.message

});

}

};









// ================= Notifications =================


exports.getNotifications = async(req,res)=>{

try{


const user =
await User.findById(req.user.id)

.select("notifications");



res.json({

success:true,

notifications:user.notifications

});



}catch(error){

res.status(500).json({

success:false,

message:error.message

});

}

};









// ================= Logout =================


exports.logout = async(req,res)=>{

try{


const {
refreshToken
}=req.body;



const user =
await User.findById(req.user.id);



user.refreshTokens =
user.refreshTokens.filter(

item=>item.token!==refreshToken

);



await user.save();



res.json({

success:true,

message:"Logged out successfully"

});



}catch(error){

res.status(500).json({

message:error.message

});

}

};









// ================= Logout All =================


exports.logoutAll = async(req,res)=>{

try{


const user =
await User.findById(req.user.id);



user.refreshTokens=[];


await user.save();



res.json({

success:true,

message:"Logged out from all devices"

});


}catch(error){

res.status(500).json({

message:error.message

});

}

};