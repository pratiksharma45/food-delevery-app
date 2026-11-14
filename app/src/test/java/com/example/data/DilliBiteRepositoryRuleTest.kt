package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.CartItem
import com.example.model.Dish
import com.example.model.UserAddress
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class DilliBiteRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun customerProfileAndCodOrder_syncWithSellerOwnedRestaurant() = runBlocking {
        // 1. Seller signs in and registers a restaurant
        val sellerUid = signInTestUser("seller_${UUID.randomUUID()}@dillibite.in")
        val restaurantId = "rest_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        withTimeout(TIMEOUT_MS) {
            firestore.collection("restaurants").document(restaurantId).set(
                mapOf(
                    "sellerUid" to sellerUid,
                    "name" to "Karim's Test Kitchen",
                    "locality" to "Connaught Place",
                    "subRegion" to "Central Delhi",
                    "deliveryZoneIds" to listOf("cp"),
                    "cuisines" to listOf("Biryani", "Mughlai"),
                    "rating" to 4.6,
                    "reviewCountText" to "1.2K+",
                    "deliveryTimeMins" to 25,
                    "deliveryFeeInr" to 25,
                    "priceForOneInr" to 280,
                    "isPureVeg" to false,
                    "offerText" to "20% OFF",
                    "isRecommended" to true,
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // 2. Customer signs up and places a COD order
        auth.signOut()
        val repo = DilliBiteFirebaseRepository(firestore, auth)
        val customerEmail = "cust_${UUID.randomUUID()}@delhi.in"
        val address = UserAddress(
            fullName = "Rohan Verma",
            phone = "9811223344",
            houseFlat = "C-12",
            streetColony = "Middle Circle",
            locality = "Connaught Place",
            landmark = "Near Metro Gate 2",
            pinCode = "110001"
        )
        val signUpResult = withTimeout(TIMEOUT_MS) {
            repo.signUpCustomer("Rohan Verma", customerEmail, "9811223344", "Password123!", address)
        }
        assertTrue(signUpResult.isSuccess)
        val customerUid = signUpResult.getOrThrow().uid

        val dish = Dish(
            id = "dish_test_biryani",
            restaurantId = restaurantId,
            sellerUid = sellerUid,
            name = "Old Delhi Mutton Dum Biryani",
            categoryId = "biryani",
            priceInr = 389,
            isVeg = false,
            rating = 4.7,
            ratingCount = 120,
            shortDescription = "Authentic dum biryani",
            description = "Authentic dum biryani",
            whatYouWillGet = listOf("1 Handi Biryani"),
            serves = "Serves 1-2 people",
            prepTimeMins = 25
        )
        val orderResult = withTimeout(TIMEOUT_MS) {
            repo.createOrderInFirestore(
                restaurantId = restaurantId,
                sellerUid = sellerUid,
                restaurantName = "Karim's Test Kitchen",
                items = listOf(CartItem(dish, "Karim's Test Kitchen", "Connaught Place", 1)),
                itemTotalInr = 389,
                deliveryFeeInr = 25,
                gstAndPackagingInr = 19,
                grandTotalInr = 433,
                address = address,
                etaText = "25 mins to Connaught Place"
            )
        }
        assertTrue(orderResult.isSuccess)
        val order = orderResult.getOrThrow()
        assertEquals("PLACED", order.status)

        // 3. Customer observes their orders via real-time Flow
        val observed = withTimeout(TIMEOUT_MS) {
            repo.observeCustomerOrders(customerUid).first { list -> list.any { it.orderId == order.orderId } }
        }
        assertTrue(observed.any { it.orderId == order.orderId })

        // 4. Another customer cannot read Rohan's profile
        auth.signOut()
        signInTestUser("other_${UUID.randomUUID()}@delhi.in")
        var denied = false
        try {
            withTimeout(TIMEOUT_MS) {
                firestore.collection("customers").document(customerUid).get().await()
            }
        } catch (e: FirebaseFirestoreException) {
            denied = e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        }
        assertTrue("Cross-customer profile read must fail with PERMISSION_DENIED", denied)
    }

    @Test
    fun observeRestaurants_returnsLiveFirestoreRestaurant() = runBlocking {
        val sellerUid = signInTestUser("seller_live_${UUID.randomUUID()}@dillibite.in")
        val restaurantId = "rest_live_${UUID.randomUUID().toString().replace("-", "").take(8)}"
        withTimeout(TIMEOUT_MS) {
            firestore.collection("restaurants").document(restaurantId).set(
                mapOf(
                    "sellerUid" to sellerUid,
                    "name" to "Noida Spice Hub",
                    "locality" to "Sector 18",
                    "subRegion" to "Noida",
                    "deliveryZoneIds" to listOf("noida_18"),
                    "cuisines" to listOf("Thali"),
                    "rating" to 4.5,
                    "reviewCountText" to "500+",
                    "deliveryTimeMins" to 22,
                    "deliveryFeeInr" to 0,
                    "priceForOneInr" to 220,
                    "isPureVeg" to true,
                    "isRecommended" to true,
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        val repo = DilliBiteFirebaseRepository(firestore, auth)
        val restaurants = withTimeout(TIMEOUT_MS) {
            repo.observeRestaurants().first { list -> list.any { it.id == restaurantId } }
        }
        val found = restaurants.firstOrNull { it.id == restaurantId }
        assertNotNull(found)
        assertEquals("Noida Spice Hub", found?.name)
    }

    @Test
    fun sellerOnboardingDishCrudAndOrderStatusUpdate_succeedUnderSecurityRules() = runBlocking {
        val repo = DilliBiteFirebaseRepository(firestore, auth)
        val sellerEmail = "seller_e2e_${UUID.randomUUID()}@dillibite.in"

        val signUpRes = withTimeout(TIMEOUT_MS) {
            repo.signUpSeller(
                ownerName = "Vikram Mehra",
                email = sellerEmail,
                phone = "9811445566",
                password = "Password123!",
                restaurantName = "Mehra's Dilli Tadka",
                locality = "Hauz Khas",
                cuisines = listOf("Biryani", "North Indian"),
                deliveryZoneIds = listOf("hauz_khas", "cp"),
                deliveryTimeMins = 26,
                priceForOneInr = 260
            )
        }
        assertTrue("Seller signup should succeed: ${signUpRes.exceptionOrNull()}", signUpRes.isSuccess)
        val seller = signUpRes.getOrThrow()

        // Create a dish under the seller's restaurant
        val newDish = Dish(
            id = "",
            restaurantId = seller.restaurantId,
            sellerUid = seller.uid,
            name = "Hauz Khas Special Paneer Biryani",
            categoryId = "biryani",
            priceInr = 299,
            originalPriceInr = 349,
            isVeg = true,
            isBestseller = true,
            spiceLevel = "Medium Spicy",
            rating = 4.6,
            ratingCount = 120,
            shortDescription = "Charred paneer tikka & kesar basmati.",
            description = "Slow-dum vegetarian biryani with raita and mirchi salan.",
            whatYouWillGet = listOf("1 Biryani Box (550g)", "Mint Raita (120ml)"),
            serves = "Serves 1-2 people",
            prepTimeMins = 22,
            isAvailable = true
        )
        val createDishRes = withTimeout(TIMEOUT_MS) {
            repo.saveSellerDish(newDish, seller.restaurantId, isNew = true)
        }
        assertTrue("Create dish should succeed: ${createDishRes.exceptionOrNull()}", createDishRes.isSuccess)
        val dishId = createDishRes.getOrThrow()

        // Edit the dish price and availability
        val editDishRes = withTimeout(TIMEOUT_MS) {
            repo.saveSellerDish(newDish.copy(id = dishId, priceInr = 315), seller.restaurantId, isNew = false)
        }
        assertTrue("Edit dish should succeed: ${editDishRes.exceptionOrNull()}", editDishRes.isSuccess)

        val sellerDishes = withTimeout(TIMEOUT_MS) {
            repo.observeSellerDishes(seller.uid).first { list -> list.any { it.id == dishId && it.priceInr == 315 } }
        }
        assertEquals(315, sellerDishes.first { it.id == dishId }.priceInr)

        // Update restaurant profile
        val liveRest = withTimeout(TIMEOUT_MS) {
            repo.observeSellerRestaurant(seller.restaurantId).first { it != null }!!
        }
        val restUpdateRes = withTimeout(TIMEOUT_MS) {
            repo.updateSellerRestaurant(liveRest.copy(offerText = "Flat ₹75 OFF", deliveryTimeMins = 22))
        }
        assertTrue("Restaurant update should succeed: ${restUpdateRes.exceptionOrNull()}", restUpdateRes.isSuccess)

        // Customer places COD order on the seller's restaurant
        auth.signOut()
        val custAddress = UserAddress(
            fullName = "Ananya Kapoor",
            phone = "9811998877",
            houseFlat = "B-14",
            streetColony = "Hauz Khas Enclave",
            locality = "Hauz Khas",
            pinCode = "110016"
        )
        val custSignUp = withTimeout(TIMEOUT_MS) {
            repo.signUpCustomer("Ananya Kapoor", "ananya_${UUID.randomUUID()}@delhi.in", "9811998877", "Password123!", custAddress)
        }
        assertTrue(custSignUp.isSuccess)

        val placedOrderRes = withTimeout(TIMEOUT_MS) {
            repo.createOrderInFirestore(
                restaurantId = seller.restaurantId,
                sellerUid = seller.uid,
                restaurantName = liveRest.name,
                items = listOf(CartItem(newDish.copy(id = dishId, priceInr = 315), liveRest.name, liveRest.locality, 1)),
                itemTotalInr = 315,
                deliveryFeeInr = 20,
                gstAndPackagingInr = 16,
                grandTotalInr = 351,
                address = custAddress,
                etaText = "22 mins to Hauz Khas"
            )
        }
        assertTrue("Customer COD order should succeed: ${placedOrderRes.exceptionOrNull()}", placedOrderRes.isSuccess)
        val order = placedOrderRes.getOrThrow()

        // Seller signs back in, sees order, and updates status to PREPARING
        auth.signOut()
        val sellerSignInRes = withTimeout(TIMEOUT_MS) {
            repo.signInSeller(sellerEmail, "Password123!")
        }
        assertTrue("Seller sign-in should succeed", sellerSignInRes.isSuccess)

        val sellerOrders = withTimeout(TIMEOUT_MS) {
            repo.observeSellerOrders(seller.uid).first { list -> list.any { it.orderId == order.orderId } }
        }
        assertTrue(sellerOrders.any { it.orderId == order.orderId })

        val statusUpdateRes = withTimeout(TIMEOUT_MS) {
            repo.updateSellerOrderStatus(order.orderId, "PREPARING", order.etaText)
        }
        assertTrue("Seller order status update should succeed: ${statusUpdateRes.exceptionOrNull()}", statusUpdateRes.isSuccess)
    }

    private companion object {
        const val TIMEOUT_MS = 8000L
    }
}
