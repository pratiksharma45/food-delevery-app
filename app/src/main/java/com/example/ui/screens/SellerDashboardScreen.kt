package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DilliBiteFirebaseRepository
import com.example.model.Dish
import com.example.model.PlacedOrder
import com.example.model.Restaurant
import com.example.ui.DilliBiteUiState
import com.example.ui.components.FoodImage
import com.example.ui.components.VegNonVegIndicator
import com.example.ui.theme.CorianderSoftContainer
import com.example.ui.theme.CorianderVegGreen
import com.example.ui.theme.SaffronSoftContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    uiState: DilliBiteUiState,
    onBackToCustomer: () -> Unit,
    onSignInSeller: (String, String) -> Unit,
    onSignUpSeller: (String, String, String, String, String, String, List<String>, List<String>, Int, Int) -> Unit,
    onDemoSellerLogin: () -> Unit,
    onSaveRestaurant: (Restaurant) -> Unit,
    onSaveDish: (Dish, Boolean, () -> Unit) -> Unit,
    onDeleteDish: (Dish) -> Unit,
    onUpdateOrderStatus: (String, String, String) -> Unit,
    onSignOut: () -> Unit,
    onClearMessages: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToCustomer)

    val seller = uiState.sellerProfile
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingDish by remember { mutableStateOf<Dish?>(null) }
    var showDishEditor by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = seller?.restaurantName?.ifBlank { "Seller Partner Dashboard" }
                                ?: "DilliBite Seller Portal",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (seller != null) "Live Sync • Delhi NCR Partner" else "Manage Restaurant, Dishes & Live Orders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToCustomer,
                        modifier = Modifier.testTag("seller_back_customer_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Customer App"
                        )
                    }
                },
                actions = {
                    if (seller != null) {
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.testTag("seller_signout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out Seller",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (seller != null && selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        editingDish = null
                        showDishEditor = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("seller_add_dish_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add New Dish")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            uiState.sellerStatusMessage?.let { msg ->
                Surface(
                    color = CorianderSoftContainer,
                    border = BorderStroke(1.dp, CorianderVegGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = CorianderVegGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onClearMessages) { Text("OK") }
                    }
                }
            }

            uiState.sellerErrorMessage?.let { err ->
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onClearMessages) { Text("Dismiss") }
                    }
                }
            }

            if (seller == null) {
                SellerAuthContent(
                    uiState = uiState,
                    onSignInSeller = onSignInSeller,
                    onSignUpSeller = onSignUpSeller,
                    onDemoSellerLogin = onDemoSellerLogin,
                    onClearMessages = onClearMessages
                )
            } else {
                // KPI Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SellerStatTile(
                        label = "Menu Dishes",
                        value = uiState.sellerDishes.size.toString(),
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    SellerStatTile(
                        label = "Live COD Orders",
                        value = uiState.sellerOrders.size.toString(),
                        accent = CorianderVegGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SellerStatTile(
                        label = "Store Status",
                        value = if (uiState.sellerRestaurant?.isActive != false) "OPEN" else "PAUSED",
                        accent = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Dishes (${uiState.sellerDishes.size})") },
                        modifier = Modifier.testTag("seller_tab_dishes")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Orders (${uiState.sellerOrders.size})") },
                        modifier = Modifier.testTag("seller_tab_orders")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Restaurant") },
                        modifier = Modifier.testTag("seller_tab_store")
                    )
                }

                when (selectedTab) {
                    0 -> SellerDishesTab(
                        dishes = uiState.sellerDishes,
                        onAddDish = {
                            editingDish = null
                            showDishEditor = true
                        },
                        onEditDish = { dish ->
                            editingDish = dish
                            showDishEditor = true
                        },
                        onToggleAvailability = { dish ->
                            onSaveDish(dish.copy(isAvailable = !dish.isAvailable), false) {}
                        },
                        onDeleteDish = onDeleteDish
                    )

                    1 -> SellerOrdersTab(
                        orders = uiState.sellerOrders,
                        onUpdateStatus = onUpdateOrderStatus
                    )

                    else -> SellerStoreTab(
                        restaurant = uiState.sellerRestaurant,
                        zones = uiState.zones,
                        isSaving = uiState.isSellerSaving,
                        onSaveRestaurant = onSaveRestaurant
                    )
                }
            }
        }
    }

    if (showDishEditor) {
        SellerDishEditorDialog(
            initialDish = editingDish,
            categories = uiState.categories,
            isSaving = uiState.isSellerSaving,
            onDismiss = { showDishEditor = false },
            onSave = { dish, isNew ->
                onSaveDish(dish, isNew) {
                    showDishEditor = false
                }
            }
        )
    }
}

