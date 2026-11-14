package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DemoDelhiNcrData
import com.example.data.DilliBiteFirebaseRepository
import com.example.model.CartItem
import com.example.model.DelhiNcrZone
import com.example.model.Dish
import com.example.model.FoodCategory
import com.example.model.PlacedOrder
import com.example.model.Restaurant
import com.example.model.SellerProfile
import com.example.model.UserAddress
import com.example.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

data class DilliBiteUiState(
    val zones: List<DelhiNcrZone> = DemoDelhiNcrData.delhiNcrZones,
    val selectedZone: DelhiNcrZone = DemoDelhiNcrData.delhiNcrZones.first(),
    val categories: List<FoodCategory> = DemoDelhiNcrData.foodCategories,
    val allRestaurants: List<Restaurant> = DemoDelhiNcrData.restaurants,
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val pureVegOnly: Boolean = false,
    val rating45PlusOnly: Boolean = false,
    val under25MinsOnly: Boolean = false,
    val under250Only: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val checkoutItems: List<CartItem> = emptyList(),
    val checkoutFromCart: Boolean = false,
    val userProfile: UserProfile? = null,
    val isAuthLoading: Boolean = false,
    val isOrderSubmitting: Boolean = false,
    val authErrorMessage: String? = null,
    val checkoutErrorMessage: String? = null,
    val recentOrders: List<PlacedOrder> = emptyList(),
    val latestPlacedOrder: PlacedOrder? = null,
    val sellerProfile: SellerProfile? = null,
    val sellerRestaurant: Restaurant? = null,
    val sellerDishes: List<Dish> = emptyList(),
    val sellerOrders: List<PlacedOrder> = emptyList(),
    val isSellerAuthLoading: Boolean = false,
    val isSellerSaving: Boolean = false,
    val sellerStatusMessage: String? = null,
    val sellerErrorMessage: String? = null,
    val shouldOpenSellerPortal: Boolean = false
) {
    val totalCartCount: Int
        get() = cartItems.sumOf { it.quantity }

    val totalCartAmountInr: Int
        get() = cartItems.sumOf { it.dish.priceInr * it.quantity }

    val restaurantsAtLocation: List<Restaurant>
        get() = allRestaurants.filter { restaurant ->
            val deliversHere = selectedZone.id in restaurant.deliveryZoneIds
            val matchesVeg = !pureVegOnly || restaurant.isPureVeg || restaurant.dishes.any { it.isVeg }
            val matchesRating = !rating45PlusOnly || restaurant.rating >= 4.5
            val matchesFast = !under25MinsOnly || restaurant.deliveryTimeMins <= 26
            val matchesPocket = !under250Only || restaurant.dishes.any { it.priceInr <= 250 }
            deliversHere && matchesVeg && matchesRating && matchesFast && matchesPocket
        }

    fun findRestaurant(restaurantId: String): Restaurant? =
        allRestaurants.firstOrNull { it.id == restaurantId }

    fun findDish(dishId: String): Pair<Restaurant, Dish>? {
        for (restaurant in allRestaurants) {
            val dish = restaurant.dishes.firstOrNull { it.id == dishId }
            if (dish != null) return restaurant to dish
        }
        return null
    }
}

