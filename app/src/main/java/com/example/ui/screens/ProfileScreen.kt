package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.model.UserAddress
import com.example.ui.DilliBiteUiState
import com.example.ui.theme.CorianderVegGreen

@Composable
fun ProfileScreen(
    uiState: DilliBiteUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String, String, UserAddress) -> Unit,
    onUpdateAddress: (UserAddress) -> Unit,
    onSignOut: () -> Unit,
    onClearError: () -> Unit,
    onOpenSellerPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.userProfile

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
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (profile == null) {
                        "Sign in or create your customer account for faster COD checkout"
                    } else {
                        "DilliBite Customer • Delhi NCR"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (profile == null) {
            AuthFormsContent(
                uiState = uiState,
                onSignIn = onSignIn,
                onSignUp = onSignUp,
                onClearError = onClearError,
                onOpenSellerPortal = onOpenSellerPortal
            )
        } else {
            SignedInProfileContent(
                uiState = uiState,
                onUpdateAddress = onUpdateAddress,
                onSignOut = onSignOut,
                onOpenSellerPortal = onOpenSellerPortal
            )
        }
    }
}

@Composable
private fun AuthFormsContent(
    uiState: DilliBiteUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String, String, UserAddress) -> Unit,
    onClearError: () -> Unit,
    onOpenSellerPortal: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }

    // Sign In State
    var signInEmail by remember { mutableStateOf("") }
    var signInPassword by remember { mutableStateOf("") }
    var showSignInPassword by remember { mutableStateOf(false) }

    // Sign Up State
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var showSignUpPassword by remember { mutableStateOf(false) }
    var houseFlat by remember { mutableStateOf("") }
    var streetColony by remember { mutableStateOf("") }
    var locality by remember(uiState.selectedZone) { mutableStateOf(uiState.selectedZone.areaName) }
    var landmark by remember { mutableStateOf("") }
    var pinCode by remember(uiState.selectedZone) { mutableStateOf(uiState.selectedZone.defaultPinCode) }
    var showZoneMenu by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

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
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !isSignUpMode,
                onClick = {
                    isSignUpMode = false
                    localValidationError = null
                    onClearError()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.testTag("profile_tab_sign_in")
            ) {
                Text("Sign In")
            }
            SegmentedButton(
                selected = isSignUpMode,
                onClick = {
                    isSignUpMode = true
                    localValidationError = null
                    onClearError()
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.testTag("profile_tab_sign_up")
            ) {
                Text("Sign Up")
            }
        }

        val displayError = localValidationError ?: uiState.authErrorMessage
        if (displayError != null) {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = displayError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isSignUpMode) {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sign in using your Email ID and password.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = signInEmail,
                        onValueChange = { signInEmail = it },
                        label = { Text("Email ID") },
                        placeholder = { Text("name@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_in_email_input")
                    )

                    OutlinedTextField(
                        value = signInPassword,
                        onValueChange = { signInPassword = it },
                        label = { Text("Password") },
                        visualTransformation = if (showSignInPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showSignInPassword = !showSignInPassword }) {
                                Icon(
                                    imageVector = if (showSignInPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_in_password_input")
                    )

                    Button(
                        enabled = !uiState.isAuthLoading,
                        onClick = {
                            if (signInEmail.isBlank() || signInPassword.isBlank()) {
                                localValidationError = "Please enter both Email ID and password."
                            } else {
                                localValidationError = null
                                onSignIn(signInEmail, signInPassword)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_in_submit_button")
                    ) {
                        Text(
                            text = if (uiState.isAuthLoading) "Signing In…" else "Sign In",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Create Customer Account",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fill in your contact details & Delhi NCR delivery address once for instant Cash on Delivery orders.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_up_name_input")
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
                            .testTag("sign_up_phone_input")
                    )

                    OutlinedTextField(
                        value = signUpEmail,
                        onValueChange = { signUpEmail = it },
                        label = { Text("Email ID *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_up_email_input")
                    )

                    OutlinedTextField(
                        value = signUpPassword,
                        onValueChange = { signUpPassword = it },
                        label = { Text("Create Password (min 4 chars) *") },
                        visualTransformation = if (showSignUpPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showSignUpPassword = !showSignUpPassword }) {
                                Icon(
                                    imageVector = if (showSignUpPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_up_password_input")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Text(
                        text = "Delivery Address",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = houseFlat,
                        onValueChange = { houseFlat = it },
                        label = { Text("House / Flat / Floor No. *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_up_house_input")
                    )

                    OutlinedTextField(
                        value = streetColony,
                        onValueChange = { streetColony = it },
                        label = { Text("Street / Colony / Sector *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_up_street_input")
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = locality,
                            onValueChange = { locality = it },
                            label = { Text("Delhi NCR Area / Locality *") },
                            trailingIcon = {
                                IconButton(onClick = { showZoneMenu = true }) {
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
                            expanded = showZoneMenu,
                            onDismissRequest = { showZoneMenu = false }
                        ) {
                            uiState.zones.forEach { zone ->
                                DropdownMenuItem(
                                    text = { Text("${zone.areaName} (${zone.subRegion})") },
                                    onClick = {
                                        locality = zone.areaName
                                        pinCode = zone.defaultPinCode
                                        showZoneMenu = false
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
                                .testTag("sign_up_pin_input")
                        )
                    }

                    Button(
                        enabled = !uiState.isAuthLoading,
                        onClick = {
                            val cleanPhone = phone.filter { it.isDigit() }
                            val cleanPin = pinCode.filter { it.isDigit() }
                            when {
                                fullName.isBlank() -> localValidationError = "Please enter your full name."
                                cleanPhone.length != 10 -> localValidationError = "Enter a valid 10-digit mobile number."
                                !signUpEmail.contains("@") -> localValidationError = "Enter a valid email ID."
                                signUpPassword.length < 6 -> localValidationError = "Password must be at least 6 characters for Firebase Auth."
                                houseFlat.isBlank() || streetColony.isBlank() -> localValidationError = "Please enter your House/Flat and Street/Colony."
                                cleanPin.length != 6 -> localValidationError = "Enter a valid 6-digit PIN code."
                                else -> {
                                    localValidationError = null
                                    onSignUp(
                                        fullName,
                                        signUpEmail,
                                        cleanPhone,
                                        signUpPassword,
                                        UserAddress(
                                            fullName = fullName.trim(),
                                            phone = cleanPhone,
                                            houseFlat = houseFlat.trim(),
                                            streetColony = streetColony.trim(),
                                            locality = locality.trim(),
                                            landmark = landmark.trim(),
                                            pinCode = cleanPin
                                        )
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_up_submit_button")
                    ) {
                        Text(
                            text = if (uiState.isAuthLoading) "Creating Account…" else "Create Account & Save Address",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        SellerPortalBannerCard(onOpenSellerPortal = onOpenSellerPortal)
    }
}

@Composable
private fun SignedInProfileContent(
    uiState: DilliBiteUiState,
    onUpdateAddress: (UserAddress) -> Unit,
    onSignOut: () -> Unit,
    onOpenSellerPortal: () -> Unit
) {
    val profile = uiState.userProfile ?: return
    var isEditingAddress by remember { mutableStateOf(false) }
    var houseFlat by remember(profile.address) { mutableStateOf(profile.address.houseFlat) }
    var streetColony by remember(profile.address) { mutableStateOf(profile.address.streetColony) }
    var locality by remember(profile.address) { mutableStateOf(profile.address.locality) }
    var landmark by remember(profile.address) { mutableStateOf(profile.address.landmark) }
    var pinCode by remember(profile.address) { mutableStateOf(profile.address.pinCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.sellerProfile != null) {
            SellerPortalBannerCard(
                restaurantName = uiState.sellerProfile.restaurantName,
                onOpenSellerPortal = onOpenSellerPortal
            )
        }

        // User Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = profile.fullName.firstOrNull()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+91 ${profile.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Saved Delivery Address Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Saved Delivery Address",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    TextButton(onClick = { isEditingAddress = !isEditingAddress }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(if (isEditingAddress) "Cancel" else "Edit")
                    }
                }

                if (!isEditingAddress) {
                    Text(
                        text = buildString {
                            append(profile.address.houseFlat)
                            append(", ")
                            append(profile.address.streetColony)
                            append("\n")
                            append(profile.address.locality)
                            if (profile.address.landmark.isNotBlank()) {
                                append(" (Near ${profile.address.landmark})")
                            }
                            append(" - ${profile.address.pinCode}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    OutlinedTextField(
                        value = houseFlat,
                        onValueChange = { houseFlat = it },
                        label = { Text("House / Flat No.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = streetColony,
                        onValueChange = { streetColony = it },
                        label = { Text("Street / Colony") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = locality,
                        onValueChange = { locality = it },
                        label = { Text("Delhi NCR Area") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = landmark,
                            onValueChange = { landmark = it },
                            label = { Text("Landmark") },
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 6) pinCode = it.filter { ch -> ch.isDigit() } },
                            label = { Text("PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                    Button(
                        onClick = {
                            onUpdateAddress(
                                profile.address.copy(
                                    houseFlat = houseFlat.trim(),
                                    streetColony = streetColony.trim(),
                                    locality = locality.trim(),
                                    landmark = landmark.trim(),
                                    pinCode = pinCode.trim()
                                )
                            )
                            isEditingAddress = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Updated Address")
                    }
                }
            }
        }

        // Session Orders
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your Orders (${uiState.recentOrders.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (uiState.recentOrders.isEmpty()) {
                    Text(
                        text = "No orders placed in this session yet. Tap any dish and click Buy Now to place a Cash on Delivery order.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.recentOrders.forEach { order ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = order.restaurantName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "₹${order.grandTotalInr}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${order.orderId} • ${order.placedAtText}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Status: ${order.status.replace('_', ' ')} • ${order.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CorianderVegGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = order.etaText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sign_out_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Sign Out",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }

        SellerPortalBannerCard(onOpenSellerPortal = onOpenSellerPortal)
    }
}

@Composable
private fun SellerPortalBannerCard(
    restaurantName: String? = null,
    onOpenSellerPortal: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, if (restaurantName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (restaurantName != null) {
                            "Seller Active: $restaurantName"
                        } else {
                            "Restaurant Seller Partner Portal"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (restaurantName != null) {
                            "Manage your dishes, photos, prices, and live COD orders."
                        } else {
                            "Sign up as a seller, edit your restaurant, upload dish photos, and manage live COD orders."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (restaurantName != null) {
                Button(
                    onClick = onOpenSellerPortal,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_seller_portal_button")
                ) {
                    Text("Open $restaurantName Dashboard", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onOpenSellerPortal,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_seller_portal_button")
                ) {
                    Text("Open Seller Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
