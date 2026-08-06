const mongoose = require("mongoose");


const placeSchema = new mongoose.Schema({


    name:{
        type:String,
        required:true
    },


    shortDescription:String,


    description:String,


    city:String,


    state:String,


    country:String,


    location:String,


    imageUrl:String,



    addedBy:{
        type:mongoose.Schema.Types.ObjectId,
        ref:"User",
        required:true
    },



    ratings:[

        {

            user:{
                type:mongoose.Schema.Types.ObjectId,
                ref:"User"
            },


            value:{
                type:Number,
                min:1,
                max:5
            }

        }

    ],



    averageRating:{
        type:Number,
        default:0
    },



    visitorsCount:{
        type:Number,
        default:0
    },



    // Admin Approval System

    approvalStatus:{

        type:String,

        enum:[
            "pending",
            "approved",
            "rejected"
        ],

        default:"pending"

    },



    adminFeedback:{

        type:String,

        default:""

    },



    reviewedBy:{

        type:mongoose.Schema.Types.ObjectId,

        ref:"User"

    },



    reviewedAt:{

        type:Date

    },



    createdAt:{

        type:Date,

        default:Date.now

    }


});


// Collection name "places" (Mongoose default plural of "Place").
// Older seeded documents live here. Forced "place" returned empty lists.
module.exports = mongoose.model("Place", placeSchema, "places");