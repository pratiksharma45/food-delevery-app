package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.DilliBiteViewModel
import com.example.ui.components.DelhiNcrLocationSheet
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CheckoutCodScreen
import com.example.ui.screens.DishDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrderConfirmationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RestaurantDetailScreen
import com.example.ui.screens.SearchAndCategoryScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.theme.DilliBiteTheme
import kotlinx.coroutines.launch

object DilliBiteRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val SELLER_PORTAL = "seller_portal"
    const val RESTAURANT = "restaurant/{restaurantId}"
    const val DISH = "dish/{restaurantId}/{dishId}"
    const val CHECKOUT_COD = "checkout_cod"
    const val ORDER_CONFIRMED = "order_confirmed"

    fun restaurant(restaurantId: String) = "restaurant/$restaurantId"
    fun dish(restaurantId: String, dishId: String) = "dish/$restaurantId/$dishId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DilliBiteTheme {
                DilliBiteApp()
            }
        }
    }
}

@Composable
fun DilliBiteApp(
    viewModel: DilliBiteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var showLocationSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.shouldOpenSellerPortal) {
        if (uiState.shouldOpenSellerPortal) {
            viewModel.consumeSellerPortalRedirect()
            if (currentRoute != DilliBiteRoutes.SELLER_PORTAL) {
                navController.navigate(DilliBiteRoutes.SELLER_PORTAL) {
                    launchSingleTop = true
                }
            }
        }
    }

    val topLevelRoutes = setOf(
        DilliBiteRoutes.HOME,
        DilliBiteRoutes.SEARCH,
        DilliBiteRoutes.CART,
        DilliBiteRoutes.PROFILE
    )
    val showBottomBar = currentRoute in topLevelRoutes

    val cartQtyMap = remember(uiState.cartItems) {
        uiState.cartItems.associate { it.dish.id to it.quantity }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == DilliBiteRoutes.HOME,
                        onClick = { navigateTopLevel(DilliBiteRoutes.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == DilliBiteRoutes.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentRoute == DilliBiteRoutes.SEARCH,
                        onClick = { navigateTopLevel(DilliBiteRoutes.SEARCH) },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == DilliBiteRoutes.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                contentDescription = "Search"
                            )
                        },
                        label = { Text("Explore") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_search")
                    )

                    NavigationBarItem(
                        selected = currentRoute == DilliBiteRoutes.CART,
                        onClick = { navigateTopLevel(DilliBiteRoutes.CART) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (uiState.totalCartCount > 0) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text(uiState.totalCartCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentRoute == DilliBiteRoutes.CART) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                                    contentDescription = "Cart"
                                )
                            }
                        },
                        label = { Text("Cart") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_cart")
                    )

                    NavigationBarItem(
                        selected = currentRoute == DilliBiteRoutes.PROFILE,
                        onClick = { navigateTopLevel(DilliBiteRoutes.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == DilliBiteRoutes.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "My Profile"
                            )
                        },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            NavHost(
                navController = navController,
                startDestination = DilliBiteRoutes.HOME,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
            ) {
                composable(DilliBiteRoutes.HOME) {
                    HomeScreen(
                        uiState = uiState,
                        onLocationClick = { showLocationSheet = true },
                        onSearchClick = {
                            viewModel.selectCategory(null)
                            navigateTopLevel(DilliBiteRoutes.SEARCH)
                        },
                        onCategoryClick = { category ->
                            viewModel.updateSearchQuery("")
                            viewModel.selectCategory(category.id)
                            navigateTopLevel(DilliBiteRoutes.SEARCH)
                        },
                        onRestaurantClick = { restaurantId ->
                            navController.navigate(DilliBiteRoutes.restaurant(restaurantId))
                        },
                        onProfileClick = { navigateTopLevel(DilliBiteRoutes.PROFILE) },
                        onTogglePureVeg = viewModel::togglePureVeg,
                        onToggleRating45Plus = viewModel::toggleRating45Plus,
                        onToggleUnder25Mins = viewModel::toggleUnder25Mins,
                        onToggleUnder250 = viewModel::toggleUnder250
                    )
                }

                composable(DilliBiteRoutes.SEARCH) {
                    SearchAndCategoryScreen(
                        uiState = uiState,
                        onLocationClick = { showLocationSheet = true },
                        onQueryChange = viewModel::updateSearchQuery,
                        onSelectCategory = viewModel::selectCategory,
                        onRestaurantClick = { restaurantId ->
                            navController.navigate(DilliBiteRoutes.restaurant(restaurantId))
                        },
                        onDishClick = { restaurantId, dishId ->
                            navController.navigate(DilliBiteRoutes.dish(restaurantId, dishId))
                        },
                        onAddDish = { dish, restaurant ->
                            viewModel.addToCart(dish, restaurant, 1)
                            scope.launch {
                                snackbarHostState.showSnackbar("Added ${dish.name} to cart")
                            }
                        },
                        onUpdateDishQty = viewModel::updateCartItemQuantity
                    )
                }

                composable(
                    route = DilliBiteRoutes.RESTAURANT,
                    arguments = listOf(navArgument("restaurantId") { type = NavType.StringType })
                ) { entry ->
                    val restaurantId = entry.arguments?.getString("restaurantId") ?: return@composable
                    val restaurant = uiState.findRestaurant(restaurantId)
                    if (restaurant != null) {
                        RestaurantDetailScreen(
                            restaurant = restaurant,
                            selectedZoneName = uiState.selectedZone.areaName,
                            cartQtyMap = cartQtyMap,
                            onBackClick = { navController.popBackStack() },
                            onDishClick = { dish ->
                                navController.navigate(DilliBiteRoutes.dish(restaurant.id, dish.id))
                            },
                            onAddDish = { dish ->
                                viewModel.addToCart(dish, restaurant, 1)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Added ${dish.name} to cart")
                                }
                            },
                            onUpdateDishQty = viewModel::updateCartItemQuantity
                        )
                    }
                }

                composable(
                    route = DilliBiteRoutes.DISH,
                    arguments = listOf(
                        navArgument("restaurantId") { type = NavType.StringType },
                        navArgument("dishId") { type = NavType.StringType }
                    )
                ) { entry ->
                    val dishId = entry.arguments?.getString("dishId") ?: return@composable
                    val pair = uiState.findDish(dishId)
                    if (pair != null) {
                        val (restaurant, dish) = pair
                        DishDetailScreen(
                            restaurant = restaurant,
                            dish = dish,
                            onBackClick = { navController.popBackStack() },
                            onAddToCart = { qty ->
                                viewModel.addToCart(dish, restaurant, qty)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Added ${qty}x ${dish.name} to cart")
                                }
                            },
                            onBuyNow = { qty ->
                                viewModel.startBuyNow(dish, restaurant, qty)
                                navController.navigate(DilliBiteRoutes.CHECKOUT_COD)
                            }
                        )
                    }
                }

                composable(DilliBiteRoutes.CART) {
                    CartScreen(
                        uiState = uiState,
                        onUpdateQuantity = viewModel::updateCartItemQuantity,
                        onBrowseFoodClick = { navigateTopLevel(DilliBiteRoutes.HOME) },
                        onProceedToCheckout = {
                            viewModel.startCheckoutFromCart()
                            navController.navigate(DilliBiteRoutes.CHECKOUT_COD)
                        }
                    )
                }

                composable(DilliBiteRoutes.CHECKOUT_COD) {
                    CheckoutCodScreen(
                        uiState = uiState,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToProfileAuth = { navigateTopLevel(DilliBiteRoutes.PROFILE) },
                        onPlaceOrder = { address ->
                            viewModel.placeCodOrder(address) {
                                navController.navigate(DilliBiteRoutes.ORDER_CONFIRMED) {
                                    popUpTo(DilliBiteRoutes.CHECKOUT_COD) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(DilliBiteRoutes.ORDER_CONFIRMED) {
                    val order = uiState.latestPlacedOrder
                    if (order != null) {
                        OrderConfirmationScreen(
                            order = order,
                            onDoneHome = {
                                viewModel.clearLatestOrder()
                                navController.navigate(DilliBiteRoutes.HOME) {
                                    popUpTo(DilliBiteRoutes.HOME) { inclusive = true }
                                }
                            },
                            onViewOrdersInProfile = {
                                viewModel.clearLatestOrder()
                                navController.navigate(DilliBiteRoutes.PROFILE) {
                                    popUpTo(DilliBiteRoutes.HOME)
                                }
                            }
                        )
                    }
                }

                composable(DilliBiteRoutes.PROFILE) {
                    ProfileScreen(
                        uiState = uiState,
                        onSignIn = viewModel::signIn,
                        onSignUp = viewModel::signUp,
                        onUpdateAddress = viewModel::updateProfileAddress,
                        onSignOut = viewModel::signOut,
                        onClearError = viewModel::clearAuthError,
                        onOpenSellerPortal = {
                            navController.navigate(DilliBiteRoutes.SELLER_PORTAL)
                        }
                    )
                }

                composable(DilliBiteRoutes.SELLER_PORTAL) {
                    SellerDashboardScreen(
                        uiState = uiState,
                        onBackToCustomer = { navController.popBackStack() },
                        onSignInSeller = viewModel::signInSeller,
                        onSignUpSeller = viewModel::signUpSeller,
                        onDemoSellerLogin = viewModel::signInDemoSeller,
                        onSaveRestaurant = viewModel::saveSellerRestaurant,
                        onSaveDish = viewModel::saveSellerDish,
                        onDeleteDish = viewModel::deleteSellerDish,
                        onUpdateOrderStatus = viewModel::updateSellerOrderStatus,
                        onSignOut = viewModel::signOut,
                        onClearMessages = viewModel::clearSellerMessages
                    )
                }
            }
        }
    }

    if (showLocationSheet) {
        DelhiNcrLocationSheet(
            zones = uiState.zones,
            selectedZone = uiState.selectedZone,
            onSelectZone = viewModel::selectZone,
            onDismiss = { showLocationSheet = false }
        )
    }
}