@Composable
private fun SellerStatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accent,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SellerAuthContent(
    uiState: DilliBiteUiState,
    onSignInSeller: (String, String) -> Unit,
    onSignUpSeller: (String, String, String, String, String, String, List<String>, List<String>, Int, Int) -> Unit,
    onDemoSellerLogin: () -> Unit,
    onClearMessages: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var restName by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf(uiState.selectedZone.areaName) }
    var cuisines by remember { mutableStateOf("Biryani, North Indian, Tandoor") }
    var timeText by remember { mutableStateOf("28") }
    var priceForOneText by remember { mutableStateOf("250") }
    var selectedZoneIds by remember { mutableStateOf(setOf("cp", "chandni_chowk", "hauz_khas")) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Demo Seller Card
        Surface(
            color = SaffronSoftContainer,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Storefront,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Instant Demo Seller Test Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Restaurant: Karim's Royal Mughlai\nID: ${DilliBiteFirebaseRepository.DEMO_SELLER_EMAIL}\nPassword: ${DilliBiteFirebaseRepository.DEMO_SELLER_PASSWORD}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    enabled = !uiState.isSellerAuthLoading,
                    onClick = onDemoSellerLogin,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seller_demo_login_button")
                ) {
                    Text(
                        text = if (uiState.isSellerAuthLoading) "Connecting…" else "One-Click Demo Seller Login (Karim's)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !isSignUp,
                onClick = {
                    isSignUp = false
                    validationError = null
                    onClearMessages()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("Seller Sign In") }
            SegmentedButton(
                selected = isSignUp,
                onClick = {
                    isSignUp = true
                    validationError = null
                    onClearMessages()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("Seller Sign Up") }
        }

        validationError?.let { msg ->
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isSignUp) {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Owner Full Name *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Mobile Number (+91) *") },
                        prefix = { Text("+91 ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Seller Email ID *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seller_email_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min 6 chars) *") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seller_password_input")
                )

                if (isSignUp) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    OutlinedTextField(
                        value = restName,
                        onValueChange = { restName = it },
                        label = { Text("Restaurant Name *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = locality,
                        onValueChange = { locality = it },
                        label = { Text("Primary Delhi NCR Locality *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cuisines,
                        onValueChange = { cuisines = it },
                        label = { Text("Cuisines (comma separated)") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = timeText,
                            onValueChange = { timeText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Delivery Mins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceForOneText,
                            onValueChange = { priceForOneText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Price for One (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "Delivery Zones in Delhi NCR",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.zones.forEach { zone ->
                            val selected = zone.id in selectedZoneIds
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedZoneIds = if (selected && selectedZoneIds.size > 1) {
                                        selectedZoneIds - zone.id
                                    } else {
                                        selectedZoneIds + zone.id
                                    }
                                },
                                label = { Text(zone.areaName) }
                            )
                        }
                    }
                }

                Button(
                    enabled = !uiState.isSellerAuthLoading,
                    onClick = {
                        if (!isSignUp) {
                            if (email.isBlank() || password.isBlank()) {
                                validationError = "Enter Seller Email ID and Password."
                            } else {
                                validationError = null
                                onSignInSeller(email, password)
                            }
                        } else {
                            val cleanPhone = phone.filter { it.isDigit() }
                            when {
                                ownerName.trim().length < 2 -> validationError = "Enter owner full name."
                                cleanPhone.length != 10 -> validationError = "Enter 10-digit mobile number."
                                !email.contains("@") -> validationError = "Enter valid email ID."
                                password.length < 6 -> validationError = "Password must be at least 6 characters."
                                restName.trim().length < 2 -> validationError = "Enter restaurant name."
                                locality.trim().length < 2 -> validationError = "Enter Delhi NCR locality."
                                else -> {
                                    validationError = null
                                    onSignUpSeller(
                                        ownerName,
                                        email,
                                        cleanPhone,
                                        password,
                                        restName,
                                        locality,
                                        cuisines.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                        selectedZoneIds.toList(),
                                        timeText.toIntOrNull() ?: 28,
                                        priceForOneText.toIntOrNull() ?: 250
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seller_auth_submit_button")
                ) {
                    Text(
                        text = if (uiState.isSellerAuthLoading) {
                            "Please wait…"
                        } else if (isSignUp) {
                            "Create Seller Account & Store"
                        } else {
                            "Sign In to Seller Dashboard"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SellerDishesTab(
    dishes: List<Dish>,
    onAddDish: () -> Unit,
    onEditDish: (Dish) -> Unit,
    onToggleAvailability: (Dish) -> Unit,
    onDeleteDish: (Dish) -> Unit
) {
    if (dishes.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No dishes listed yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add your signature Biryani, Thalis, or Chaat items to sync instantly with DilliBite customers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddDish) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add First Dish")
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dishes, key = { it.id }) { dish ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FoodImage(
                                imageUrl = dish.imageUrl,
                                fallbackRes = dish.imageRes,
                                contentDescription = dish.name,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    VegNonVegIndicator(isVeg = dish.isVeg)
                                    Text(
                                        text = dish.categoryId.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!dish.isAvailable) {
                                        Text(
                                            text = "OUT OF STOCK",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${dish.priceInr} • ${dish.serves}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = dish.shortDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onEditDish(dish) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit")
                            }
                            OutlinedButton(
                                onClick = { onToggleAvailability(dish) },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text(if (dish.isAvailable) "Pause Item" else "Make Live")
                            }
                            IconButton(onClick = { onDeleteDish(dish) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete dish",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SellerOrdersTab(
    orders: List<PlacedOrder>,
    onUpdateStatus: (String, String, String) -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No customer COD orders for your restaurant yet. Place an order from the customer storefront to test live sync!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.orderId }) { order ->
                val isTerminal = order.status == "DELIVERED" || order.status == "CANCELLED"
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order #${order.orderId}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${order.grandTotalInr} COD",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Status: ${order.status.replace('_', ' ')}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (order.status == "DELIVERED") CorianderVegGreen else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Customer: ${order.deliveryAddress.fullName} (+91 ${order.deliveryAddress.phone})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${order.deliveryAddress.houseFlat}, ${order.deliveryAddress.streetColony}, ${order.deliveryAddress.locality} - ${order.deliveryAddress.pinCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        order.items.forEach { item ->
                            Text(
                                text = "• ${item.quantity}x ${item.dish.name} (₹${item.dish.priceInr * item.quantity})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (!isTerminal) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedButton(onClick = { onUpdateStatus(order.orderId, "PREPARING", order.etaText) }) {
                                    Text("Preparing")
                                }
                                OutlinedButton(onClick = { onUpdateStatus(order.orderId, "OUT_FOR_DELIVERY", order.etaText) }) {
                                    Text("Out for Delivery")
                                }
                                Button(onClick = { onUpdateStatus(order.orderId, "DELIVERED", order.etaText) }) {
                                    Text("Delivered")
                                }
                                TextButton(onClick = { onUpdateStatus(order.orderId, "CANCELLED", order.etaText) }) {
                                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SellerStoreTab(
    restaurant: Restaurant?,
    zones: List<com.example.model.DelhiNcrZone>,
    isSaving: Boolean,
    onSaveRestaurant: (Restaurant) -> Unit
) {
    if (restaurant == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading restaurant profile…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val context = LocalContext.current
    var name by remember(restaurant) { mutableStateOf(restaurant.name) }
    var locality by remember(restaurant) { mutableStateOf(restaurant.locality) }
    var subRegion by remember(restaurant) { mutableStateOf(restaurant.subRegion) }
    var cuisinesText by remember(restaurant) { mutableStateOf(restaurant.cuisines.joinToString(", ")) }
    var offerText by remember(restaurant) { mutableStateOf(restaurant.offerText ?: "") }
    var timeText by remember(restaurant) { mutableStateOf(restaurant.deliveryTimeMins.toString()) }
    var feeText by remember(restaurant) { mutableStateOf(restaurant.deliveryFeeInr.toString()) }
    var priceOneText by remember(restaurant) { mutableStateOf(restaurant.priceForOneInr.toString()) }
    var imageUrl by remember(restaurant) { mutableStateOf(restaurant.imageUrl ?: "") }
    var isPureVeg by remember(restaurant) { mutableStateOf(restaurant.isPureVeg) }
    var isActive by remember(restaurant) { mutableStateOf(restaurant.isActive) }
    var selectedZoneIds by remember(restaurant) { mutableStateOf(restaurant.deliveryZoneIds.toSet()) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            compressPickedImageToDataUrl(context, uri)?.let { imageUrl = it }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Restaurant Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = locality,
            onValueChange = { locality = it },
            label = { Text("Primary Locality *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = subRegion,
            onValueChange = { subRegion = it },
            label = { Text("Delhi NCR Sub-Region") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = cuisinesText,
            onValueChange = { cuisinesText = it },
            label = { Text("Cuisines (comma separated)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = offerText,
            onValueChange = { offerText = it },
            label = { Text("Promo Offer Banner (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = timeText,
                onValueChange = { timeText = it.filter { ch -> ch.isDigit() } },
                label = { Text("ETA Mins") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = feeText,
                onValueChange = { feeText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Del Fee ₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = priceOneText,
                onValueChange = { priceOneText = it.filter { ch -> ch.isDigit() } },
                label = { Text("For One ₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = if (imageUrl.startsWith("data:image")) "[Uploaded Store Image Ready]" else imageUrl,
            onValueChange = { if (!it.startsWith("[Uploaded")) imageUrl = it },
            label = { Text("Cover Image URL (or pick photo)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Upload Cover Photo")
            }
            if (imageUrl.isNotBlank()) {
                FoodImage(
                    imageUrl = imageUrl,
                    fallbackRes = R.drawable.img_hero_delhi_ncr,
                    contentDescription = "Restaurant cover preview",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pure Veg Restaurant", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = isPureVeg, onCheckedChange = { isPureVeg = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Store Open & Accepting Orders", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = isActive, onCheckedChange = { isActive = it })
        }

        Text(
            text = "Delhi NCR Delivery Zones",
            style = MaterialTheme.typography.labelLarge
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            zones.forEach { zone ->
                val selected = zone.id in selectedZoneIds
                FilterChip(
                    selected = selected,
                    onClick = {
                        selectedZoneIds = if (selected && selectedZoneIds.size > 1) {
                            selectedZoneIds - zone.id
                        } else {
                            selectedZoneIds + zone.id
                        }
                    },
                    label = { Text(zone.areaName) }
                )
            }
        }

        Button(
            enabled = !isSaving,
            onClick = {
                onSaveRestaurant(
                    restaurant.copy(
                        name = name.trim().ifBlank { restaurant.name },
                        locality = locality.trim().ifBlank { restaurant.locality },
                        subRegion = subRegion.trim().ifBlank { "Delhi NCR" },
                        cuisines = cuisinesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            .ifEmpty { listOf("North Indian") },
                        offerText = offerText.trim().takeIf { it.isNotBlank() },
                        deliveryTimeMins = (timeText.toIntOrNull() ?: restaurant.deliveryTimeMins).coerceIn(5, 120),
                        deliveryFeeInr = (feeText.toIntOrNull() ?: restaurant.deliveryFeeInr).coerceIn(0, 500),
                        priceForOneInr = (priceOneText.toIntOrNull() ?: restaurant.priceForOneInr).coerceIn(10, 5000),
                        imageUrl = imageUrl.trim().takeIf { it.isNotBlank() },
                        isPureVeg = isPureVeg,
                        isActive = isActive,
                        deliveryZoneIds = selectedZoneIds.toList().ifEmpty { listOf("cp") }
                    )
                )
            },
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("seller_save_restaurant_button")
        ) {
            Text(
                text = if (isSaving) "Saving…" else "Save Restaurant & Sync Storefront",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
