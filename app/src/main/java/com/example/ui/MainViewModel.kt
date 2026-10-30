package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.credentials.CredentialManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Bill
import com.example.data.model.ChatMessage
import com.example.data.model.Notice
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.ShopDetails
import com.example.data.model.UserProfile
import com.example.data.repository.AiModule
import com.example.data.repository.AiRepository
import com.example.data.repository.MeroBazarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProductFilter(
    val query: String = "",
    val category: Int = 0,
    val district: String = "",
    val inStockOnly: Boolean = false,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val pricePreset: String = "all", // "all", "under_500", "500_1500", "1500_5000", "above_5000", "custom"
    val sortOption: String = "popular"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repo = MeroBazarRepository(application)
    private val aiRepo = AiRepository()

    val currentUser: StateFlow<UserProfile?> = repo.currentUser
    val isNepali: StateFlow<Boolean> = repo.isNepali
    val products: StateFlow<List<Product>> = repo.products
    val shops: StateFlow<List<UserProfile>> = repo.shops
    val notices: StateFlow<List<Notice>> = repo.notices
    val orders: StateFlow<List<Order>> = repo.orders
    val cart: StateFlow<Map<String, Int>> = repo.cart
    val reviews: StateFlow<List<Review>> = repo.reviews
    val bills: StateFlow<List<Bill>> = repo.bills
    val appUpdateInfo: StateFlow<com.example.data.model.AppUpdateInfo> = repo.appUpdateInfo

    // Navigation and screen sub-navigation
    // The user requested that Registration/Sign-up is first ("सर्वप्रथम दर्ता रजिस्टर")
    private val _currentScreen = MutableStateFlow(if (repo.currentUser.value == null) "auth" else "home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedShopId = MutableStateFlow<String?>(null)
    val selectedShopId: StateFlow<String?> = _selectedShopId.asStateFlow()

    private val _selectedProductId = MutableStateFlow<String?>(null)
    val selectedProductId: StateFlow<String?> = _selectedProductId.asStateFlow()

    // Filter state
    val filterState = MutableStateFlow(ProductFilter())
    val productFilter: StateFlow<ProductFilter> = filterState.asStateFlow()
    fun setFilter(f: ProductFilter) { filterState.value = f }

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(
        repo.products,
        filterState
    ) { prods: List<Product>, filter: ProductFilter ->
        var list = prods

        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.type.lowercase().contains(q) ||
                it.shopName.lowercase().contains(q)
            }
        }

        if (filter.category > 0) {
            list = list.filter { it.cat == filter.category }
        }

        if (filter.district.isNotBlank()) {
            val shopIds = shops.value.filter { it.dist.equals(filter.district, ignoreCase = true) }.map { it.id }
            list = list.filter { it.mid in shopIds }
        }

        if (filter.inStockOnly) {
            list = list.filter { it.stock > 0 }
        }

        if (filter.minPrice != null && filter.minPrice > 0) {
            list = list.filter { it.effectivePrice >= filter.minPrice }
        }

        if (filter.maxPrice != null && filter.maxPrice > 0) {
            list = list.filter { it.effectivePrice <= filter.maxPrice }
        }

        when (filter.sortOption) {
            "price_low" -> list.sortedBy { it.effectivePrice }
            "price_high" -> list.sortedByDescending { it.effectivePrice }
            "discount" -> list.sortedByDescending { it.dis }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repo.products.value)

    fun setSearchQuery(query: String) {
        filterState.value = filterState.value.copy(query = query)
    }

    fun setCategory(category: Int) {
        filterState.value = filterState.value.copy(category = category)
    }

    fun setDistrict(district: String) {
        filterState.value = filterState.value.copy(district = district)
    }

    fun setInStockOnly(inStock: Boolean) {
        filterState.value = filterState.value.copy(inStockOnly = inStock)
    }

    fun setPricePreset(preset: String) {
        when (preset) {
            "under_500" -> filterState.value = filterState.value.copy(minPrice = 0L, maxPrice = 500L, pricePreset = preset)
            "500_1500" -> filterState.value = filterState.value.copy(minPrice = 500L, maxPrice = 1500L, pricePreset = preset)
            "1500_5000" -> filterState.value = filterState.value.copy(minPrice = 1500L, maxPrice = 5000L, pricePreset = preset)
            "above_5000" -> filterState.value = filterState.value.copy(minPrice = 5000L, maxPrice = null, pricePreset = preset)
            else -> filterState.value = filterState.value.copy(minPrice = null, maxPrice = null, pricePreset = "all")
        }
    }

    fun setPriceRange(min: Long?, max: Long?) {
        filterState.value = filterState.value.copy(minPrice = min, maxPrice = max, pricePreset = "custom")
    }

    fun setMaxPrice(price: Long?) {
        filterState.value = filterState.value.copy(maxPrice = price, pricePreset = if (price == null) "all" else "custom")
    }

    fun setFilters(cat: Int, dist: String, inStock: Boolean, maxPrice: Long?, sort: String) {
        filterState.value = filterState.value.copy(
            category = cat,
            district = dist,
            inStockOnly = inStock,
            maxPrice = maxPrice,
            sortOption = sort
        )
    }

    fun clearFilters() {
        filterState.value = ProductFilter()
    }

    // AI Shopping Assistant
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome_1",
                senderId = "ai",
                senderName = "मेरो बजार सहायक",
                text = "नमस्ते! मेरो बजारमा स्वागत छ। म तपाईंलाई सामान खोज्न, मूल्य तुलना गर्न र उत्कृष्ट पसल सिफारिस गर्न सहयोग गर्न सक्छु। के खोज्दै हुनुहुन्छ?",
                isAi = true
            )
        )
    )
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _merchantInsight = MutableStateFlow<String?>(null)
    val merchantInsight: StateFlow<String?> = _merchantInsight.asStateFlow()

    val activeAiModule = MutableStateFlow(AiModule.LOCATION)
    fun setAiModule(module: AiModule) {
        activeAiModule.value = module
    }

    // Navigation methods
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun openShop(shopId: String) {
        _selectedShopId.value = shopId
        _currentScreen.value = "shop_detail"
    }

    fun openProduct(productId: String) {
        _selectedProductId.value = productId
        _currentScreen.value = "product_detail"
    }

    fun goBack(): Boolean {
        return when (_currentScreen.value) {
            "shop_detail", "product_detail", "ai_chat", "order_tracking", "new_bill", "bill_detail", "edit_profile", "auth" -> {
                _currentScreen.value = "home"
                true
            }
            else -> false
        }
    }

    // Auth actions
    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cm = CredentialManager.create(activity)
        repo.signInWithGoogle(activity, cm, viewModelScope, onSuccess, onError)
    }

    fun signOut(activity: Activity, onDone: () -> Unit) {
        val cm = CredentialManager.create(activity)
        repo.signOut(cm, viewModelScope) {
            _currentScreen.value = "auth"
            onDone()
        }
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        signInWithEmailOrPhone(email, pass, onResult)
    }

    fun signInWithEmailOrPhone(identifier: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repo.signInWithEmailOrPhone(identifier, pass)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Sign-in failed")
            }
        }
    }

    fun signUpWithEmail(
        name: String,
        email: String,
        pass: String,
        role: String,
        phone: String,
        shopDetails: ShopDetails? = null,
        prov: String = "Bagmati",
        dist: String = "Kathmandu",
        local: String = "",
        ward: Int = 1,
        toleRoad: String = "",
        isMapTagged: Boolean = false,
        dob: String = "",
        gn: String = "Male",
        photo: String = "",
        la: Double? = null,
        lo: Double? = null,
        onResult: (Boolean, String?, UserProfile?) -> Unit
    ) {
        viewModelScope.launch {
            val res = repo.signUpWithEmail(
                name = name,
                email = email,
                pass = pass,
                role = role,
                phone = phone,
                shopDetails = shopDetails,
                prov = prov,
                dist = dist,
                local = local,
                ward = ward,
                toleRoad = toleRoad,
                isMapTagged = isMapTagged,
                dob = dob,
                gn = gn,
                photo = photo,
                la = la,
                lo = lo
            )
            if (res.isSuccess) {
                onResult(true, null, res.getOrNull())
            } else {
                onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Sign-up failed", null)
            }
        }
    }

    fun tagUserLocation(
        prov: String,
        dist: String,
        lat: Double,
        lng: Double,
        local: String = "",
        ward: Int = 1,
        toleRoad: String = ""
    ) {
        repo.tagUserLocation(prov, dist, lat, lng, local, ward, toleRoad)
        setDistrict(dist)
    }

    fun checkForAppUpdates(): Boolean = repo.checkForAppUpdates()
    fun dismissUpdateAlert() = repo.dismissUpdateAlert()
    fun setAutoUpgrade(enabled: Boolean) = repo.setAutoUpgrade(enabled)
    fun updateAppReleaseInfo(version: String, directApkUrl: String, githubReleaseUrl: String, notes: String) {
        repo.updateAppReleaseInfo(version, directApkUrl, githubReleaseUrl, notes)
    }

    fun toggleShopVerification(shopId: String) {
        repo.toggleShopVerification(shopId)
    }

    fun addNotice(type: String, title: String, body: String) {
        repo.addNotice(type, title, body)
    }

    fun deleteNotice(noticeId: String) {
        repo.deleteNotice(noticeId)
    }

    fun resetPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repo.resetPassword(email)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Password reset failed")
            }
        }
    }

    // Cart & Order
    fun addToCart(productId: String) = repo.addToCart(productId)
    fun removeFromCart(productId: String) = repo.removeFromCart(productId)
    fun updateCartQuantity(productId: String, quantity: Int) = repo.updateCartQuantity(productId, quantity)
    fun deleteCartItem(productId: String) = repo.deleteCartItem(productId)
    fun clearCart() = repo.clearCart()

    fun placeOrder(address: String, phone: String, payMode: String, onResult: (Order?) -> Unit) {
        val res = repo.placeOrder(address, phone, payMode)
        onResult(res.getOrNull())
    }

    fun updateOrderStatus(orderId: String, status: String) {
        repo.updateOrderStatus(orderId, status)
    }

    fun cancelOrder(orderId: String) {
        repo.updateOrderStatus(orderId, "Cancelled")
    }

    fun reorder(order: Order) {
        for (item in order.items) {
            for (i in 1..item.q) {
                repo.addToCart(item.pid)
            }
        }
    }

    // AI Queries (Location, Shops, Problem, Support, Analytics)
    fun sendAiMessage(userText: String, module: AiModule = activeAiModule.value) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = currentUser.value?.id ?: "user",
            senderName = currentUser.value?.name ?: "Customer",
            text = userText,
            isAi = false
        )
        val updated = _aiMessages.value.toMutableList()
        updated.add(userMsg)
        _aiMessages.value = updated
        _isAiLoading.value = true

        viewModelScope.launch {
            val result = aiRepo.getAssistance(
                userMessage = userText,
                module = module,
                catalog = repo.products.value,
                shops = repo.shops.value,
                orders = repo.orders.value,
                currentUser = currentUser.value,
                isNepali = isNepali.value
            )
            _isAiLoading.value = false
            val aiMsg = ChatMessage(
                id = "msg_${System.currentTimeMillis() + 1}",
                senderId = "ai",
                senderName = "मेरो बजार सहायक (${module.titleNe})",
                text = result.getOrElse { "माफ गर्नुहोस्, सम्पर्क हुन सकेन: ${it.localizedMessage}" },
                isAi = true
            )
            val list = _aiMessages.value.toMutableList()
            list.add(aiMsg)
            _aiMessages.value = list
        }
    }

    fun loadMerchantAiInsights(shopName: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val merchantOrders = orders.value.filter { it.muid == currentUser.value?.id }
            val totalSales = merchantOrders.sumOf { it.total }
            val topProducts = repo.products.value.filter { it.mid == currentUser.value?.id }.map { it.name }.take(5)

            val res = aiRepo.generateMerchantInsights(
                shopName = shopName,
                salesCount = merchantOrders.size,
                totalSales = totalSales,
                topProducts = topProducts,
                isNepali = isNepali.value
            )
            _isAiLoading.value = false
            _merchantInsight.value = res.getOrNull()
        }
    }

    fun toggleLanguage() = repo.toggleLanguage()

    // Khata
    fun createBill(cname: String, phone: String, items: List<OrderItem>): Bill {
        return repo.createBill(cname, phone, items)
    }

    fun addProduct(name: String, type: String, price: Long, dis: Long, stock: Int, cat: Int) {
        repo.addProduct(name, type, price, dis, stock, cat)
    }

    fun addReview(shopId: String, rating: Int, comment: String) {
        repo.addReview(shopId, rating, comment)
    }
}
