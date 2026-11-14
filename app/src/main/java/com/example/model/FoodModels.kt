package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

data class DelhiNcrZone(
    val id: String,
    val areaName: String,
    val subRegion: String,
    val defaultPinCode: String
)

data class FoodCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    @DrawableRes val imageRes: Int
)

data class Dish(
    val id: String,
    val restaurantId: String,
    val sellerUid: String = "",
    val name: String,
    val categoryId: String,
    val priceInr: Int,
    val originalPriceInr: Int? = null,
    val isVeg: Boolean,
    val isBestseller: Boolean = false,
    val spiceLevel: String = "Medium",
    val rating: Double,
    val ratingCount: Int,
    val shortDescription: String,
    val description: String,
    val whatYouWillGet: List<String>,
    val serves: String,
    val prepTimeMins: Int,
    @DrawableRes val imageRes: Int = R.drawable.img_food_biryani,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true
)

data class Restaurant(
    val id: String,
    val sellerUid: String = "",
    val name: String,
    val locality: String,
    val subRegion: String,
    val deliveryZoneIds: List<String>,
    val cuisines: List<String>,
    val rating: Double,
    val reviewCountText: String,
    val deliveryTimeMins: Int,
    val deliveryFeeInr: Int,
    val priceForOneInr: Int,
    val isPureVeg: Boolean,
    val offerText: String? = null,
    val isRecommended: Boolean = true,
    @DrawableRes val imageRes: Int = R.drawable.img_hero_delhi_ncr,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val isLiveStore: Boolean = false,
    val dishes: List<Dish>
)

data class CartItem(
    val dish: Dish,
    val restaurantName: String,
    val restaurantLocality: String,
    val quantity: Int
)

data class UserAddress(
    val fullName: String = "",
    val phone: String = "",
    val houseFlat: String = "",
    val streetColony: String = "",
    val locality: String = "",
    val landmark: String = "",
    val pinCode: String = "",
    val instructions: String = ""
)

data class UserProfile(
    val uid: String = "",
    val fullName: String,
    val email: String,
    val phone: String,
    val address: UserAddress
)

data class SellerProfile(
    val uid: String = "",
    val ownerName: String = "",
    val email: String = "",
    val phone: String = "",
    val restaurantId: String = "",
    val restaurantName: String = ""
)

data class PlacedOrder(
    val orderId: String,
    val restaurantId: String = "",
    val restaurantName: String,
    val items: List<CartItem>,
    val itemTotalInr: Int,
    val deliveryFeeInr: Int,
    val gstAndPackagingInr: Int,
    val grandTotalInr: Int,
    val deliveryAddress: UserAddress,
    val paymentMethod: String = "Cash on Delivery (COD)",
    val status: String = "PLACED",
    val etaText: String,
    val placedAtText: String,
    val isSyncedToCloud: Boolean = false
)
