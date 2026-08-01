const express = require("express");
const router = express.Router();

const Place = require("../models/place");

router.get("/", async (req, res) => {
    try {
        const places = await Place.find();
        res.json(places);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;