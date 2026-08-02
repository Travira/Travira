const express = require("express");

const router = express.Router();


const authMiddleware =
require("../middleware/authMiddleware");



const {

getPlaces,

getPlaceById,

addPlace,

getMyPlaces,

updatePlace,

deletePlace,

addWishlist,

removeWishlist,

getWishlist,

ratePlace

}=require("../controllers/place");




// Public

router.get(
"/",
getPlaces
);


router.get(
"/:id",
getPlaceById
);




// User Place

router.post(
"/add",
authMiddleware,
addPlace
);


router.get(
"/user/my-places",
authMiddleware,
getMyPlaces
);



router.put(
"/:id",
authMiddleware,
updatePlace
);



router.delete(
"/:id",
authMiddleware,
deletePlace
);




// Wishlist

router.post(
"/:id/wishlist",
authMiddleware,
addWishlist
);


router.delete(
"/:id/wishlist",
authMiddleware,
removeWishlist
);



router.get(
"/user/wishlist",
authMiddleware,
getWishlist
);




// Rating

router.post(
"/:id/rating",
authMiddleware,
ratePlace
);



module.exports = router;