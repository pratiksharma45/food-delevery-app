package com.example.data

import com.example.R
import com.example.model.DelhiNcrZone
import com.example.model.Dish
import com.example.model.FoodCategory
import com.example.model.Restaurant

object DemoDelhiNcrData {

    val delhiNcrZones: List<DelhiNcrZone> = listOf(
        DelhiNcrZone("cp", "Connaught Place", "Central Delhi", "110001"),
        DelhiNcrZone("hauz_khas", "Hauz Khas & Saket", "South Delhi", "110016"),
        DelhiNcrZone("rajouri", "Rajouri Garden", "West Delhi", "110027"),
        DelhiNcrZone("chandni_chowk", "Chandni Chowk", "Old Delhi", "110006"),
        DelhiNcrZone("noida_18", "Sector 18 & 62", "Noida", "201301"),
        DelhiNcrZone("cyberhub", "DLF CyberHub", "Gurugram", "122002"),
        DelhiNcrZone("indirapuram", "Indirapuram", "Ghaziabad", "201014"),
        DelhiNcrZone("dwarka", "Dwarka Sector 12", "South West Delhi", "110078"),
        DelhiNcrZone("rohini", "Rohini Sector 7", "North West Delhi", "110085"),
        DelhiNcrZone("laxmi_nagar", "Laxmi Nagar", "East Delhi", "110092")
    )

    val foodCategories: List<FoodCategory> = listOf(
        FoodCategory("biryani", "Biryani", "Awadhi & Hyderabadi", "Popular", R.drawable.img_food_biryani),
        FoodCategory("thali", "Thali", "Complete Indian Meal", "Value", R.drawable.img_food_thali),
        FoodCategory("chaat", "Chaat & Snacks", "Samosa, Tikki, Golgappe", "Dilli Fav", R.drawable.img_food_snacks),
        FoodCategory("paneer", "Paneer & Curries", "Shahi Paneer, Dal Makhani", "Rich", R.drawable.img_food_thali),
        FoodCategory("tandoor", "Tandoor & Kebabs", "Tikka, Seekh, Naan", "Smoky", R.drawable.img_hero_delhi_ncr),
        FoodCategory("momos", "Momos & Chinese", "Tandoori & Steamed", "Hot", R.drawable.img_food_snacks),
        FoodCategory("south_indian", "South Indian", "Dosa, Idli, Filter Coffee", "Light", R.drawable.img_food_snacks),
        FoodCategory("rolls", "Rolls & Parathas", "Kathi Rolls, Stuffed", "Quick", R.drawable.img_hero_delhi_ncr),
        FoodCategory("sweets", "Sweets & Lassi", "Jalebi, Gulab Jamun", "Dessert", R.drawable.img_food_thali),
        FoodCategory("combos", "Family Combos", "Party & Group Packs", "Save 25%", R.drawable.img_food_biryani)
    )

