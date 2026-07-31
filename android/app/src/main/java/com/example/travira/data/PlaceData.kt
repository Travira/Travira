package com.example.travira.data

data class Place(
    val id: Int,
    val name: String,
    val shortDescription: String,
    val city: String,
    val state: String,
    val country: String,
    val location: String,
    val imageUrl: String,
    val rating: Double
)

val places = listOf(

    Place(
        id = 1,
        name = "Taj Mahal",
        shortDescription = "One of the Seven Wonders of the World.",
        city = "Agra",
        state = "Uttar Pradesh",
        country = "India",
        location = "27.1751, 78.0421",
        imageUrl = "https://res.cloudinary.com/yv3rd7a3/image/upload/v1785445561/taj_fffx6e.jpg",
        rating = 4.9
    ),

    Place(
        id = 2,
        name = "Manali",
        shortDescription = "A beautiful hill station surrounded by mountains.",
        city = "Manali",
        state = "Himachal Pradesh",
        country = "India",
        location = "32.2396, 77.1887",
        imageUrl = "https://res.cloudinary.com/yv3rd7a3/image/upload/v1785445755/Manali_City_x12ef7.jpg",
        rating = 4.8
    ),

    Place(
        id = 3,
        name = "Jaipur",
        shortDescription = "The famous Pink City of Rajasthan.",
        city = "Jaipur",
        state = "Rajasthan",
        country = "India",
        location = "26.9124, 75.7873",
        imageUrl = "https://res.cloudinary.com/yv3rd7a3/image/upload/v1785445795/East_facade_Hawa_Mahal_Jaipur_from_ground_level__July_2022__-_img_01.jpg_lwhxdr.webp",
        rating = 4.7
    ),

    Place(
        id = 4,
        name = "Goa",
        shortDescription = "Known for beaches, nightlife and Portuguese heritage.",
        city = "Panaji",
        state = "Goa",
        country = "India",
        location = "15.4909, 73.8278",
        imageUrl = "https://res.cloudinary.com/yv3rd7a3/image/upload/v1785445828/BeachFun_wnqjjg.jpg",
        rating = 4.8
    ),

    Place(
        id = 5,
        name = "Mysore Palace",
        shortDescription = "A magnificent royal palace with rich history.",
        city = "Mysuru",
        state = "Karnataka",
        country = "India",
        location = "12.3052, 76.6552",
        imageUrl = "YOUR_MYSORE_PALACE_CLOUDINARY_URL",
        rating = 4.8
    ),

    Place(
        id = 6,
        name = "Golden Temple",
        shortDescription = "The holiest Sikh shrine with a golden façade.",
        city = "Amritsar",
        state = "Punjab",
        country = "India",
        location = "31.6200, 74.8765",
        imageUrl = "https://res.cloudinary.com/yv3rd7a3/image/upload/v1785445859/The_Golden_Temple_of_Amrithsar_7_ybwvqt.jpg",
        rating = 4.9
    ),

    Place(
        id = 7,
        name = "Dal Lake",
        shortDescription = "A peaceful lake famous for houseboats.",
        city = "Srinagar",
        state = "Jammu and Kashmir",
        country = "India",
        location = "34.0837, 74.7973",
        imageUrl = "YOUR_DAL_LAKE_CLOUDINARY_URL",
        rating = 4.8
    ),

    Place(
        id = 8,
        name = "Rann of Kutch",
        shortDescription = "A vast white salt desert with stunning landscapes.",
        city = "Bhuj",
        state = "Gujarat",
        country = "India",
        location = "23.7337, 69.8597",
        imageUrl = "YOUR_RANN_OF_KUTCH_CLOUDINARY_URL",
        rating = 4.7
    )
)
