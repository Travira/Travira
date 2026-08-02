const express = require("express");

const router = express.Router();



const authMiddleware =
require("../middleware/authMiddleware");


const adminMiddleware =
require("../middleware/adminMiddleware");



const {


getPendingPlaces,

approvePlace,

rejectPlace,

getUsers,

deleteAnyPlace


}=require("../controllers/admin");







// All routes below require admin


router.use(
authMiddleware,
adminMiddleware
);







// Pending places

router.get(

"/places/pending",

getPendingPlaces

);







// Approve place

router.put(

"/places/:id/approve",

approvePlace

);







// Reject place

router.put(

"/places/:id/reject",

rejectPlace

);







// Users list

router.get(

"/users",

getUsers

);







// Delete any place

router.delete(

"/places/:id",

deleteAnyPlace

);







module.exports = router;