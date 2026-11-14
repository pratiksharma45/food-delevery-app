package com.example.data

import androidx.annotation.DrawableRes
import com.example.R
import com.example.model.CartItem
import com.example.model.Dish
import com.example.model.PlacedOrder
import com.example.model.Restaurant
import com.example.model.SellerProfile
import com.example.model.UserAddress
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DilliBiteFirebaseRepository(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth
) {

    fun currentUserId(): String? = auth.currentUser?.uid

    fun currentUserEmail(): String? = auth.currentUser?.email

    fun observeRestaurants(): Flow<List<Restaurant>> = flow {
        val path = "restaurants"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc -> doc.toRestaurantOrNull(includeInactive = false) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path, auth)
                    emit(emptyList())
                }
        )
    }

    fun observeAllRestaurantsForCatalog(): Flow<List<Restaurant>> = flow {
        val path = "restaurants"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc -> doc.toRestaurantOrNull(includeInactive = true) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path, auth)
                    emit(emptyList())
                }
        )
    }

    fun observeDishes(): Flow<List<Dish>> = flow {
        val path = "dishes"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc -> doc.toDishOrNull(includeUnavailable = false) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, path, auth)
                    emit(emptyList())
                }
        )
    }

    fun observeCustomerOrders(customerUid: String): Flow<List<PlacedOrder>> = flow {
        val path = "orders"
        emitAll(
            db.collection(path)
                .whereEqualTo("customerUid", customerUid)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents
                        .mapNotNull { doc -> doc.toPlacedOrderOrNull() }
                        .sortedByDescending { it.placedAtText }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == customerUid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path, auth)
                    }
                    emit(emptyList())
                }
        )
    }

    // --- Seller Real-Time Observers ---

    fun observeSellerRestaurant(restaurantId: String): Flow<Restaurant?> = flow {
        val path = "restaurants/$restaurantId"
        emitAll(
            db.collection("restaurants").document(restaurantId)
                .snapshots()
                .map { snapshot -> snapshot.toRestaurantOrNull(includeInactive = true) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    handleFirestoreError(firestoreError ?: error, OperationType.GET, path, auth)
                    emit(null)
                }
        )
    }

    fun observeSellerDishes(sellerUid: String): Flow<List<Dish>> = flow {
        val path = "dishes"
        emitAll(
            db.collection(path)
                .whereEqualTo("sellerUid", sellerUid)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc -> doc.toDishOrNull(includeUnavailable = true) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == sellerUid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path, auth)
                    }
                    emit(emptyList())
                }
        )
    }

    fun observeSellerOrders(sellerUid: String): Flow<List<PlacedOrder>> = flow {
        val path = "orders"
        emitAll(
            db.collection(path)
                .whereEqualTo("sellerUid", sellerUid)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents
                        .mapNotNull { doc -> doc.toPlacedOrderOrNull() }
                        .sortedByDescending { it.placedAtText }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == sellerUid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path, auth)
                    }
                    emit(emptyList())
                }
        )
    }

    // --- Customer Auth & Profile ---

    suspend fun signInCustomer(email: String, password: String): Result<UserProfile> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = result.user?.uid ?: return Result.failure(IllegalStateException("Auth returned empty UID"))
            val profile = fetchCustomerProfile(uid).getOrNull() ?: UserProfile(
                uid = uid,
                fullName = result.user?.displayName?.ifBlank { email.substringBefore("@") } ?: email.substringBefore("@"),
                email = result.user?.email ?: email.trim(),
                phone = "",
                address = UserAddress()
            )
            Result.success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpCustomer(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        address: UserAddress
    ): Result<UserProfile> {
        return try {
            val cleanEmail = email.trim().lowercase()
            val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val uid = result.user?.uid ?: return Result.failure(IllegalStateException("Registration failed"))
            val savedAddress = address.copy(fullName = fullName.trim(), phone = phone.trim())
            val path = "customers/$uid"
            val payload = mapOf(
                "uid" to uid,
                "fullName" to fullName.trim(),
                "email" to cleanEmail,
                "phone" to phone.trim(),
                "address" to savedAddress.toFirestoreMap(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                db.collection("customers").document(uid).set(payload).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, path, auth)
                return Result.failure(e)
            }
            Result.success(
                UserProfile(
                    uid = uid,
                    fullName = fullName.trim(),
                    email = cleanEmail,
                    phone = phone.trim(),
                    address = savedAddress
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCustomerProfile(uid: String): Result<UserProfile?> {
        val path = "customers/$uid"
        return try {
            val snap = db.collection("customers").document(uid).get().await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(snap.toCustomerProfileOrNull())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateCustomerAddress(profile: UserProfile, newAddress: UserAddress): Result<UserProfile> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "customers/$uid"
        val docRef = db.collection("customers").document(uid)
        val mergedAddress = newAddress.copy(
            fullName = newAddress.fullName.ifBlank { profile.fullName },
            phone = newAddress.phone.ifBlank { profile.phone }
        )
        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "address" to mergedAddress.toFirestoreMap(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "uid" to uid,
                        "fullName" to profile.fullName,
                        "email" to profile.email,
                        "phone" to profile.phone.ifBlank { mergedAddress.phone },
                        "address" to mergedAddress.toFirestoreMap(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(profile.copy(address = mergedAddress))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun createOrderInFirestore(
        restaurantId: String,
        sellerUid: String,
        restaurantName: String,
        items: List<CartItem>,
        itemTotalInr: Int,
        deliveryFeeInr: Int,
        gstAndPackagingInr: Int,
        grandTotalInr: Int,
        address: UserAddress,
        etaText: String
    ): Result<PlacedOrder> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Please sign in before placing an order."))
        val orderId = "DB_NCR_${UUID.randomUUID().toString().replace("-", "").take(8).uppercase()}"
        val path = "orders/$orderId"

        val itemMaps = items.map { cartItem ->
            mapOf(
                "dishId" to cartItem.dish.id,
                "dishName" to cartItem.dish.name,
                "quantity" to cartItem.quantity,
                "priceInr" to cartItem.dish.priceInr,
                "isVeg" to cartItem.dish.isVeg
            )
        }

        val payload = mapOf(
            "customerUid" to uid,
            "restaurantId" to restaurantId,
            "sellerUid" to sellerUid,
            "restaurantName" to restaurantName,
            "items" to itemMaps,
            "itemTotalInr" to itemTotalInr,
            "deliveryFeeInr" to deliveryFeeInr,
            "gstAndPackagingInr" to gstAndPackagingInr,
            "grandTotalInr" to grandTotalInr,
            "deliveryAddress" to address.toFirestoreMap(),
            "paymentMethod" to "Cash on Delivery (COD)",
            "status" to "PLACED",
            "etaText" to etaText,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            db.collection("orders").document(orderId).set(payload).await()
            val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            Result.success(
                PlacedOrder(
                    orderId = orderId,
                    restaurantId = restaurantId,
                    restaurantName = restaurantName,
                    items = items,
                    itemTotalInr = itemTotalInr,
                    deliveryFeeInr = deliveryFeeInr,
                    gstAndPackagingInr = gstAndPackagingInr,
                    grandTotalInr = grandTotalInr,
                    deliveryAddress = address,
                    paymentMethod = "Cash on Delivery (COD)",
                    status = "PLACED",
                    etaText = etaText,
                    placedAtText = timeFormat.format(Date()),
                    isSyncedToCloud = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path, auth)
            Result.failure(e)
        }
    }

    // --- Seller Auth, Onboarding & Dashboard CRUD ---

    suspend fun fetchSellerProfile(uid: String): Result<SellerProfile?> {
        val path = "sellers/$uid"
        return try {
            val snap = db.collection("sellers").document(uid).get().await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(snap.toSellerProfileOrNull())
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path, auth)
            Result.failure(e)
        }
    }

    suspend fun signInSeller(email: String, password: String): Result<SellerProfile> {
        val cleanEmail = email.trim()
        if (cleanEmail.equals(DEMO_SELLER_EMAIL, ignoreCase = true) && password == DEMO_SELLER_PASSWORD) {
            return signInDemoSellerKarims()
        }
        return try {
            val result = auth.signInWithEmailAndPassword(cleanEmail, password).await()
            val uid = result.user?.uid ?: return Result.failure(IllegalStateException("Auth returned empty UID"))
            val existingSeller = fetchSellerProfile(uid).getOrNull()
            if (existingSeller != null) {
                return Result.success(existingSeller)
            }
            if (cleanEmail.equals(DEMO_SELLER_EMAIL, ignoreCase = true)) {
                return ensureDemoKarimSeeded(uid, cleanEmail.lowercase())
            }
            Result.failure(
                IllegalStateException("This account is a customer account. Please use Seller Sign Up or the Demo Seller login.")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpSeller(
        ownerName: String,
        email: String,
        phone: String,
        password: String,
        restaurantName: String,
        locality: String,
        cuisines: List<String>,
        deliveryZoneIds: List<String>,
        deliveryTimeMins: Int,
        priceForOneInr: Int
    ): Result<SellerProfile> {
        return try {
            val cleanEmail = email.trim().lowercase()
            val cleanPhone = phone.filter { it.isDigit() }.ifBlank { "9811001122" }
            val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val uid = result.user?.uid ?: return Result.failure(IllegalStateException("Registration failed"))
            val restId = "rest_${uid.take(12)}"
            val cleanCuisines = cuisines.map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("North Indian") }
            val cleanZones = deliveryZoneIds.ifEmpty { listOf("cp") }

            val restPayload = hashMapOf<String, Any?>(
                "sellerUid" to uid,
                "name" to restaurantName.trim(),
                "locality" to locality.trim(),
                "subRegion" to "Delhi NCR",
                "deliveryZoneIds" to cleanZones,
                "cuisines" to cleanCuisines,
                "rating" to 4.5,
                "reviewCountText" to "New",
                "deliveryTimeMins" to deliveryTimeMins.coerceIn(5, 120),
                "deliveryFeeInr" to 20,
                "priceForOneInr" to priceForOneInr.coerceIn(10, 5000),
                "isPureVeg" to false,
                "offerText" to "20% OFF Welcome Offer",
                "isRecommended" to true,
                "localImageKey" to "img_hero_delhi_ncr",
                "isActive" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                db.collection("restaurants").document(restId).set(restPayload).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "restaurants/$restId", auth)
                return Result.failure(e)
            }

            val sellerPayload = mapOf(
                "uid" to uid,
                "ownerName" to ownerName.trim(),
                "email" to cleanEmail,
                "phone" to cleanPhone,
                "restaurantId" to restId,
                "restaurantName" to restaurantName.trim(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                db.collection("sellers").document(uid).set(sellerPayload).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, "sellers/$uid", auth)
                return Result.failure(e)
            }

            Result.success(
                SellerProfile(
                    uid = uid,
                    ownerName = ownerName.trim(),
                    email = cleanEmail,
                    phone = cleanPhone,
                    restaurantId = restId,
                    restaurantName = restaurantName.trim()
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInDemoSellerKarims(): Result<SellerProfile> {
        val demoEmail = DEMO_SELLER_EMAIL
        val demoPassword = DEMO_SELLER_PASSWORD
        return try {
            val authResult = try {
                auth.signInWithEmailAndPassword(demoEmail, demoPassword).await()
            } catch (e: Exception) {
                if (e is FirebaseAuthInvalidUserException || e is FirebaseAuthInvalidCredentialsException) {
                    auth.createUserWithEmailAndPassword(demoEmail, demoPassword).await()
                } else {
                    throw e
                }
            }
            val uid = authResult.user?.uid ?: return Result.failure(IllegalStateException("Demo auth failed"))
            ensureDemoKarimSeeded(uid, demoEmail)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "sellers", auth)
            Result.failure(e)
        }
    }

    suspend fun ensureDemoKarimSeeded(uid: String, email: String = DEMO_SELLER_EMAIL): Result<SellerProfile> {
        return try {
            val existingSeller = fetchSellerProfile(uid).getOrNull()
            if (existingSeller != null) {
                return Result.success(existingSeller)
            }

            val preferredRest = db.collection("restaurants").document(DEMO_RESTAURANT_ID).get().await()
            val restId = if (!preferredRest.exists() || preferredRest.getString("sellerUid") == uid) {
                DEMO_RESTAURANT_ID
            } else {
                "rest_karims_${uid.take(8)}"
            }

            val targetRestSnap = if (restId == DEMO_RESTAURANT_ID) {
                preferredRest
            } else {
                db.collection("restaurants").document(restId).get().await()
            }

            if (!targetRestSnap.exists()) {
                val restPayload = mapOf(
                    "sellerUid" to uid,
                    "name" to "Karim's Royal Mughlai",
                    "locality" to "Jama Masjid & CP",
                    "subRegion" to "Central & Old Delhi",
                    "deliveryZoneIds" to listOf("cp", "chandni_chowk", "hauz_khas", "laxmi_nagar", "noida_18"),
                    "cuisines" to listOf("Biryani", "Mughlai", "Tandoor & Kebabs"),
                    "rating" to 4.6,
                    "reviewCountText" to "12.4K+",
                    "deliveryTimeMins" to 28,
                    "deliveryFeeInr" to 25,
                    "priceForOneInr" to 280,
                    "isPureVeg" to false,
                    "offerText" to "20% OFF up to ₹120",
                    "isRecommended" to true,
                    "localImageKey" to "img_food_biryani",
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("restaurants").document(restId).set(restPayload).await()

                val dishPrefix = if (restId == DEMO_RESTAURANT_ID) "dish_karim" else "dish_karim_${uid.take(6)}"
                val seedDishes = listOf(
                    mapOf(
                        "id" to "${dishPrefix}_mutton_biryani",
                        "name" to "Old Delhi Mutton Dum Biryani",
                        "categoryId" to "biryani",
                        "priceInr" to 389,
                        "originalPriceInr" to 449,
                        "isVeg" to false,
                        "isBestseller" to true,
                        "spiceLevel" to "Medium Spicy",
                        "rating" to 4.7,
                        "ratingCount" to 1840,
                        "shortDescription" to "Slow-cooked tender mutton & aged basmati rice sealed in clay handi.",
                        "description" to "Authentic Purani Dilli style slow-dum biryani layered with saffron milk, browned onions, fresh mint, and succulent goat pieces.",
                        "whatYouWillGet" to listOf(
                            "1 Clay Handi Mutton Dum Biryani (600g)",
                            "Mint & Coriander Boondi Raita (120ml)",
                            "Rich Mirchi Ka Salan Gravy (100ml)",
                            "Lachha Pyaaz & Lemon Wedges"
                        ),
                        "serves" to "Serves 1–2 people",
                        "prepTimeMins" to 25,
                        "localImageKey" to "img_food_biryani"
                    ),
                    mapOf(
                        "id" to "${dishPrefix}_chicken_biryani",
                        "name" to "Nizami Chicken Tikka Biryani",
                        "categoryId" to "biryani",
                        "priceInr" to 319,
                        "originalPriceInr" to 369,
                        "isVeg" to false,
                        "isBestseller" to true,
                        "spiceLevel" to "Medium",
                        "rating" to 4.5,
                        "ratingCount" to 1320,
                        "shortDescription" to "Char-grilled chicken tikka folded into fragrant kesar basmati.",
                        "description" to "Tandoor-roasted boneless chicken tikka simmered in spiced yakhni and layered under long-grain basmati rice.",
                        "whatYouWillGet" to listOf(
                            "1 Chicken Tikka Biryani Pack (550g)",
                            "Cool Cucumber Raita (120ml)",
                            "Roasted Garlic & Chilli Chutney (50ml)"
                        ),
                        "serves" to "Serves 1–2 people",
                        "prepTimeMins" to 22,
                        "localImageKey" to "img_food_biryani"
                    ),
                    mapOf(
                        "id" to "${dishPrefix}_seekh",
                        "name" to "Seekh Kebab & Roomali Combo",
                        "categoryId" to "tandoor",
                        "priceInr" to 295,
                        "isVeg" to false,
                        "isBestseller" to false,
                        "spiceLevel" to "Medium Spicy",
                        "rating" to 4.6,
                        "ratingCount" to 690,
                        "shortDescription" to "Juicy charcoal-grilled seekh kebabs paired with soft roomali rotis.",
                        "description" to "Hand-minced spiced seekh skewers grilled over live charcoal, served hot with paper-thin roomali rotis and green chutney.",
                        "whatYouWillGet" to listOf(
                            "4 Charcoal Seekh Kebab Skewers",
                            "2 Soft Roomali Rotis",
                            "Dhaniya-Pudina Chutney (60ml)"
                        ),
                        "serves" to "Serves 1–2 people",
                        "prepTimeMins" to 20,
                        "localImageKey" to "img_hero_delhi_ncr"
                    )
                )

                for (seed in seedDishes) {
                    val dishId = seed["id"] as String
                    val dishDoc = hashMapOf<String, Any?>(
                        "restaurantId" to restId,
                        "sellerUid" to uid,
                        "name" to seed["name"],
                        "categoryId" to seed["categoryId"],
                        "priceInr" to seed["priceInr"],
                        "isVeg" to seed["isVeg"],
                        "isBestseller" to seed["isBestseller"],
                        "spiceLevel" to seed["spiceLevel"],
                        "rating" to seed["rating"],
                        "ratingCount" to seed["ratingCount"],
                        "shortDescription" to seed["shortDescription"],
                        "description" to seed["description"],
                        "whatYouWillGet" to seed["whatYouWillGet"],
                        "serves" to seed["serves"],
                        "prepTimeMins" to seed["prepTimeMins"],
                        "localImageKey" to seed["localImageKey"],
                        "isAvailable" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    seed["originalPriceInr"]?.let { dishDoc["originalPriceInr"] = it }
                    db.collection("dishes").document(dishId).set(dishDoc).await()
                }
            }

            val sellerDoc = mapOf(
                "uid" to uid,
                "ownerName" to "Karim Demo Partner",
                "email" to email.trim().lowercase(),
                "phone" to "9811009900",
                "restaurantId" to restId,
                "restaurantName" to "Karim's Royal Mughlai",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("sellers").document(uid).set(sellerDoc).await()

            Result.success(
                SellerProfile(
                    uid = uid,
                    ownerName = "Karim Demo Partner",
                    email = email.trim().lowercase(),
                    phone = "9811009900",
                    restaurantId = restId,
                    restaurantName = "Karim's Royal Mughlai"
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "sellers/$uid", auth)
            Result.failure(e)
        }
    }

    suspend fun updateSellerRestaurant(restaurant: Restaurant): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "restaurants/${restaurant.id}"
        val cleanCuisines = restaurant.cuisines.map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("North Indian") }
        val cleanZones = restaurant.deliveryZoneIds.ifEmpty { listOf("cp") }
        val updates = hashMapOf<String, Any?>(
            "name" to restaurant.name.trim(),
            "locality" to restaurant.locality.trim(),
            "subRegion" to restaurant.subRegion.trim().ifBlank { "Delhi NCR" },
            "deliveryZoneIds" to cleanZones,
            "cuisines" to cleanCuisines,
            "rating" to restaurant.rating.coerceIn(0.0, 5.0),
            "reviewCountText" to restaurant.reviewCountText.ifBlank { "New" },
            "deliveryTimeMins" to restaurant.deliveryTimeMins.coerceIn(5, 120),
            "deliveryFeeInr" to restaurant.deliveryFeeInr.coerceIn(0, 500),
            "priceForOneInr" to restaurant.priceForOneInr.coerceIn(10, 5000),
            "isPureVeg" to restaurant.isPureVeg,
            "offerText" to restaurant.offerText?.trim()?.takeIf { it.isNotBlank() },
            "isRecommended" to restaurant.isRecommended,
            "imageUrl" to restaurant.imageUrl?.trim()?.takeIf { it.isNotBlank() },
            "isActive" to restaurant.isActive,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            db.collection("restaurants").document(restaurant.id).update(updates).await()
            val sellerUpdates = mapOf(
                "restaurantName" to restaurant.name.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            runCatching { db.collection("sellers").document(uid).update(sellerUpdates).await() }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun saveSellerDish(dish: Dish, restaurantId: String, isNew: Boolean): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val dishId = if (isNew || dish.id.isBlank()) "dish_${System.currentTimeMillis()}" else dish.id
        val path = "dishes/$dishId"
        val cleanItems = dish.whatYouWillGet.map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { listOf("1 Regular Portion") }

        val commonFields = hashMapOf<String, Any?>(
            "name" to dish.name.trim(),
            "categoryId" to dish.categoryId.ifBlank { "biryani" },
            "priceInr" to dish.priceInr.coerceIn(1, 50000),
            "originalPriceInr" to dish.originalPriceInr?.takeIf { it > 0 },
            "isVeg" to dish.isVeg,
            "isBestseller" to dish.isBestseller,
            "spiceLevel" to dish.spiceLevel.trim().ifBlank { "Medium" },
            "rating" to dish.rating.coerceIn(0.0, 5.0),
            "ratingCount" to dish.ratingCount.coerceAtLeast(0),
            "shortDescription" to dish.shortDescription.trim().ifBlank { dish.name.trim() },
            "description" to dish.description.trim().ifBlank { dish.shortDescription.trim().ifBlank { dish.name.trim() } },
            "whatYouWillGet" to cleanItems,
            "serves" to dish.serves.trim().ifBlank { "Serves 1 person" },
            "prepTimeMins" to dish.prepTimeMins.coerceIn(1, 180),
            "imageUrl" to dish.imageUrl?.trim()?.takeIf { it.isNotBlank() },
            "isAvailable" to dish.isAvailable,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            if (isNew) {
                commonFields["restaurantId"] = restaurantId
                commonFields["sellerUid"] = uid
                commonFields["localImageKey"] = "img_food_biryani"
                commonFields["createdAt"] = FieldValue.serverTimestamp()
                db.collection("dishes").document(dishId).set(commonFields).await()
            } else {
                db.collection("dishes").document(dishId).update(commonFields).await()
            }
            Result.success(dishId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun deleteSellerDish(dishId: String): Result<Unit> {
        val path = "dishes/$dishId"
        return try {
            db.collection("dishes").document(dishId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path, auth)
            Result.failure(e)
        }
    }

    suspend fun updateSellerOrderStatus(orderId: String, newStatus: String, currentEta: String): Result<Unit> {
        val path = "orders/$orderId"
        val updatedEta = if (newStatus == "OUT_FOR_DELIVERY") "Arriving in 10 mins" else currentEta
        return try {
            db.collection("orders").document(orderId).update(
                mapOf(
                    "status" to newStatus,
                    "etaText" to updatedEta,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path, auth)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun UserAddress.toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "fullName" to fullName.trim().ifBlank { "Customer" },
            "phone" to phone.trim().ifBlank { "9999999999" },
            "houseFlat" to houseFlat.trim().ifBlank { "N/A" },
            "streetColony" to streetColony.trim().ifBlank { "N/A" },
            "locality" to locality.trim().ifBlank { "Connaught Place" },
            "pinCode" to pinCode.trim().ifBlank { "110001" }
        )
        if (landmark.isNotBlank()) map["landmark"] = landmark.trim()
        if (instructions.isNotBlank()) map["instructions"] = instructions.trim()
        return map
    }

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toRestaurantOrNull(includeInactive: Boolean): Restaurant? {
        val data = data ?: return null
        val name = data["name"] as? String ?: return null
        val sellerUid = data["sellerUid"] as? String ?: ""
        val locality = data["locality"] as? String ?: "Delhi NCR"
        val subRegion = data["subRegion"] as? String ?: "Delhi NCR"
        val deliveryZoneIds = (data["deliveryZoneIds"] as? List<*>)?.mapNotNull { it as? String }
            ?.ifEmpty { listOf("cp") } ?: listOf("cp")
        val cuisines = (data["cuisines"] as? List<*>)?.mapNotNull { it as? String }
            ?.ifEmpty { listOf("North Indian") } ?: listOf("North Indian")
        val rating = (data["rating"] as? Number)?.toDouble() ?: 4.4
        val reviewCountText = data["reviewCountText"] as? String ?: "New"
        val deliveryTimeMins = (data["deliveryTimeMins"] as? Number)?.toInt() ?: 30
        val deliveryFeeInr = (data["deliveryFeeInr"] as? Number)?.toInt() ?: 25
        val priceForOneInr = (data["priceForOneInr"] as? Number)?.toInt() ?: 200
        val isPureVeg = data["isPureVeg"] as? Boolean ?: false
        val offerText = (data["offerText"] as? String)?.takeIf { it.isNotBlank() }
        val isRecommended = data["isRecommended"] as? Boolean ?: true
        val imageUrl = (data["imageUrl"] as? String)?.takeIf { it.isNotBlank() }
        val localImageKey = data["localImageKey"] as? String
        val isActive = data["isActive"] as? Boolean ?: true
        if (!includeInactive && !isActive) return null

        return Restaurant(
            id = id,
            sellerUid = sellerUid,
            name = name,
            locality = locality,
            subRegion = subRegion,
            deliveryZoneIds = deliveryZoneIds,
            cuisines = cuisines,
            rating = rating,
            reviewCountText = reviewCountText,
            deliveryTimeMins = deliveryTimeMins,
            deliveryFeeInr = deliveryFeeInr,
            priceForOneInr = priceForOneInr,
            isPureVeg = isPureVeg,
            offerText = offerText,
            isRecommended = isRecommended,
            imageRes = resolveDrawableKey(localImageKey, R.drawable.img_hero_delhi_ncr),
            imageUrl = imageUrl,
            isActive = isActive,
            isLiveStore = true,
            dishes = emptyList()
        )
    }

    private fun DocumentSnapshot.toDishOrNull(includeUnavailable: Boolean): Dish? {
        val data = data ?: return null
        val restaurantId = data["restaurantId"] as? String ?: return null
        val name = data["name"] as? String ?: return null
        val isAvailable = data["isAvailable"] as? Boolean ?: true
        if (!includeUnavailable && !isAvailable) return null

        val sellerUid = data["sellerUid"] as? String ?: ""
        val categoryId = data["categoryId"] as? String ?: "biryani"
        val priceInr = (data["priceInr"] as? Number)?.toInt() ?: 199
        val originalPriceInr = (data["originalPriceInr"] as? Number)?.toInt()?.takeIf { it > priceInr }
        val isVeg = data["isVeg"] as? Boolean ?: true
        val isBestseller = data["isBestseller"] as? Boolean ?: false
        val spiceLevel = data["spiceLevel"] as? String ?: "Medium"
        val rating = (data["rating"] as? Number)?.toDouble() ?: 4.5
        val ratingCount = (data["ratingCount"] as? Number)?.toInt() ?: 100
        val shortDescription = data["shortDescription"] as? String ?: ""
        val description = data["description"] as? String ?: shortDescription
        val whatYouWillGet = (data["whatYouWillGet"] as? List<*>)?.mapNotNull { it as? String }
            ?.ifEmpty { listOf("1 Regular Portion") } ?: listOf("1 Regular Portion")
        val serves = data["serves"] as? String ?: "Serves 1 person"
        val prepTimeMins = (data["prepTimeMins"] as? Number)?.toInt() ?: 20
        val imageUrl = (data["imageUrl"] as? String)?.takeIf { it.isNotBlank() }
        val localImageKey = data["localImageKey"] as? String

        return Dish(
            id = id,
            restaurantId = restaurantId,
            sellerUid = sellerUid,
            name = name,
            categoryId = categoryId,
            priceInr = priceInr,
            originalPriceInr = originalPriceInr,
            isVeg = isVeg,
            isBestseller = isBestseller,
            spiceLevel = spiceLevel,
            rating = rating,
            ratingCount = ratingCount,
            shortDescription = shortDescription,
            description = description,
            whatYouWillGet = whatYouWillGet,
            serves = serves,
            prepTimeMins = prepTimeMins,
            imageRes = resolveDrawableKey(localImageKey, R.drawable.img_food_biryani),
            imageUrl = imageUrl,
            isAvailable = isAvailable
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toCustomerProfileOrNull(): UserProfile? {
        val data = data ?: return null
        val uid = data["uid"] as? String ?: id
        val fullName = data["fullName"] as? String ?: ""
        val email = data["email"] as? String ?: ""
        val phone = data["phone"] as? String ?: ""
        val addrMap = data["address"] as? Map<String, Any?> ?: emptyMap()
        val address = UserAddress(
            fullName = addrMap["fullName"] as? String ?: fullName,
            phone = addrMap["phone"] as? String ?: phone,
            houseFlat = addrMap["houseFlat"] as? String ?: "",
            streetColony = addrMap["streetColony"] as? String ?: "",
            locality = addrMap["locality"] as? String ?: "",
            landmark = addrMap["landmark"] as? String ?: "",
            pinCode = addrMap["pinCode"] as? String ?: "",
            instructions = addrMap["instructions"] as? String ?: ""
        )
        return UserProfile(uid = uid, fullName = fullName, email = email, phone = phone, address = address)
    }

    private fun DocumentSnapshot.toSellerProfileOrNull(): SellerProfile? {
        val data = data ?: return null
        val uid = data["uid"] as? String ?: id
        val restaurantId = data["restaurantId"] as? String ?: return null
        return SellerProfile(
            uid = uid,
            ownerName = data["ownerName"] as? String ?: "",
            email = data["email"] as? String ?: "",
            phone = data["phone"] as? String ?: "",
            restaurantId = restaurantId,
            restaurantName = data["restaurantName"] as? String ?: ""
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toPlacedOrderOrNull(): PlacedOrder? {
        val data = data ?: return null
        val restaurantId = data["restaurantId"] as? String ?: ""
        val restaurantName = data["restaurantName"] as? String ?: return null
        val itemMaps = data["items"] as? List<Map<String, Any?>> ?: emptyList()
        val items = itemMaps.map { m ->
            val dishId = m["dishId"] as? String ?: ""
            val dishName = m["dishName"] as? String ?: "Dish"
            val qty = (m["quantity"] as? Number)?.toInt() ?: 1
            val price = (m["priceInr"] as? Number)?.toInt() ?: 0
            val isVeg = m["isVeg"] as? Boolean ?: true
            CartItem(
                dish = Dish(
                    id = dishId,
                    restaurantId = restaurantId,
                    name = dishName,
                    categoryId = "",
                    priceInr = price,
                    isVeg = isVeg,
                    rating = 4.5,
                    ratingCount = 0,
                    shortDescription = "",
                    description = "",
                    whatYouWillGet = emptyList(),
                    serves = "",
                    prepTimeMins = 20
                ),
                restaurantName = restaurantName,
                restaurantLocality = "",
                quantity = qty
            )
        }
        val addrMap = data["deliveryAddress"] as? Map<String, Any?> ?: emptyMap()
        val address = UserAddress(
            fullName = addrMap["fullName"] as? String ?: "",
            phone = addrMap["phone"] as? String ?: "",
            houseFlat = addrMap["houseFlat"] as? String ?: "",
            streetColony = addrMap["streetColony"] as? String ?: "",
            locality = addrMap["locality"] as? String ?: "",
            landmark = addrMap["landmark"] as? String ?: "",
            pinCode = addrMap["pinCode"] as? String ?: "",
            instructions = addrMap["instructions"] as? String ?: ""
        )
        val ts = (data["createdAt"] as? Timestamp)?.toDate()
        val formattedTime = if (ts != null) {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(ts)
        } else {
            "Just now"
        }

        return PlacedOrder(
            orderId = id,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            items = items,
            itemTotalInr = (data["itemTotalInr"] as? Number)?.toInt() ?: 0,
            deliveryFeeInr = (data["deliveryFeeInr"] as? Number)?.toInt() ?: 0,
            gstAndPackagingInr = (data["gstAndPackagingInr"] as? Number)?.toInt() ?: 0,
            grandTotalInr = (data["grandTotalInr"] as? Number)?.toInt() ?: 0,
            deliveryAddress = address,
            paymentMethod = data["paymentMethod"] as? String ?: "Cash on Delivery (COD)",
            status = data["status"] as? String ?: "PLACED",
            etaText = data["etaText"] as? String ?: "25-35 mins",
            placedAtText = formattedTime,
            isSyncedToCloud = true
        )
    }

    @DrawableRes
    private fun resolveDrawableKey(key: String?, @DrawableRes defaultRes: Int): Int {
        return when (key) {
            "img_food_biryani" -> R.drawable.img_food_biryani
            "img_food_thali" -> R.drawable.img_food_thali
            "img_food_snacks" -> R.drawable.img_food_snacks
            "img_hero_delhi_ncr" -> R.drawable.img_hero_delhi_ncr
            else -> defaultRes
        }
    }

    companion object {
        const val DEMO_SELLER_EMAIL = "karim.seller@dillibite.in"
        const val DEMO_SELLER_PASSWORD = "Seller@123"
        const val DEMO_RESTAURANT_ID = "rest_karims"
    }
}