    val restaurants: List<Restaurant> = listOf(
        Restaurant(
            id = "rest_karims",
            name = "Karim's Royal Mughlai",
            locality = "Jama Masjid & CP",
            subRegion = "Central & Old Delhi",
            deliveryZoneIds = listOf("cp", "chandni_chowk", "hauz_khas", "laxmi_nagar", "noida_18"),
            cuisines = listOf("Biryani", "Mughlai", "Tandoor & Kebabs"),
            rating = 4.6,
            reviewCountText = "12.4K+",
            deliveryTimeMins = 28,
            deliveryFeeInr = 25,
            priceForOneInr = 280,
            isPureVeg = false,
            offerText = "20% OFF up to ₹120",
            isRecommended = true,
            imageRes = R.drawable.img_food_biryani,
            dishes = listOf(
                Dish(
                    id = "dish_karim_mutton_biryani",
                    restaurantId = "rest_karims",
                    name = "Old Delhi Mutton Dum Biryani",
                    categoryId = "biryani",
                    priceInr = 389,
                    originalPriceInr = 449,
                    isVeg = false,
                    isBestseller = true,
                    spiceLevel = "Medium Spicy",
                    rating = 4.7,
                    ratingCount = 1840,
                    shortDescription = "Slow-cooked tender mutton & aged basmati rice sealed in clay handi.",
                    description = "Authentic Purani Dilli style slow-dum biryani layered with saffron milk, browned onions, fresh mint, and succulent goat pieces cooked with whole aromatic spices.",
                    whatYouWillGet = listOf(
                        "1 Clay Handi Mutton Dum Biryani (600g)",
                        "Mint & Coriander Boondi Raita (120ml)",
                        "Rich Mirchi Ka Salan Gravy (100ml)",
                        "Lachha Pyaaz & Lemon Wedges"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 25,
                    imageRes = R.drawable.img_food_biryani
                ),
                Dish(
                    id = "dish_karim_chicken_biryani",
                    restaurantId = "rest_karims",
                    name = "Nizami Chicken Tikka Biryani",
                    categoryId = "biryani",
                    priceInr = 319,
                    originalPriceInr = 369,
                    isVeg = false,
                    isBestseller = true,
                    spiceLevel = "Medium",
                    rating = 4.5,
                    ratingCount = 1320,
                    shortDescription = "Char-grilled chicken tikka folded into fragrant kesar basmati.",
                    description = "Tandoor-roasted boneless chicken tikka simmered in spiced yakhni and layered under long-grain basmati rice for a smoky royal finish.",
                    whatYouWillGet = listOf(
                        "1 Chicken Tikka Biryani Pack (550g)",
                        "Cool Cucumber Raita (120ml)",
                        "Roasted Garlic & Chilli Chutney (50ml)",
                        "Spiced Onion Salad"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 22,
                    imageRes = R.drawable.img_food_biryani
                ),
                Dish(
                    id = "dish_karim_seekh",
                    restaurantId = "rest_karims",
                    name = "Seekh Kebab & Roomali Combo",
                    categoryId = "tandoor",
                    priceInr = 295,
                    isVeg = false,
                    isBestseller = false,
                    spiceLevel = "Medium Spicy",
                    rating = 4.6,
                    ratingCount = 690,
                    shortDescription = "Juicy charcoal-grilled seekh kebabs paired with soft roomali rotis.",
                    description = "Hand-minced spiced seekh skewers grilled over live charcoal, served hot with paper-thin roomali rotis and signature green chutney.",
                    whatYouWillGet = listOf(
                        "4 Charcoal Seekh Kebab Skewers",
                        "2 Soft Roomali Rotis",
                        "Dhaniya-Pudina Chutney (60ml)",
                        "Masala Onion Rings"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 20,
                    imageRes = R.drawable.img_hero_delhi_ncr
                )
            )
        ),
        Restaurant(
            id = "rest_nazeer_biryani",
            name = "Biryani By Kilometre NCR",
            locality = "Sector 18 & CyberHub",
            subRegion = "Noida, Gurugram & East Delhi",
            deliveryZoneIds = listOf("noida_18", "cyberhub", "indirapuram", "laxmi_nagar", "cp", "hauz_khas"),
            cuisines = listOf("Biryani", "Rolls & Parathas", "Family Combos"),
            rating = 4.4,
            reviewCountText = "8.9K+",
            deliveryTimeMins = 26,
            deliveryFeeInr = 19,
            priceForOneInr = 240,
            isPureVeg = false,
            offerText = "50% OFF up to ₹100",
            isRecommended = true,
            imageRes = R.drawable.img_food_biryani,
            dishes = listOf(
                Dish(
                    id = "dish_bbk_hyderabadi_veg",
                    restaurantId = "rest_nazeer_biryani",
                    name = "Subz Hyderabadi Handi Biryani",
                    categoryId = "biryani",
                    priceInr = 249,
                    originalPriceInr = 299,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Medium Spicy",
                    rating = 4.4,
                    ratingCount = 950,
                    shortDescription = "Fresh paneer, beans, carrots & saffron basmati cooked on dum.",
                    description = "Aromatic vegetarian dum biryani packed with soft malai paneer cubes, roasted cashews, crispy birista onions, and fresh mint.",
                    whatYouWillGet = listOf(
                        "1 Sealed Earthen Handi Subz Biryani (550g)",
                        "1 Burani Garlic Raita (120ml)",
                        "1 Peanut-Sesame Salan (100ml)",
                        "1 Gulab Jamun Sweet Treat"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 22,
                    imageRes = R.drawable.img_food_biryani
                ),
                Dish(
                    id = "dish_bbk_family_bucket",
                    restaurantId = "rest_nazeer_biryani",
                    name = "Royal NCR Chicken Biryani Party Handi",
                    categoryId = "combos",
                    priceInr = 699,
                    originalPriceInr = 849,
                    isVeg = false,
                    isBestseller = true,
                    spiceLevel = "Medium",
                    rating = 4.6,
                    ratingCount = 1120,
                    shortDescription = "Generous family-size dum biryani handi with starters & desserts.",
                    description = "Perfect for weekend house gatherings across Delhi NCR—a full 1.4kg clay handi of chicken dum biryani with kebabs and sweets.",
                    whatYouWillGet = listOf(
                        "1 Jumbo Chicken Dum Biryani Handi (1.4 kg)",
                        "6 Pcs Chicken Malai Tikka",
                        "2 Large Raitas (300ml total) & 2 Salans",
                        "4 Warm Gulab Jamuns"
                    ),
                    serves = "Serves 3–4 people",
                    prepTimeMins = 30,
                    imageRes = R.drawable.img_food_biryani
                ),
                Dish(
                    id = "dish_bbk_paneer_biryani",
                    restaurantId = "rest_nazeer_biryani",
                    name = "Tandoori Paneer Makhani Biryani",
                    categoryId = "biryani",
                    priceInr = 289,
                    isVeg = true,
                    isBestseller = false,
                    spiceLevel = "Mild",
                    rating = 4.5,
                    ratingCount = 540,
                    shortDescription = "Smoky paneer tikka layered with creamy makhani gravy & rice.",
                    description = "Delhi's favourite fusion—charred tandoori paneer tossed in rich tomato-butter makhani masala and sealed with fragrant basmati.",
                    whatYouWillGet = listOf(
                        "1 Paneer Makhani Biryani Box (550g)",
                        "1 Mint Raita (120ml)",
                        "Pickled Red Onions & Lemon"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 20,
                    imageRes = R.drawable.img_food_biryani
                )
            )
        ),
        Restaurant(
            id = "rest_bikaner_heritage",
            name = "Bikaner Heritage Sweets & Thali",
            locality = "Connaught Place & Rajouri",
            subRegion = "All Delhi NCR",
            deliveryZoneIds = listOf("cp", "rajouri", "dwarka", "rohini", "noida_18", "cyberhub", "indirapuram", "chandni_chowk", "hauz_khas", "laxmi_nagar"),
            cuisines = listOf("Thali", "Chaat & Snacks", "Sweets & Lassi"),
            rating = 4.5,
            reviewCountText = "24K+",
            deliveryTimeMins = 24,
            deliveryFeeInr = 0,
            priceForOneInr = 210,
            isPureVeg = true,
            offerText = "FREE Delivery + ₹75 OFF",
            isRecommended = true,
            imageRes = R.drawable.img_food_thali,
            dishes = listOf(
                Dish(
                    id = "dish_bik_maharaja_thali",
                    restaurantId = "rest_bikaner_heritage",
                    name = "Dilli Special Pure Veg Maharaja Thali",
                    categoryId = "thali",
                    priceInr = 329,
                    originalPriceInr = 379,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Mild-Medium",
                    rating = 4.6,
                    ratingCount = 3420,
                    shortDescription = "Complete royal North Indian vegetarian feast with 2 curries, dal, breads & sweet.",
                    description = "Our signature wholesome Indian thali crafted in pure desi ghee with rich Shahi Paneer, creamy Dal Makhani, seasonal Sabzi, Jeera Pulao, crispy Naan, Raita, and hot Gulab Jamun.",
                    whatYouWillGet = listOf(
                        "Shahi Paneer (200ml) & Dal Makhani (200ml)",
                        "Mix Veg Dry Sabzi (150ml) & Jeera Pulao (200g)",
                        "1 Butter Naan + 2 Tandoori Rotis",
                        "Boondi Raita, Papad, Pickle & 2 Gulab Jamuns"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 20,
                    imageRes = R.drawable.img_food_thali
                ),
                Dish(
                    id = "dish_bik_chole_bhature",
                    restaurantId = "rest_bikaner_heritage",
                    name = "Old Delhi Chole Bhature & Lassi",
                    categoryId = "chaat",
                    priceInr = 179,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Medium Spicy",
                    rating = 4.7,
                    ratingCount = 4150,
                    shortDescription = "Fluffy golden bhaturas with tangy Pindi chole, aam ka achaar & sweet lassi.",
                    description = "Iconic Delhi morning-to-evening comfort plate! Slow-brewed anardana Pindi chole served with two puffed bhature, green chilli pickle, and thick kulhad lassi.",
                    whatYouWillGet = listOf(
                        "2 Fluffy Golden Bhaturas",
                        "Pindi Chole Masala (250ml)",
                        "Spiced Potato Cubes, Achaar & Onion Salad",
                        "1 Chilled Sweet Punjabi Lassi (250ml)"
                    ),
                    serves = "Serves 1 person (Heavy)",
                    prepTimeMins = 15,
                    imageRes = R.drawable.img_food_snacks
                ),
                Dish(
                    id = "dish_bik_raj_kachori",
                    restaurantId = "rest_bikaner_heritage",
                    name = "Shahi Raj Kachori & Papdi Chaat",
                    categoryId = "chaat",
                    priceInr = 155,
                    isVeg = true,
                    isBestseller = false,
                    spiceLevel = "Sweet & Tangy",
                    rating = 4.5,
                    ratingCount = 1280,
                    shortDescription = "Crispy giant kachori loaded with dahi, sprouts, bhalla, pomegranate & sev.",
                    description = "Delivered with chilled sweet curd and tamarind-mint chutneys packed separately so every bite stays crunchy right at your doorstep.",
                    whatYouWillGet = listOf(
                        "1 Crisp Raj Kachori Shell with Bhalla & Sprouts",
                        "Chilled Sweet Dahi Container (150ml)",
                        "Imli & Pudina Chutneys (2 tubs)",
                        "Fine Nylon Sev & Pomegranate Garnish"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 12,
                    imageRes = R.drawable.img_food_snacks
                ),
                Dish(
                    id = "dish_bik_kesar_jalebi",
                    restaurantId = "rest_bikaner_heritage",
                    name = "Desi Ghee Kesar Jalebi & Rabri",
                    categoryId = "sweets",
                    priceInr = 185,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Sweet",
                    rating = 4.8,
                    ratingCount = 2190,
                    shortDescription = "Hot crispy saffron jalebis paired with thick malai lachedar rabri.",
                    description = "Golden spirals fried in pure cow ghee and soaked in saffron-cardamom syrup, accompanied by chilled creamy rabri.",
                    whatYouWillGet = listOf(
                        "250g Hot Desi Ghee Kesar Jalebi",
                        "150g Chilled Lachedar Rabri Cup"
                    ),
                    serves = "Serves 2–3 people",
                    prepTimeMins = 15,
                    imageRes = R.drawable.img_food_thali
                )
            )
        ),
        Restaurant(
            id = "rest_punjabi_dhaba",
            name = "Dilli Darbar Punjabi Dhaba",
            locality = "Rajouri Garden & Dwarka",
            subRegion = "West & South West Delhi",
            deliveryZoneIds = listOf("rajouri", "dwarka", "rohini", "cp", "hauz_khas"),
            cuisines = listOf("Paneer & Curries", "Thali", "Tandoor & Kebabs"),
            rating = 4.4,
            reviewCountText = "6.7K+",
            deliveryTimeMins = 32,
            deliveryFeeInr = 20,
            priceForOneInr = 230,
            isPureVeg = false,
            offerText = "Flat ₹100 OFF above ₹399",
            isRecommended = true,
            imageRes = R.drawable.img_hero_delhi_ncr,
            dishes = listOf(
                Dish(
                    id = "dish_dhaba_butter_chicken",
                    restaurantId = "rest_punjabi_dhaba",
                    name = "Purani Dilli Butter Chicken & Garlic Naan",
                    categoryId = "tandoor",
                    priceInr = 359,
                    originalPriceInr = 410,
                    isVeg = false,
                    isBestseller = true,
                    spiceLevel = "Mild & Creamy",
                    rating = 4.7,
                    ratingCount = 2640,
                    shortDescription = "Char-grilled tandoori chicken in velvety tomato-butter makhani gravy.",
                    description = "Classic Delhi butter chicken finished with fenugreek (kasuri methi) and white butter, paired with two flaky butter-garlic naans.",
                    whatYouWillGet = listOf(
                        "Boneless Butter Chicken Curry (350ml)",
                        "2 Crispy Butter Garlic Naans",
                        "Sirka Pyaaz (Vinegar Onions) & Mint Chutney"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 25,
                    imageRes = R.drawable.img_hero_delhi_ncr
                ),
                Dish(
                    id = "dish_dhaba_kadhai_paneer",
                    restaurantId = "rest_punjabi_dhaba",
                    name = "Kadhai Paneer & Dal Makhani Combo",
                    categoryId = "paneer",
                    priceInr = 279,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Medium Spicy",
                    rating = 4.5,
                    ratingCount = 1490,
                    shortDescription = "Wok-tossed cottage cheese with bell peppers, slow-simmered black dal & laccha paratha.",
                    description = "Fresh malai paneer cooked with freshly pounded coriander seeds and capsicum alongside 18-hour slow-cooked dhaba dal makhani.",
                    whatYouWillGet = listOf(
                        "Kadhai Paneer (250ml)",
                        "Slow-Cooked Dal Makhani (200ml)",
                        "2 Crispy Lachha Parathas",
                        "Jeera Rice Bowl (150g) & Salad"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 24,
                    imageRes = R.drawable.img_food_thali
                ),
                Dish(
                    id = "dish_dhaba_paratha_platter",
                    restaurantId = "rest_punjabi_dhaba",
                    name = "Amritsari Aloo Kulcha & Chole Platter",
                    categoryId = "rolls",
                    priceInr = 199,
                    isVeg = true,
                    isBestseller = false,
                    spiceLevel = "Medium",
                    rating = 4.4,
                    ratingCount = 720,
                    shortDescription = "2 stuffed tandoori kulchas crushed with Amul butter, chole & tamarind chutney.",
                    description = "Crisp, flaky tandoor-baked potato and paneer kulchas topped generously with butter and served with spicy Punjabi chole.",
                    whatYouWillGet = listOf(
                        "2 Buttered Amritsari Aloo-Paneer Kulchas",
                        "Punjabi Chole (220ml)",
                        "Imli-Onion Chutney & Boondi Raita (100ml)"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 18,
                    imageRes = R.drawable.img_hero_delhi_ncr
                )
            )
        ),
        Restaurant(
            id = "rest_momos_wok",
            name = "Chalte Firte Momos & Wok",
            locality = "Hauz Khas & Indirapuram",
            subRegion = "South Delhi, Noida & Ghaziabad",
            deliveryZoneIds = listOf("hauz_khas", "noida_18", "indirapuram", "laxmi_nagar", "cyberhub", "cp"),
            cuisines = listOf("Momos & Chinese", "Rolls & Parathas"),
            rating = 4.3,
            reviewCountText = "9.3K+",
            deliveryTimeMins = 22,
            deliveryFeeInr = 15,
            priceForOneInr = 175,
            isPureVeg = false,
            offerText = "Buy 1 Get 1 on Steamed Momos",
            isRecommended = true,
            imageRes = R.drawable.img_food_snacks,
            dishes = listOf(
                Dish(
                    id = "dish_momos_tandoori_paneer",
                    restaurantId = "rest_momos_wok",
                    name = "Dilli Tandoori Malai Paneer Momos (8 Pcs)",
                    categoryId = "momos",
                    priceInr = 199,
                    originalPriceInr = 239,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Medium Spicy",
                    rating = 4.6,
                    ratingCount = 2980,
                    shortDescription = "Char-grilled paneer dumplings tossed in creamy spiced tandoori marinade.",
                    description = "Delhi's favourite street craving elevated! Crisp-edged tandoori momos coated in smoky yoghurt-butter masala with spicy red chilli garlic dip.",
                    whatYouWillGet = listOf(
                        "8 Jumbo Tandoori Paneer Momos",
                        "Fiery Tibetan Red Chilli Chutney (60ml)",
                        "Mint Mayo Dip (50ml) & Shredded Cabbage"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 18,
                    imageRes = R.drawable.img_food_snacks
                ),
                Dish(
                    id = "dish_momos_chicken_kurkure",
                    restaurantId = "rest_momos_wok",
                    name = "Kurkure Chicken Momos & Chilli Noodle Bowl",
                    categoryId = "momos",
                    priceInr = 259,
                    isVeg = false,
                    isBestseller = true,
                    spiceLevel = "Spicy",
                    rating = 4.5,
                    ratingCount = 1610,
                    shortDescription = "Crunchy golden-fried chicken momos paired with street-style chilli garlic noodles.",
                    description = "Juicy minced chicken momos coated in crispy cornflake crust served alongside wok-tossed Delhi street-style veg hakka noodles.",
                    whatYouWillGet = listOf(
                        "6 Kurkure Fried Chicken Momos",
                        "1 Half-Box Chilli Garlic Noodles (300g)",
                        "Spicy Momo Red Chutney & Garlic Mayo"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 20,
                    imageRes = R.drawable.img_food_snacks
                )
            )
        ),
        Restaurant(
            id = "rest_south_spice",
            name = "Sagar Ratna & South Spice",
            locality = "Connaught Place & Gurugram",
            subRegion = "Central, South Delhi, Gurugram & Noida",
            deliveryZoneIds = listOf("cp", "hauz_khas", "cyberhub", "noida_18", "dwarka", "rajouri"),
            cuisines = listOf("South Indian", "Thali"),
            rating = 4.6,
            reviewCountText = "15.1K+",
            deliveryTimeMins = 23,
            deliveryFeeInr = 15,
            priceForOneInr = 190,
            isPureVeg = true,
            offerText = "30% OFF up to ₹90",
            isRecommended = false,
            imageRes = R.drawable.img_food_snacks,
            dishes = listOf(
                Dish(
                    id = "dish_south_ghee_dosa",
                    restaurantId = "rest_south_spice",
                    name = "Ghee Roast Masala Dosa & Vada Combo",
                    categoryId = "south_indian",
                    priceInr = 219,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Mild",
                    rating = 4.7,
                    ratingCount = 2400,
                    shortDescription = "Crisp golden ghee-roasted masala dosa with 1 medu vada, hot sambar & 3 chutneys.",
                    description = "Fermented rice-lentil batter roasted in pure ghee with spiced potato bhaji, packed in foil-lined leakproof boxes with piping hot drumstick sambar.",
                    whatYouWillGet = listOf(
                        "1 Crisp Ghee Roast Masala Dosa",
                        "1 Crispy Medu Vada",
                        "Hot Drumstick & Dal Sambar (250ml)",
                        "Coconut, Tomato & Mint Chutneys (3 cups)"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 16,
                    imageRes = R.drawable.img_food_snacks
                ),
                Dish(
                    id = "dish_south_thali",
                    restaurantId = "rest_south_spice",
                    name = "Dakshin Special Pure Veg Thali",
                    categoryId = "thali",
                    priceInr = 289,
                    isVeg = true,
                    isBestseller = false,
                    spiceLevel = "Mild-Medium",
                    rating = 4.5,
                    ratingCount = 870,
                    shortDescription = "Wholesome South Indian meal with 2 malabar parottas, curd rice, sambar, rasam & payasam.",
                    description = "Light, comforting South Indian lunch & dinner platter packed with traditional kootu, poriyal, tangy tomato rasam, and sweet semiya payasam.",
                    whatYouWillGet = listOf(
                        "2 Flaky Malabar Parottas + Steamed Rice",
                        "Sambar, Tomato Rasam & Vegetable Korma",
                        "Creamy Curd Rice & Crisp Appalam",
                        "Warm Semiya Payasam Cup (100ml)"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 20,
                    imageRes = R.drawable.img_food_thali
                )
            )
        ),
        Restaurant(
            id = "rest_khan_rolls",
            name = "Khan Chacha Kathi & Biryani",
            locality = "Khan Market & CyberHub",
            subRegion = "Central, South Delhi & Gurugram",
            deliveryZoneIds = listOf("cp", "hauz_khas", "cyberhub", "noida_18", "rohini"),
            cuisines = listOf("Rolls & Parathas", "Biryani", "Tandoor & Kebabs"),
            rating = 4.5,
            reviewCountText = "11.2K+",
            deliveryTimeMins = 25,
            deliveryFeeInr = 20,
            priceForOneInr = 260,
            isPureVeg = false,
            offerText = "₹75 OFF above ₹299",
            isRecommended = true,
            imageRes = R.drawable.img_hero_delhi_ncr,
            dishes = listOf(
                Dish(
                    id = "dish_khan_mutton_roll",
                    restaurantId = "rest_khan_rolls",
                    name = "Double Mutton Kakori Kathi Roll",
                    categoryId = "rolls",
                    priceInr = 299,
                    isVeg = false,
                    isBestseller = true,
                    spiceLevel = "Medium Spicy",
                    rating = 4.6,
                    ratingCount = 1920,
                    shortDescription = "Melt-in-mouth spiced mutton kakori wrapped in flaky egg-laced paratha.",
                    description = "Legendary Delhi kathi roll stuffed with juicy smoky kakori seekh, pickled onions, chaat masala, and house mint chutney.",
                    whatYouWillGet = listOf(
                        "1 Jumbo Double-Stuffed Kakori Roll (280g)",
                        "Extra Pudina Green Chutney (50ml)",
                        "Tangy Masala Onion Salad"
                    ),
                    serves = "Serves 1 person (Filling)",
                    prepTimeMins = 18,
                    imageRes = R.drawable.img_hero_delhi_ncr
                ),
                Dish(
                    id = "dish_khan_lucknowi_biryani",
                    restaurantId = "rest_khan_rolls",
                    name = "Awadhi Zafrani Chicken Biryani",
                    categoryId = "biryani",
                    priceInr = 339,
                    isVeg = false,
                    isBestseller = true,
                    spiceLevel = "Mild Aromatic",
                    rating = 4.5,
                    ratingCount = 1090,
                    shortDescription = "Subtly spiced Lucknowi yakhni chicken biryani infused with kewra & saffron.",
                    description = "Delicately flavoured Awadhi dum biryani where tender chicken on the bone and aged basmati rice are slow-steamed with rose water and saffron.",
                    whatYouWillGet = listOf(
                        "1 Awadhi Chicken Biryani Bowl (580g)",
                        "1 Roasted Cumin Raita (120ml)",
                        "1 Shahi Korma Gravy Side (80ml)"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 24,
                    imageRes = R.drawable.img_food_biryani
                )
            )
        ),
        Restaurant(
            id = "rest_giani_chaat",
            name = "Old Delhi Chaat & Lassi Junction",
            locality = "Chandni Chowk & Rohini",
            subRegion = "North, West & East Delhi",
            deliveryZoneIds = listOf("chandni_chowk", "rohini", "rajouri", "dwarka", "laxmi_nagar", "indirapuram"),
            cuisines = listOf("Chaat & Snacks", "Sweets & Lassi", "Paneer & Curries"),
            rating = 4.3,
            reviewCountText = "7.4K+",
            deliveryTimeMins = 21,
            deliveryFeeInr = 10,
            priceForOneInr = 150,
            isPureVeg = true,
            offerText = "Flat 25% OFF on Chaat",
            isRecommended = false,
            imageRes = R.drawable.img_food_snacks,
            dishes = listOf(
                Dish(
                    id = "dish_giani_aloo_tikki",
                    restaurantId = "rest_giani_chaat",
                    name = "Desi Ghee Stuffed Aloo Tikki & Golgappe Pack",
                    categoryId = "chaat",
                    priceInr = 169,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Tangy & Spicy",
                    rating = 4.5,
                    ratingCount = 1760,
                    shortDescription = "2 crisp chana-stuffed desi ghee tikkis + 8 atta golgappas with teekha-meetha pani.",
                    description = "Hygiene-packed Delhi street chaat combo with crisp suji & atta puris, chilled spiced mint water, sweet tamarind water, and hot crispy tikkis.",
                    whatYouWillGet = listOf(
                        "2 Crispy Stuffed Desi Ghee Aloo Tikkis",
                        "8 Golgappa Puris + Masala Aloo Filling",
                        "Teekha Pudina Pani (200ml) & Meetha Pani (100ml)",
                        "Dahi & Chutney Cups for Tikki"
                    ),
                    serves = "Serves 2 people",
                    prepTimeMins = 14,
                    imageRes = R.drawable.img_food_snacks
                ),
                Dish(
                    id = "dish_giani_veg_biryani_thali",
                    restaurantId = "rest_giani_chaat",
                    name = "Soya Chaap Dum Biryani & Raita",
                    categoryId = "biryani",
                    priceInr = 229,
                    isVeg = true,
                    isBestseller = false,
                    spiceLevel = "Medium Spicy",
                    rating = 4.4,
                    ratingCount = 810,
                    shortDescription = "Char-grilled masala soya chaap layered with saffron basmati & roasted garlic.",
                    description = "High-protein vegetarian Delhi favourite—juicy tandoori soya chaap marinated in rich spices and dum-cooked with basmati rice.",
                    whatYouWillGet = listOf(
                        "1 Soya Chaap Dum Biryani Pack (520g)",
                        "1 Mint Boondi Raita (120ml)",
                        "Roasted Onion & Green Chutney"
                    ),
                    serves = "Serves 1–2 people",
                    prepTimeMins = 20,
                    imageRes = R.drawable.img_food_biryani
                ),
                Dish(
                    id = "dish_giani_mango_lassi",
                    restaurantId = "rest_giani_chaat",
                    name = "Kulhad Malai Lassi & Rabri Falooda",
                    categoryId = "sweets",
                    priceInr = 149,
                    isVeg = true,
                    isBestseller = true,
                    spiceLevel = "Sweet",
                    rating = 4.6,
                    ratingCount = 1340,
                    shortDescription = "Thick churned malai lassi topped with pistachios & rose-scented rabri falooda.",
                    description = "Creamy Old Delhi style dessert-beverage duo crafted with fresh curd, saffron strands, basil seeds, and slow-thickened rabri.",
                    whatYouWillGet = listOf(
                        "1 Sealed Malai Kesar Lassi (300ml)",
                        "1 Royal Rabri Falooda Cup (250ml)",
                        "Pistachio & Almond Slivers Pack"
                    ),
                    serves = "Serves 2 people",
                    prepTimeMins = 10,
                    imageRes = R.drawable.img_food_thali
                )
            )
        )
    )
}
