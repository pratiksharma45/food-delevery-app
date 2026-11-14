package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.Dish
import com.example.model.FoodCategory
import com.example.ui.components.FoodImage
import java.io.ByteArrayOutputStream
import kotlin.math.min

fun compressPickedImageToDataUrl(context: Context, uri: Uri): String? {
    return try {
        val rawStream = context.contentResolver.openInputStream(uri) ?: return null
        val decoded = rawStream.use { BitmapFactory.decodeStream(it) } ?: return null
        val maxDim = 640f
        val scale = min(1f, maxDim / maxOf(decoded.width, decoded.height).toFloat())
        val targetWidth = (decoded.width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (decoded.height * scale).toInt().coerceAtLeast(1)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true)
        } else {
            decoded
        }
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 72, out)
        val bytes = out.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (_: Exception) {
        null
    }
}

@Composable
fun SellerDishEditorDialog(
    initialDish: Dish?,
    categories: List<FoodCategory>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Dish, Boolean) -> Unit
) {
    val isNew = initialDish == null
    val context = LocalContext.current

    var name by remember(initialDish) { mutableStateOf(initialDish?.name ?: "") }
    var categoryId by remember(initialDish) {
        mutableStateOf(initialDish?.categoryId ?: categories.firstOrNull()?.id ?: "biryani")
    }
    var priceText by remember(initialDish) { mutableStateOf(initialDish?.priceInr?.toString() ?: "") }
    var mrpText by remember(initialDish) { mutableStateOf(initialDish?.originalPriceInr?.toString() ?: "") }
    var prepMinsText by remember(initialDish) { mutableStateOf((initialDish?.prepTimeMins ?: 22).toString()) }
    var serves by remember(initialDish) { mutableStateOf(initialDish?.serves ?: "Serves 1-2 people") }
    var spiceLevel by remember(initialDish) { mutableStateOf(initialDish?.spiceLevel ?: "Medium Spicy") }
    var shortDesc by remember(initialDish) { mutableStateOf(initialDish?.shortDescription ?: "") }
    var fullDesc by remember(initialDish) { mutableStateOf(initialDish?.description ?: "") }
    var whatYouGetText by remember(initialDish) {
        mutableStateOf((initialDish?.whatYouWillGet ?: emptyList()).joinToString("\n"))
    }
    var imageUrl by remember(initialDish) { mutableStateOf(initialDish?.imageUrl ?: "") }
    var isVeg by remember(initialDish) { mutableStateOf(initialDish?.isVeg ?: false) }
    var isBestseller by remember(initialDish) { mutableStateOf(initialDish?.isBestseller ?: false) }
    var isAvailable by remember(initialDish) { mutableStateOf(initialDish?.isAvailable ?: true) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val encoded = compressPickedImageToDataUrl(context, uri)
            if (encoded != null) {
                imageUrl = encoded
            } else {
                validationError = "Could not process selected photo."
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Add New Dish" else "Edit Dish",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seller_dish_name_input")
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    val selectedCategoryTitle = categories.firstOrNull { it.id == categoryId }?.title ?: categoryId
                    OutlinedTextField(
                        value = selectedCategoryTitle,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Food Category *") },
                        trailingIcon = {
                            IconButton(onClick = { showCategoryMenu = true }) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Select category")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.title) },
                                onClick = {
                                    categoryId = cat.id
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("seller_dish_price_input")
                    )
                    OutlinedTextField(
                        value = mrpText,
                        onValueChange = { mrpText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("MRP (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prepMinsText,
                        onValueChange = { prepMinsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Mins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = serves,
                        onValueChange = { serves = it },
                        label = { Text("Serves *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = spiceLevel,
                        onValueChange = { spiceLevel = it },
                        label = { Text("Spice Level *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = shortDesc,
                    onValueChange = { shortDesc = it },
                    label = { Text("1-Line Short Description *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fullDesc,
                    onValueChange = { fullDesc = it },
                    label = { Text("Full Description *") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = whatYouGetText,
                    onValueChange = { whatYouGetText = it },
                    label = { Text("What Customer Gets (one per line) *") },
                    placeholder = { Text("1 Handi Biryani (600g)\nMint Raita (120ml)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = if (imageUrl.startsWith("data:image")) "[Uploaded Photo Ready]" else imageUrl,
                    onValueChange = { if (!it.startsWith("[Uploaded")) imageUrl = it },
                    label = { Text("Dish Image URL (or pick photo)") },
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
                        Text("Pick Photo")
                    }
                    if (imageUrl.isNotBlank()) {
                        FoodImage(
                            imageUrl = imageUrl,
                            fallbackRes = R.drawable.img_food_biryani,
                            contentDescription = "Dish image preview",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pure Veg Dish", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bestseller Tag", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isBestseller, onCheckedChange = { isBestseller = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Available for Customers", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    enabled = !isSaving,
                    onClick = {
                        val price = priceText.toIntOrNull() ?: 0
                        val mrp = mrpText.toIntOrNull()?.takeIf { it > price }
                        val prep = (prepMinsText.toIntOrNull() ?: 20).coerceIn(1, 180)
                        val items = whatYouGetText.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() }

                        when {
                            name.trim().length < 2 -> validationError = "Enter a valid dish name (min 2 chars)."
                            price < 1 -> validationError = "Enter a valid selling price in ₹."
                            shortDesc.isBlank() -> validationError = "Enter a short 1-line description."
                            fullDesc.isBlank() -> validationError = "Enter a full dish description."
                            items.isEmpty() -> validationError = "List at least one item in 'What Customer Gets'."
                            else -> {
                                validationError = null
                                val updatedDish = (initialDish ?: Dish(
                                    id = "",
                                    restaurantId = "",
                                    name = "",
                                    categoryId = categoryId,
                                    priceInr = price,
                                    isVeg = isVeg,
                                    rating = 4.6,
                                    ratingCount = 120,
                                    shortDescription = "",
                                    description = "",
                                    whatYouWillGet = items,
                                    serves = serves,
                                    prepTimeMins = prep
                                )).copy(
                                    name = name.trim(),
                                    categoryId = categoryId,
                                    priceInr = price,
                                    originalPriceInr = mrp,
                                    isVeg = isVeg,
                                    isBestseller = isBestseller,
                                    spiceLevel = spiceLevel.trim().ifBlank { "Medium" },
                                    shortDescription = shortDesc.trim(),
                                    description = fullDesc.trim(),
                                    whatYouWillGet = items,
                                    serves = serves.trim().ifBlank { "Serves 1 person" },
                                    prepTimeMins = prep,
                                    imageUrl = imageUrl.trim().takeIf { it.isNotBlank() },
                                    isAvailable = isAvailable
                                )
                                onSave(updatedDish, isNew)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seller_save_dish_button")
                ) {
                    Text(
                        text = if (isSaving) "Syncing…" else if (isNew) "Publish Dish" else "Save Dish Changes",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
