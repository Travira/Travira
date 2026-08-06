const mongoose = require("mongoose");


const userSchema = new mongoose.Schema({


    name:{
        type:String,
        required:true
    },


    email:{
        type:String,
        required:true,
        unique:true
    },


    password:{
        type:String,
        required:true
    },


    profileImage:{
        type:String,
        default:""
    },

    coverImage:{
        type:String,
        default:""
    },

    phone:{
        type:String,
        default:""
    },

    location:{
        type:String,
        default:""
    },

    // For admin applicants: pending | approved | rejected
    adminStatus:{
        type:String,
        enum:["none","pending","approved","rejected"],
        default:"none"
    },



    // Places added by user

    addedPlaces:[
        {
            type:mongoose.Schema.Types.ObjectId,
            ref:"Place"
        }
    ],



    // Wishlist

    wishlist:[
        {
            type:mongoose.Schema.Types.ObjectId,
            ref:"Place"
        }
    ],



    // Visited Places

    visitedPlaces:[

        {

            place:{
                type:mongoose.Schema.Types.ObjectId,
                ref:"Place"
            },


            visitedAt:{
                type:Date,
                default:Date.now
            }

        }

    ],




    // Admin/User Notifications

    notifications:[

        {

            title:{
                type:String,
                required:true
            },


            message:{
                type:String,
                required:true
            },


            read:{
                type:Boolean,
                default:false
            },


            createdAt:{
                type:Date,
                default:Date.now
            }

        }

    ],





    role:{
        type:String,
        enum:["user","admin","superadmin"],
        default:"user"
    },




    // Refresh Tokens

    refreshTokens:[

        {

            token:{
                type:String,
                required:true
            },


            createdAt:{
                type:Date,
                default:Date.now,
                expires:2592000
            }

        }

    ],





    createdAt:{
        type:Date,
        default:Date.now
    }


});



module.exports =
mongoose.model(
"User",
userSchema
);