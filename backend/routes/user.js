const express = require("express");

const router = express.Router();


const authMiddleware =
require("../middleware/authMiddleware");



const {

register,

login,

profile,

getCurrentUser,

refreshToken,

logout,

logoutAll,

getNotifications

}=require("../controllers/user");





router.post(
"/register",
register
);



router.post(
"/login",
login
);



router.post(
"/refresh-token",
refreshToken
);



router.get(
"/profile",
authMiddleware,
profile
);



router.get(
"/me",
authMiddleware,
getCurrentUser
);



router.get(
"/notifications",
authMiddleware,
getNotifications
);



router.post(
"/logout",
authMiddleware,
logout
);



router.post(
"/logout-all",
authMiddleware,
logoutAll
);



module.exports = router;