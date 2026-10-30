package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.MainViewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiShoppingAssistantScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MerchantDashboardScreen
import com.example.ui.screens.NoticesScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.PastOrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ShopDetailScreen
import com.example.ui.screens.ShopsScreen
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.MeroBazarTheme

import com.example.ui.screens.AppUpdateScreen
import com.example.ui.screens.NepalMapScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MeroBazarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MeroBazarApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun MeroBazarApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val updateInfo by viewModel.appUpdateInfo.collectAsState()
    val totalCartItems = cart.values.sum()
    val isMerchant = currentUser?.role == "m"
    val isAdmin = currentUser?.role == "a"

    // Automatic OTA Update Notification Dialog
    if (updateInfo.isUpdateAvailable) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateAlert() },
            title = {
                Text(
                    text = if (isNepali) "🚀 नयाँ अपडेट v${updateInfo.latestVersion} उपलब्ध छ!" else "New Update v${updateInfo.latestVersion} Available!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${updateInfo.releaseNotes}\n\n" + if (isNepali)
                        "अनलाइन जोडिएको छ। के तपाईं नयाँ APK सिधै डाउनलोड गरी इन्स्टल गर्न चाहनुहुन्छ?"
                    else
                        "Online connected. Do you want to download and install the latest APK?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.githubReleaseUrl))
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                        viewModel.dismissUpdateAlert()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "अहिले डाउनलोड गर्नुहोस्" else "Download APK")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissUpdateAlert() }) {
                    Text(if (isNepali) "पछि गर्ने" else "Later")
                }
            }
        )
    }

    // Back button handling
    BackHandler(enabled = currentScreen !in listOf("home", "merchant_dashboard", "admin_dashboard")) {
        viewModel.goBack()
    }

    val showBottomBar = currentScreen in listOf("home", "shops", "notices", "cart", "profile", "merchant_dashboard", "admin_dashboard", "past_orders")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    if (isAdmin) {
                        // Admin Navigation items
                        NavigationBarItem(
                            selected = currentScreen == "admin_dashboard",
                            onClick = { viewModel.navigateTo("admin_dashboard") },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                            label = { Text(if (isNepali) "एडमिन" else "Admin") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "home",
                            onClick = { viewModel.navigateTo("home") },
                            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Catalog") },
                            label = { Text(if (isNepali) "सामानहरू" else "Products") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin_products")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "shops",
                            onClick = { viewModel.navigateTo("shops") },
                            icon = { Icon(Icons.Default.Store, contentDescription = "Shops") },
                            label = { Text(if (isNepali) "पसलहरू" else "Shops") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin_shops")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "past_orders",
                            onClick = { viewModel.navigateTo("past_orders") },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                            label = { Text(if (isNepali) "अर्डरहरू" else "Orders") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin_orders")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "profile",
                            onClick = { viewModel.navigateTo("profile") },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text(if (isNepali) "प्रोफाइल" else "Profile") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_admin_profile")
                        )
                    } else if (isMerchant) {
                        // Merchant Navigation items
                        NavigationBarItem(
                            selected = currentScreen == "merchant_dashboard",
                            onClick = { viewModel.navigateTo("merchant_dashboard") },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text(if (isNepali) "ड्यासबोर्ड" else "Dashboard") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "home",
                            onClick = { viewModel.navigateTo("home") },
                            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Catalog") },
                            label = { Text(if (isNepali) "सामानहरू" else "Products") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_products")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "past_orders",
                            onClick = { viewModel.navigateTo("past_orders") },
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                            label = { Text(if (isNepali) "अर्डरहरू" else "Orders") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_orders")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "profile",
                            onClick = { viewModel.navigateTo("profile") },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text(if (isNepali) "प्रोफाइल" else "Profile") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_merchant_profile")
                        )
                    } else {
                        // Customer Navigation items
                        NavigationBarItem(
                            selected = currentScreen == "home",
                            onClick = { viewModel.navigateTo("home") },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text(if (isNepali) "गृहपृष्ठ" else "Home") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "shops",
                            onClick = { viewModel.navigateTo("shops") },
                            icon = { Icon(Icons.Default.Store, contentDescription = "Shops") },
                            label = { Text(if (isNepali) "पसलहरू" else "Shops") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_shops")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "notices",
                            onClick = { viewModel.navigateTo("notices") },
                            icon = { Icon(Icons.Default.Campaign, contentDescription = "Notices") },
                            label = { Text(if (isNepali) "सूचना" else "Notices") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_notices")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "cart",
                            onClick = { viewModel.navigateTo("cart") },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalCartItems > 0) {
                                            Badge(containerColor = CrimsonRed) {
                                                Text("$totalCartItems", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                                }
                            },
                            label = { Text(if (isNepali) "झोला" else "Cart") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_cart")
                        )
                        NavigationBarItem(
                            selected = currentScreen == "profile",
                            onClick = { viewModel.navigateTo("profile") },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text(if (isNepali) "प्रोफाइल" else "Profile") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = CrimsonRed, indicatorColor = CrimsonRed.copy(alpha = 0.15f)),
                            modifier = Modifier.testTag("nav_profile")
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onOpenAiChat = { viewModel.navigateTo("ai_chat") },
                    onOpenCart = { viewModel.navigateTo("cart") },
                    onOpenShop = { shopId -> viewModel.openShop(shopId) },
                    onOpenProduct = { prodId -> viewModel.openProduct(prodId) },
                    onOpenPastOrders = { viewModel.navigateTo("past_orders") }
                )
                "shops" -> ShopsScreen(
                    viewModel = viewModel,
                    onOpenShop = { shopId -> viewModel.openShop(shopId) }
                )
                "shop_detail" -> {
                    val shopId = viewModel.selectedShopId.collectAsState().value ?: ""
                    ShopDetailScreen(
                        shopId = shopId,
                        viewModel = viewModel,
                        onBack = { viewModel.goBack() },
                        onOpenProduct = { prodId -> viewModel.openProduct(prodId) }
                    )
                }
                "product_detail" -> {
                    val prodId = viewModel.selectedProductId.collectAsState().value ?: ""
                    ProductDetailScreen(
                        productId = prodId,
                        viewModel = viewModel,
                        onBack = { viewModel.goBack() },
                        onOpenShop = { shopId -> viewModel.openShop(shopId) }
                    )
                }
                "notices" -> NoticesScreen(
                    viewModel = viewModel
                )
                "cart" -> CartScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("home") },
                    onOrderPlaced = { viewModel.navigateTo("past_orders") }
                )
                "past_orders" -> PastOrdersScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() },
                    onNavigateToCart = { viewModel.navigateTo("cart") },
                    onBrowseProducts = { viewModel.navigateTo("home") }
                )
                "order_tracking" -> PastOrdersScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("home") },
                    onNavigateToCart = { viewModel.navigateTo("cart") },
                    onBrowseProducts = { viewModel.navigateTo("home") }
                )
                "admin_dashboard" -> AdminPanelScreen(
                    viewModel = viewModel,
                    onNavigateToCustomerMode = {
                        viewModel.repo.setRole("c")
                        viewModel.navigateTo("home")
                    },
                    onNavigateToMerchantMode = {
                        viewModel.repo.setRole("m")
                        viewModel.navigateTo("merchant_dashboard")
                    }
                )
                "profile" -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToAuth = { viewModel.navigateTo("auth") },
                    onNavigateToOrders = { viewModel.navigateTo("past_orders") }
                )
                "auth" -> AuthScreen(
                    viewModel = viewModel,
                    onAuthSuccess = {
                        val role = viewModel.currentUser.value?.role
                        when (role) {
                            "m" -> viewModel.navigateTo("merchant_dashboard")
                            "a" -> viewModel.navigateTo("admin_dashboard")
                            else -> viewModel.navigateTo("home")
                        }
                    },
                    onBack = { viewModel.navigateTo("home") },
                    onOpenNepalMap = { viewModel.navigateTo("nepal_map") }
                )
                "ai_chat" -> AiShoppingAssistantScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo("home") },
                    onOpenNepalMap = { viewModel.navigateTo("nepal_map") }
                )
                "nepal_map" -> NepalMapScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() },
                    onExploreShops = { dist ->
                        viewModel.setDistrict(dist)
                        viewModel.navigateTo("shops")
                    },
                    onExploreProducts = { dist ->
                        viewModel.setDistrict(dist)
                        viewModel.navigateTo("home")
                    }
                )
                "merchant_dashboard" -> MerchantDashboardScreen(
                    viewModel = viewModel
                )
                "app_update" -> AppUpdateScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.goBack() }
                )
                else -> HomeScreen(
                    viewModel = viewModel,
                    onOpenAiChat = { viewModel.navigateTo("ai_chat") },
                    onOpenCart = { viewModel.navigateTo("cart") },
                    onOpenShop = { shopId -> viewModel.openShop(shopId) },
                    onOpenProduct = { prodId -> viewModel.openProduct(prodId) },
                    onOpenPastOrders = { viewModel.navigateTo("past_orders") },
                    onOpenNepalMap = { viewModel.navigateTo("nepal_map") }
                )
            }
        }
    }
}
