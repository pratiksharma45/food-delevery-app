package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.Dish
import com.example.model.Restaurant
import com.example.ui.DilliBiteUiState
import com.example.ui.components.DishListRow
import com.example.ui.components.RestaurantCard

@Composable
fun SearchAndCategoryScreen(
    uiState: DilliBiteUiState,
    onBackClick: (() -> Unit)? = null,
    onLocationClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onRestaurantClick: (String) -> Unit,
    onDishClick: (String, String) -> Unit,
    onAddDish: (Dish, Restaurant) -> Unit,
    onUpdateDishQty: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }
    val normalizedQuery = uiState.searchQuery.trim().lowercase()

    // Filter restaurants delivering to the user's current Delhi NCR zone
    val zoneRestaurants = remember(uiState.selectedZone, uiState.allRestaurants) {
        uiState.allRestaurants.filter { uiState.selectedZone.id in it.deliveryZoneIds }
    }

    val matchingRestaurants = remember(zoneRestaurants, uiState.selectedCategoryId, normalizedQuery) {
        zoneRestaurants.filter { restaurant ->
            val matchesCategory = uiState.selectedCategoryId == null ||
                restaurant.dishes.any { it.categoryId == uiState.selectedCategoryId } ||
                restaurant.cuisines.any {
                    selectedCategory != null && it.contains(selectedCategory.title, ignoreCase = true)
                }

            val matchesQuery = normalizedQuery.isBlank() ||
                restaurant.name.lowercase().contains(normalizedQuery) ||
                restaurant.cuisines.any { it.lowercase().contains(normalizedQuery) } ||
                restaurant.locality.lowercase().contains(normalizedQuery) ||
                restaurant.dishes.any {
                    it.name.lowercase().contains(normalizedQuery) ||
                        it.shortDescription.lowercase().contains(normalizedQuery)
                }

            matchesCategory && matchesQuery
        }
    }

    val matchingDishes = remember(zoneRestaurants, uiState.selectedCategoryId, normalizedQuery) {
        zoneRestaurants.flatMap { restaurant ->
            restaurant.dishes.filter { dish ->
                val matchesCategory = uiState.selectedCategoryId == null ||
                    dish.categoryId == uiState.selectedCategoryId
                val matchesQuery = normalizedQuery.isBlank() ||
                    dish.name.lowercase().contains(normalizedQuery) ||
                    dish.shortDescription.lowercase().contains(normalizedQuery) ||
                    restaurant.name.lowercase().contains(normalizedQuery)
                matchesCategory && matchesQuery
            }.map { dish -> restaurant to dish }
        }
    }

    val cartQtyMap = remember(uiState.cartItems) {
        uiState.cartItems.associate { it.dish.id to it.quantity }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar + Search Box
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCategory?.let { "${it.title} in ${uiState.selectedZone.areaName}" }
                                ?: "Search in ${uiState.selectedZone.areaName}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier
                                .clickable(onClick = onLocationClick)
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Delivering to ${uiState.selectedZone.areaName} (${uiState.selectedZone.subRegion}) • Tap to change",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "Search Biryani, Thali, Chaat, Karim's…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field")
                )

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryId == null,
                            onClick = { onSelectCategory(null) },
                            label = { Text("All Food") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    items(uiState.categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = uiState.selectedCategoryId == category.id,
                            onClick = {
                                onSelectCategory(
                                    if (uiState.selectedCategoryId == category.id) null else category.id
                                )
                            },
                            label = { Text(category.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("search_cat_chip_${category.id}")
                        )
                    }
                }
            }
        }

        if (matchingRestaurants.isEmpty() && matchingDishes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.RestaurantMenu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No matching food found in ${uiState.selectedZone.areaName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Try searching another dish, clearing the category filter, or selecting another Delhi NCR zone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    onQueryChange("")
                    onSelectCategory(null)
                }) {
                    Text("Show All Options")
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Restaurants serving this category/query at this location
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (selectedCategory != null) {
                                "Restaurants delivering ${selectedCategory.title} here (${matchingRestaurants.size})"
                            } else {
                                "Restaurants delivering here (${matchingRestaurants.size})"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tap a restaurant to see its full menu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(matchingRestaurants, key = { "search_rest_${it.id}" }) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onClick = { onRestaurantClick(restaurant.id) }
                    )
                }

                if (matchingDishes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedCategory != null) {
                                "${selectedCategory.title} Dishes near ${uiState.selectedZone.areaName} (${matchingDishes.size})"
                            } else {
                                "Recommended Dishes (${matchingDishes.size})"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(matchingDishes, key = { "search_dish_${it.second.id}" }) { (restaurant, dish) ->
                        DishListRow(
                            dish = dish,
                            restaurantName = restaurant.name,
                            quantityInCart = cartQtyMap[dish.id] ?: 0,
                            onDishClick = { onDishClick(restaurant.id, dish.id) },
                            onAddClick = { onAddDish(dish, restaurant) },
                            onDeltaClick = { delta -> onUpdateDishQty(dish.id, delta) }
                        )
                    }
                }
            }
        }
    }
}
