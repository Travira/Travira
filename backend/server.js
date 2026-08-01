if (process.env.NODE_ENV !== "production") {
    require("dotenv").config();
}

const express = require("express");
const app = express();

const cors = require("cors");
const dns = require("dns");
const mongoose = require("mongoose");
const path = require("path");

const placeRoutes = require("./routes/placeroute");


// ================= DNS Fix (Local Testing) =================
// Fix MongoDB Atlas SRV DNS issue
dns.setServers([
    "8.8.8.8",
    "8.8.4.4"
]);


// ================= Middleware =================

app.use(cors());

app.use(express.json());

app.use(express.urlencoded({
    extended: true
}));


// Static folder (future images/uploads)
app.use(express.static(path.join(__dirname, "public")));


// Request logger
app.use((req, res, next) => {
    console.log(`${req.method} ${req.url}`);
    next();
});


// ================= MongoDB Connection =================

const connectDB = async () => {

    try {

        await mongoose.connect(process.env.MONGODB_URI, {
            serverSelectionTimeoutMS: 10000
        });

        console.log("✅ MongoDB Connected Successfully");

    } catch (error) {

        console.error(
            "MongoDB connection error:",
            error.message
        );

        throw error;
    }
};



// ================= Routes =================

app.get("/", (req, res) => {

    res.send("🚀 Travira Backend is Running...");

});


app.use("/api/places", placeRoutes);



// ================= 404 Handler =================

app.use((req, res) => {

    res.status(404).json({
        success: false,
        message: "API Route Not Found"
    });

});



// ================= Start Server =================

const startServer = async () => {

    try {

        await connectDB();


        const PORT = process.env.PORT || 5000;


        app.listen(PORT, "0.0.0.0", () => {

            console.log(
                `🚀 Server running on port ${PORT}`
            );

        });


    } catch(error){

        console.error(
            "❌ Failed to start server:",
            error.message
        );

        process.exit(1);

    }

};


startServer();