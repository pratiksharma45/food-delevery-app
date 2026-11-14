package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.CartItem
import com.example.model.DelhiNcrZone
import com.example.model.PlacedOrder
import com.example.model.UserAddress
import com.example.ui.DilliBiteUiState
import com.example.ui.components.VegNonVegIndicator
import com.example.ui.theme.CorianderSoftContainer
import com.example.ui.theme.CorianderVegGreen
import kotlin.math.roundToInt

@Composable
fun CartScreen(
    uiState: DilliBiteUiState,
    onUpdateQuantity: (String, Int) -> Unit,
    onBrowseFoodClick: () -> Unit,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemTotal = uiState.totalCartAmountInr
    val deliveryFee = if (itemTotal == 0 || itemTotal >= 499) 0 else 25
    val gstAndPkg = if (itemTotal == 0) 0 else (itemTotal * 0.05).roundToInt().coerceAtLeast(12)
    val grandTotal = itemTotal + deliveryFee + gstAndPkg

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = "Your Food Cart",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Delivering to ${uiState.selectedZone.areaName}, Delhi NCR",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (uiState.cartItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(84.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your cart is empty",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Add delicious Biryani, Thalis, or Chaat from top Delhi NCR kitchens to place a Cash on Delivery order.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onBrowseFoodClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("empty_cart_browse_button")
                ) {
                    Text("Explore Delhi NCR Food")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.cartItems, key = { it.dish.id }) { item ->
                    CartItemRow(
                        item = item,
                        onDelta = { delta -> onUpdateQuantity(item.dish.id, delta) }
                    )
                }

                item {
                    BillSummaryCard(
                        itemTotal = itemTotal,
                        deliveryFee = deliveryFee,
                        gstAndPkg = gstAndPkg,
                        grandTotal = grandTotal
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹$grandTotal",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cash on Delivery",
                            style = MaterialTheme.typography.labelSmall,
                            color = CorianderVegGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onProceedToCheckout,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                        modifier = Modifier.testTag("cart_proceed_checkout_button")
                    ) {
                        Text(
                            text = "Proceed to Buy Now",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onDelta: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            com.example.ui.components.FoodImage(
                imageUrl = item.dish.imageUrl,
                fallbackRes = item.dish.imageRes,
                contentDescription = item.dish.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VegNonVegIndicator(isVeg = item.dish.isVeg)
                    Text(
                        text = item.dish.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${item.restaurantName} • ${item.dish.serves}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${item.dish.priceInr * item.quantity}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { onDelta(-1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Remove one",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { onDelta(1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add one",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutCodScreen(
    uiState: DilliBiteUiState,
    onBackClick: () -> Unit,
    onNavigateToProfileAuth: () -> Unit,
    onPlaceOrder: (UserAddress) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    val items = uiState.checkoutItems.ifEmpty { uiState.cartItems }
    val itemTotal = items.sumOf { it.dish.priceInr * it.quantity }
    val deliveryFee = if (itemTotal >= 499) 0 else 25
    val gstAndPkg = (itemTotal * 0.05).roundToInt().coerceAtLeast(12)
    val grandTotal = itemTotal + deliveryFee + gstAndPkg

    val savedAddr = uiState.userProfile?.address
    var fullName by remember(uiState.userProfile) {
        mutableStateOf(savedAddr?.fullName?.ifBlank { uiState.userProfile?.fullName ?: "" } ?: "")
    }
    var phone by remember(uiState.userProfile) {
        mutableStateOf(savedAddr?.phone?.ifBlank { uiState.userProfile?.phone ?: "" } ?: "")
    }
    var houseFlat by remember(uiState.userProfile) { mutableStateOf(savedAddr?.houseFlat ?: "") }
    var streetColony by remember(uiState.userProfile) { mutableStateOf(savedAddr?.streetColony ?: "") }
    var locality by remember(uiState.selectedZone) {
        mutableStateOf(savedAddr?.locality?.ifBlank { uiState.selectedZone.areaName } ?: uiState.selectedZone.areaName)
    }
    var landmark by remember(uiState.userProfile) { mutableStateOf(savedAddr?.landmark ?: "") }
    var pinCode by remember(uiState.selectedZone) {
        mutableStateOf(savedAddr?.pinCode?.ifBlank { uiState.selectedZone.defaultPinCode } ?: uiState.selectedZone.defaultPinCode)
    }
    var instructions by remember { mutableStateOf(savedAddr?.instructions ?: "") }
    var showZoneDropdown by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Delivery Address & COD",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Delhi NCR Doorstep Delivery",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val combinedError = errorText ?: uiState.checkoutErrorMessage
                    if (combinedError != null) {
                        Text(
                            text = combinedError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (uiState.userProfile == null) {
                        OutlinedButton(
                            onClick = onNavigateToProfileAuth,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign In / Sign Up on Profile to Sync Live Orders", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        enabled = !uiState.isOrderSubmitting,
                        onClick = {
                            val cleanPhone = phone.filter { it.isDigit() }
                            val cleanPin = pinCode.filter { it.isDigit() }
                            when {
                                fullName.isBlank() -> errorText = "Please enter receiver's full name."
                                cleanPhone.length != 10 -> errorText = "Please enter a valid 10-digit Indian mobile number."
                                houseFlat.isBlank() -> errorText = "Please enter House / Flat / Floor number."
                                streetColony.isBlank() -> errorText = "Please enter Street / Colony / Sector."
                                cleanPin.length != 6 -> errorText = "Please enter a valid 6-digit PIN code."
                                else -> {
                                    errorText = null
                                    onPlaceOrder(
                                        UserAddress(
                                            fullName = fullName.trim(),
                                            phone = cleanPhone,
                                            houseFlat = houseFlat.trim(),
                                            streetColony = streetColony.trim(),
                                            locality = locality.trim(),
                                            landmark = landmark.trim(),
                                            pinCode = cleanPin,
                                            instructions = instructions.trim()
                                        )
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("place_cod_order_button")
                    ) {
                        Text(
                            text = if (uiState.isOrderSubmitting) {
                                "Placing Order…"
                            } else {
                                "Place Order • Cash on Delivery (₹$grandTotal)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Items Summary
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Order Summary (${items.sumOf { it.quantity }} item${if (items.sumOf { it.quantity } > 1) "s" else ""})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                VegNonVegIndicator(isVeg = item.dish.isVeg)
                                Text(
                                    text = "${item.quantity}x ${item.dish.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "₹${item.dish.priceInr * item.quantity}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Delivery Address Form
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Delivery Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Mobile Number (+91) *") },
                        prefix = { Text("+91 ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input")
                    )

                    OutlinedTextField(
                        value = houseFlat,
                        onValueChange = { houseFlat = it },
                        label = { Text("House / Flat / Floor No. *") },
                        placeholder = { Text("e.g., Flat 402, Tower B") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_house_input")
                    )

                    OutlinedTextField(
                        value = streetColony,
                        onValueChange = { streetColony = it },
                        label = { Text("Street / Colony / Sector *") },
                        placeholder = { Text("e.g., Block C, Lajpat Nagar II") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_street_input")
                    )

                    // Delhi NCR Area Dropdown Field
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = locality,
                            onValueChange = { locality = it },
                            label = { Text("Delhi NCR Area / Locality *") },
                            trailingIcon = {
                                IconButton(onClick = { showZoneDropdown = true }) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Choose Delhi NCR area"
                                    )
                                }
                            },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = showZoneDropdown,
                            onDismissRequest = { showZoneDropdown = false }
                        ) {
                            uiState.zones.forEach { zone: DelhiNcrZone ->
                                DropdownMenuItem(
                                    text = { Text("${zone.areaName} (${zone.subRegion})") },
                                    onClick = {
                                        locality = zone.areaName
                                        pinCode = zone.defaultPinCode
                                        showZoneDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = landmark,
                            onValueChange = { landmark = it },
                            label = { Text("Landmark") },
                            placeholder = { Text("Near Metro Gate 2") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1.3f)
                        )
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it.filter { ch -> ch.isDigit() } },
                            label = { Text("PIN Code *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("checkout_pin_input")
                        )
                    }

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Delivery Instructions (Optional)") },
                        placeholder = { Text("e.g., Leave at security guard / Call before arrival") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Payment Method: Cash on Delivery (COD)
            Surface(
                color = CorianderSoftContainer,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.5.dp, CorianderVegGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payments,
                        contentDescription = null,
                        tint = CorianderVegGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cash on Delivery (COD)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pay cash or UPI QR to our delivery partner at your doorstep",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.RadioButtonChecked,
                        contentDescription = "Selected",
                        tint = CorianderVegGreen
                    )
                }
            }

            BillSummaryCard(
                itemTotal = itemTotal,
                deliveryFee = deliveryFee,
                gstAndPkg = gstAndPkg,
                grandTotal = grandTotal
            )
        }
    }
}

@Composable
private fun BillSummaryCard(
    itemTotal: Int,
    deliveryFee: Int,
    gstAndPkg: Int,
    grandTotal: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Bill Summary",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            BillLine("Item Total", "₹$itemTotal")
            BillLine(
                "Delivery Fee",
                if (deliveryFee == 0) "FREE" else "₹$deliveryFee",
                valueColor = if (deliveryFee == 0) CorianderVegGreen else MaterialTheme.colorScheme.onSurface
            )
            BillLine("Restaurant Packaging & GST (5%)", "₹$gstAndPkg")
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Grand Total (COD)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹$grandTotal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BillLine(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun OrderConfirmationScreen(
    order: PlacedOrder,
    onDoneHome: () -> Unit,
    onViewOrdersInProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDoneHome)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = CorianderSoftContainer,
            shape = CircleShape,
            modifier = Modifier.size(92.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Order Confirmed",
                    tint = CorianderVegGreen,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Order Placed Successfully!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Order ID: ${order.orderId} • ${order.paymentMethod}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalShipping,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Estimated Arrival",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = order.etaText,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Text(
                    text = "Delivering to ${order.deliveryAddress.fullName} (+91 ${order.deliveryAddress.phone})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buildString {
                        append(order.deliveryAddress.houseFlat)
                        append(", ")
                        append(order.deliveryAddress.streetColony)
                        append(", ")
                        append(order.deliveryAddress.locality)
                        if (order.deliveryAddress.landmark.isNotBlank()) {
                            append(" (Near ${order.deliveryAddress.landmark})")
                        }
                        append(" - ${order.deliveryAddress.pinCode}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Text(
                    text = "From ${order.restaurantName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.dish.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${item.dish.priceInr * item.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pay Cash on Delivery",
                        style = MaterialTheme.typography.titleMedium,
                        color = CorianderVegGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${order.grandTotalInr}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onDoneHome,
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_confirm_back_home")
        ) {
            Text("Back to Home", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onViewOrdersInProfile,
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Order in My Profile")
        }
    }
}