class DilliBiteViewModel(
    private val repository: DilliBiteFirebaseRepository = DilliBiteFirebaseRepository(),
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DilliBiteUiState())
    val uiState: StateFlow<DilliBiteUiState> = _uiState.asStateFlow()

    private var ordersJob: Job? = null
    private var sellerRestJob: Job? = null
    private var sellerDishesJob: Job? = null
    private var sellerOrdersJob: Job? = null
    private var isSellerOnboardingInProgress = false

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user != null) {
            loadAuthenticatedCustomer(user.uid, user.email ?: "")
            if (!isSellerOnboardingInProgress) {
                checkAndLoadSeller(user.uid, user.email ?: "")
            }
        } else {
            cancelOrdersAndSellerJobs()
            _uiState.update {
                it.copy(
                    userProfile = null,
                    recentOrders = emptyList(),
                    sellerProfile = null,
                    sellerRestaurant = null,
                    sellerDishes = emptyList(),
                    sellerOrders = emptyList(),
                    shouldOpenSellerPortal = false
                )
            }
        }
    }

    init {
        observeCatalogRealtime()
        firebaseAuth.addAuthStateListener(authListener)
        firebaseAuth.currentUser?.let { user ->
            loadAuthenticatedCustomer(user.uid, user.email ?: "")
            checkAndLoadSeller(user.uid, user.email ?: "")
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authListener)
    }

    private fun cancelOrdersAndSellerJobs() {
        ordersJob?.cancel()
        ordersJob = null
        sellerRestJob?.cancel()
        sellerRestJob = null
        sellerDishesJob?.cancel()
        sellerDishesJob = null
        sellerOrdersJob?.cancel()
        sellerOrdersJob = null
    }

    private fun observeCatalogRealtime() {
        viewModelScope.launch {
            combine(
                repository.observeAllRestaurantsForCatalog(),
                repository.observeDishes()
            ) { liveRestaurants, liveDishes ->
                mergeCatalog(liveRestaurants, liveDishes)
            }.collect { merged ->
                _uiState.update { it.copy(allRestaurants = merged) }
            }
        }
    }

    private fun mergeCatalog(
        liveRestaurants: List<Restaurant>,
        liveDishes: List<Dish>
    ): List<Restaurant> {
        val dishesByRestaurant = liveDishes.groupBy { it.restaurantId }
        val liveIds = liveRestaurants.map { it.id }.toSet()

        val hydratedActiveLive = liveRestaurants
            .filter { it.isActive }
            .map { rest ->
                val cloudDishes = dishesByRestaurant[rest.id].orEmpty()
                val fallbackDishes = DemoDelhiNcrData.restaurants
                    .firstOrNull { it.id == rest.id }?.dishes.orEmpty()
                rest.copy(
                    isLiveStore = true,
                    dishes = if (cloudDishes.isNotEmpty()) cloudDishes else fallbackDishes
                )
            }

        val remainingDemo = DemoDelhiNcrData.restaurants.filter { it.id !in liveIds }
        return hydratedActiveLive + remainingDemo
    }

    private fun loadAuthenticatedCustomer(uid: String, email: String) {
        viewModelScope.launch {
            val profile = repository.fetchCustomerProfile(uid).getOrNull() ?: UserProfile(
                uid = uid,
                fullName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
                phone = "",
                address = UserAddress()
            )
            _uiState.update { it.copy(userProfile = profile) }
        }

        ordersJob?.cancel()
        ordersJob = viewModelScope.launch {
            repository.observeCustomerOrders(uid).collect { cloudOrders ->
                _uiState.update { it.copy(recentOrders = cloudOrders) }
            }
        }
    }

    private fun checkAndLoadSeller(uid: String, email: String = "") {
        viewModelScope.launch {
            val fetched = repository.fetchSellerProfile(uid).getOrNull()
            val seller = fetched ?: if (email.equals(DilliBiteFirebaseRepository.DEMO_SELLER_EMAIL, ignoreCase = true)) {
                repository.ensureDemoKarimSeeded(uid, email).getOrNull()
            } else {
                null
            }
            if (seller != null) {
                attachSellerListeners(seller)
            } else {
                sellerRestJob?.cancel()
                sellerDishesJob?.cancel()
                sellerOrdersJob?.cancel()
                _uiState.update {
                    it.copy(
                        sellerProfile = null,
                        sellerRestaurant = null,
                        sellerDishes = emptyList(),
                        sellerOrders = emptyList()
                    )
                }
            }
        }
    }

    private fun attachSellerListeners(seller: SellerProfile) {
        _uiState.update { it.copy(sellerProfile = seller) }

        sellerRestJob?.cancel()
        sellerRestJob = viewModelScope.launch {
            repository.observeSellerRestaurant(seller.restaurantId).collect { rest ->
                _uiState.update { it.copy(sellerRestaurant = rest) }
            }
        }

        sellerDishesJob?.cancel()
        sellerDishesJob = viewModelScope.launch {
            repository.observeSellerDishes(seller.uid).collect { dishes ->
                _uiState.update { it.copy(sellerDishes = dishes) }
            }
        }

        sellerOrdersJob?.cancel()
        sellerOrdersJob = viewModelScope.launch {
            repository.observeSellerOrders(seller.uid).collect { orders ->
                _uiState.update { it.copy(sellerOrders = orders) }
            }
        }
    }

    fun selectZone(zone: DelhiNcrZone) {
        _uiState.update { it.copy(selectedZone = zone) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun togglePureVeg() {
        _uiState.update { it.copy(pureVegOnly = !it.pureVegOnly) }
    }

    fun toggleRating45Plus() {
        _uiState.update { it.copy(rating45PlusOnly = !it.rating45PlusOnly) }
    }

    fun toggleUnder25Mins() {
        _uiState.update { it.copy(under25MinsOnly = !it.under25MinsOnly) }
    }

    fun toggleUnder250() {
        _uiState.update { it.copy(under250Only = !it.under250Only) }
    }

    fun addToCart(dish: Dish, restaurant: Restaurant, quantity: Int = 1) {
        _uiState.update { current ->
            val existingIndex = current.cartItems.indexOfFirst { it.dish.id == dish.id }
            val updated = if (existingIndex >= 0) {
                current.cartItems.toMutableList().apply {
                    val existing = this[existingIndex]
                    this[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
                }
            } else {
                current.cartItems + CartItem(
                    dish = dish,
                    restaurantName = restaurant.name,
                    restaurantLocality = restaurant.locality,
                    quantity = quantity
                )
            }
            current.copy(cartItems = updated)
        }
    }

    fun updateCartItemQuantity(dishId: String, delta: Int) {
        _uiState.update { current ->
            val updated = current.cartItems.mapNotNull { item ->
                if (item.dish.id == dishId) {
                    val newQty = item.quantity + delta
                    if (newQty <= 0) null else item.copy(quantity = newQty)
                } else {
                    item
                }
            }
            current.copy(cartItems = updated)
        }
    }

    fun startBuyNow(dish: Dish, restaurant: Restaurant, quantity: Int) {
        val item = CartItem(
            dish = dish,
            restaurantName = restaurant.name,
            restaurantLocality = restaurant.locality,
            quantity = quantity
        )
        _uiState.update {
            it.copy(
                checkoutItems = listOf(item),
                checkoutFromCart = false,
                checkoutErrorMessage = null,
                latestPlacedOrder = null
            )
        }
    }

    fun startCheckoutFromCart() {
        _uiState.update {
            it.copy(
                checkoutItems = it.cartItems,
                checkoutFromCart = true,
                checkoutErrorMessage = null,
                latestPlacedOrder = null
            )
        }
    }

    fun placeCodOrder(address: UserAddress, onSuccess: (PlacedOrder) -> Unit) {
        val current = _uiState.value
        val itemsToOrder = current.checkoutItems.ifEmpty { current.cartItems }
        if (itemsToOrder.isEmpty()) return

        // Group items by restaurant so each Firestore order maps cleanly to one restaurant & seller
        val groupedByRestaurant = itemsToOrder.groupBy { it.dish.restaurantId }

        viewModelScope.launch {
            _uiState.update { it.copy(isOrderSubmitting = true, checkoutErrorMessage = null) }

            val currentUid = repository.currentUserId()
            val firstGroupEntry = groupedByRestaurant.entries.first()
            val restId = firstGroupEntry.key
            val restaurant = current.findRestaurant(restId)
            val isLiveRestaurant = restaurant?.isLiveStore == true && restaurant.sellerUid.isNotBlank()

            if (isLiveRestaurant && currentUid == null) {
                _uiState.update {
                    it.copy(
                        isOrderSubmitting = false,
                        checkoutErrorMessage = "Please sign in on the Profile tab first to sync your order live with ${restaurant?.name}."
                    )
                }
                return@launch
            }

            var lastPlaced: PlacedOrder? = null
            val localOnlyOrders = mutableListOf<PlacedOrder>()

            for ((groupRestId, groupItems) in groupedByRestaurant) {
                val groupRest = current.findRestaurant(groupRestId)
                val itemTotal = groupItems.sumOf { it.dish.priceInr * it.quantity }
                val deliveryFee = if (itemTotal >= 499) 0 else (groupRest?.deliveryFeeInr ?: 25)
                val gstAndPkg = (itemTotal * 0.05).roundToInt().coerceAtLeast(12)
                val grandTotal = itemTotal + deliveryFee + gstAndPkg
                val etaText = "${groupRest?.deliveryTimeMins ?: 28} mins to ${address.locality.ifBlank { current.selectedZone.areaName }}"
                val restName = groupRest?.name ?: groupItems.first().restaurantName

                if (groupRest != null && groupRest.isLiveStore && groupRest.sellerUid.isNotBlank() && currentUid != null) {
                    val result = repository.createOrderInFirestore(
                        restaurantId = groupRest.id,
                        sellerUid = groupRest.sellerUid,
                        restaurantName = restName,
                        items = groupItems,
                        itemTotalInr = itemTotal,
                        deliveryFeeInr = deliveryFee,
                        gstAndPackagingInr = gstAndPkg,
                        grandTotalInr = grandTotal,
                        address = address,
                        etaText = etaText
                    )
                    result.fold(
                        onSuccess = { placed -> lastPlaced = placed },
                        onFailure = { err ->
                            _uiState.update {
                                it.copy(
                                    isOrderSubmitting = false,
                                    checkoutErrorMessage = err.localizedMessage ?: "Failed to place order in Firestore."
                                )
                            }
                            return@launch
                        }
                    )
                } else {
                    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    val localOrder = PlacedOrder(
                        orderId = "DB-DEMO-${Random.nextInt(1000, 9999)}",
                        restaurantId = groupRestId,
                        restaurantName = restName,
                        items = groupItems,
                        itemTotalInr = itemTotal,
                        deliveryFeeInr = deliveryFee,
                        gstAndPackagingInr = gstAndPkg,
                        grandTotalInr = grandTotal,
                        deliveryAddress = address,
                        paymentMethod = "Cash on Delivery (COD)",
                        status = "PLACED",
                        etaText = etaText,
                        placedAtText = timeFormat.format(Date()),
                        isSyncedToCloud = false
                    )
                    lastPlaced = localOrder
                    localOnlyOrders.add(localOrder)
                }
            }

            val finalOrder = lastPlaced ?: return@launch
            _uiState.update { state ->
                state.copy(
                    isOrderSubmitting = false,
                    checkoutErrorMessage = null,
                    cartItems = if (state.checkoutFromCart) emptyList() else state.cartItems,
                    checkoutItems = emptyList(),
                    recentOrders = if (localOnlyOrders.isNotEmpty()) {
                        localOnlyOrders + state.recentOrders
                    } else {
                        state.recentOrders
                    },
                    latestPlacedOrder = finalOrder
                )
            }
            onSuccess(finalOrder)
        }
    }

    fun clearLatestOrder() {
        _uiState.update { it.copy(latestPlacedOrder = null) }
    }

    fun clearCheckoutError() {
        _uiState.update { it.copy(checkoutErrorMessage = null) }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            val cleanEmail = email.trim()
            if (cleanEmail.equals(DilliBiteFirebaseRepository.DEMO_SELLER_EMAIL, ignoreCase = true)) {
                isSellerOnboardingInProgress = true
                val sellerResult = if (password == DilliBiteFirebaseRepository.DEMO_SELLER_PASSWORD) {
                    repository.signInDemoSellerKarims()
                } else {
                    repository.signInSeller(cleanEmail, password)
                }
                isSellerOnboardingInProgress = false
                sellerResult.fold(
                    onSuccess = { seller ->
                        attachSellerListeners(seller)
                        _uiState.update {
                            it.copy(
                                isAuthLoading = false,
                                authErrorMessage = null,
                                shouldOpenSellerPortal = true,
                                sellerStatusMessage = "Welcome back, ${seller.restaurantName}!"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isAuthLoading = false,
                                authErrorMessage = error.localizedMessage ?: "Sign in failed. Check your email and password."
                            )
                        }
                    }
                )
                return@launch
            }

            val result = repository.signInCustomer(cleanEmail, password)
            result.fold(
                onSuccess = { profile ->
                    val existingSeller = repository.fetchSellerProfile(profile.uid).getOrNull()
                    if (existingSeller != null) {
                        attachSellerListeners(existingSeller)
                    }
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            userProfile = profile,
                            authErrorMessage = null,
                            shouldOpenSellerPortal = existingSeller != null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = error.localizedMessage ?: "Sign in failed. Check your email and password."
                        )
                    }
                }
            )
        }
    }

    fun consumeSellerPortalRedirect() {
        _uiState.update { it.copy(shouldOpenSellerPortal = false) }
    }

    fun signUp(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        address: UserAddress
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            val result = repository.signUpCustomer(fullName, email, phone, password, address)
            result.fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            userProfile = profile,
                            authErrorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = error.localizedMessage ?: "Sign up failed."
                        )
                    }
                }
            )
        }
    }

    fun updateProfileAddress(address: UserAddress) {
        val currentProfile = _uiState.value.userProfile ?: return
        viewModelScope.launch {
            val result = repository.updateCustomerAddress(currentProfile, address)
            result.onSuccess { updated ->
                _uiState.update { it.copy(userProfile = updated) }
            }
        }
    }

    fun clearAuthError() {
        _uiState.update { it.copy(authErrorMessage = null) }
    }

    fun signOut() {
        repository.signOut()
        cancelOrdersAndSellerJobs()
        _uiState.update {
            it.copy(
                userProfile = null,
                recentOrders = emptyList(),
                authErrorMessage = null,
                sellerProfile = null,
                sellerRestaurant = null,
                sellerDishes = emptyList(),
                sellerOrders = emptyList()
            )
        }
    }

    // --- Seller Portal Actions ---

    fun signInSeller(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSellerAuthLoading = true, sellerErrorMessage = null, sellerStatusMessage = null)
            }
            val result = repository.signInSeller(email, password)
            result.fold(
                onSuccess = { seller ->
                    attachSellerListeners(seller)
                    _uiState.update {
                        it.copy(
                            isSellerAuthLoading = false,
                            sellerErrorMessage = null,
                            sellerStatusMessage = "Welcome back, ${seller.restaurantName}!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSellerAuthLoading = false,
                            sellerErrorMessage = error.localizedMessage ?: "Seller sign in failed."
                        )
                    }
                }
            )
        }
    }

    fun signUpSeller(
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
    ) {
        viewModelScope.launch {
            isSellerOnboardingInProgress = true
            _uiState.update {
                it.copy(isSellerAuthLoading = true, sellerErrorMessage = null, sellerStatusMessage = null)
            }
            val result = repository.signUpSeller(
                ownerName = ownerName,
                email = email,
                phone = phone,
                password = password,
                restaurantName = restaurantName,
                locality = locality,
                cuisines = cuisines,
                deliveryZoneIds = deliveryZoneIds,
                deliveryTimeMins = deliveryTimeMins,
                priceForOneInr = priceForOneInr
            )
            isSellerOnboardingInProgress = false
            result.fold(
                onSuccess = { seller ->
                    attachSellerListeners(seller)
                    _uiState.update {
                        it.copy(
                            isSellerAuthLoading = false,
                            sellerErrorMessage = null,
                            sellerStatusMessage = "${seller.restaurantName} is now live in Firestore!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSellerAuthLoading = false,
                            sellerErrorMessage = error.localizedMessage ?: "Seller registration failed."
                        )
                    }
                }
            )
        }
    }

    fun signInDemoSeller() {
        viewModelScope.launch {
            isSellerOnboardingInProgress = true
            _uiState.update {
                it.copy(isSellerAuthLoading = true, sellerErrorMessage = null, sellerStatusMessage = null)
            }
            val result = repository.signInDemoSellerKarims()
            isSellerOnboardingInProgress = false
            result.fold(
                onSuccess = { seller ->
                    attachSellerListeners(seller)
                    _uiState.update {
                        it.copy(
                            isSellerAuthLoading = false,
                            sellerErrorMessage = null,
                            sellerStatusMessage = "Signed in as Demo Seller: ${seller.restaurantName}"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSellerAuthLoading = false,
                            sellerErrorMessage = error.localizedMessage ?: "Demo seller login failed."
                        )
                    }
                }
            )
        }
    }

    fun saveSellerRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSellerSaving = true, sellerErrorMessage = null, sellerStatusMessage = null) }
            val result = repository.updateSellerRestaurant(restaurant)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSellerSaving = false,
                            sellerStatusMessage = "Restaurant details & image synced with customer storefront!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSellerSaving = false,
                            sellerErrorMessage = error.localizedMessage ?: "Failed to update restaurant."
                        )
                    }
                }
            )
        }
    }

    fun saveSellerDish(dish: Dish, isNew: Boolean, onDone: () -> Unit = {}) {
        val restId = _uiState.value.sellerProfile?.restaurantId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSellerSaving = true, sellerErrorMessage = null, sellerStatusMessage = null) }
            val result = repository.saveSellerDish(dish, restId, isNew)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSellerSaving = false,
                            sellerStatusMessage = if (isNew) {
                                "${dish.name} published to live menu!"
                            } else {
                                "${dish.name} updated & synced!"
                            }
                        )
                    }
                    onDone()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSellerSaving = false,
                            sellerErrorMessage = error.localizedMessage ?: "Failed to save dish."
                        )
                    }
                }
            )
        }
    }

    fun deleteSellerDish(dish: Dish) {
        viewModelScope.launch {
            val result = repository.deleteSellerDish(dish.id)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(sellerStatusMessage = "Deleted ${dish.name} from menu.") }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(sellerErrorMessage = error.localizedMessage ?: "Failed to delete dish.")
                    }
                }
            )
        }
    }

    fun updateSellerOrderStatus(orderId: String, newStatus: String, currentEta: String) {
        viewModelScope.launch {
            val result = repository.updateSellerOrderStatus(orderId, newStatus, currentEta)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(sellerStatusMessage = "Order #$orderId marked as ${newStatus.replace('_', ' ')}.")
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(sellerErrorMessage = error.localizedMessage ?: "Failed to update order status.")
                    }
                }
            )
        }
    }

    fun clearSellerMessages() {
        _uiState.update { it.copy(sellerErrorMessage = null, sellerStatusMessage = null) }
    }
}
