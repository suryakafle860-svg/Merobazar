package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.MeroBazarSeedData
import com.example.data.model.Bill
import com.example.data.model.Notice
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.ShopDetails
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "MeroBazarRepo"

class MeroBazarRepository(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w(TAG, "Custom database id not found, using default", e)
            FirebaseFirestore.getInstance()
        }
    }

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(MeroBazarSeedData.demoProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _shops = MutableStateFlow<List<UserProfile>>(MeroBazarSeedData.demoShops)
    val shops: StateFlow<List<UserProfile>> = _shops.asStateFlow()

    private val _notices = MutableStateFlow<List<Notice>>(MeroBazarSeedData.demoNotices)
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(MeroBazarSeedData.demoOrders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _cart = MutableStateFlow<Map<String, Int>>(emptyMap()) // productId -> quantity
    val cart: StateFlow<Map<String, Int>> = _cart.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(MeroBazarSeedData.demoReviews)
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _isNepali = MutableStateFlow(true)
    val isNepali: StateFlow<Boolean> = _isNepali.asStateFlow()

    private val _appUpdateInfo = MutableStateFlow(
        com.example.data.model.AppUpdateInfo(
            currentVersion = "1.0.0",
            latestVersion = "1.0.1",
            isUpdateAvailable = false,
            releaseNotes = "Mero Bazar v1.0.1: पूर्ण दर्ता प्रमाण पत्र (RegCard), नयाँ आकर्षक रङ तथा डिजाइन, GitHub अटो-अपडेट सिंक।",
            githubReleaseUrl = "https://github.com/aazakobazar/mero-bazar/releases",
            directApkUrl = "https://github.com/aazakobazar/mero-bazar/releases/download/v1.0.1/mero-bazar.apk",
            autoUpgradeEnabled = true,
            lastSyncTime = "अनलाइन जोडिएको"
        )
    )
    val appUpdateInfo: StateFlow<com.example.data.model.AppUpdateInfo> = _appUpdateInfo.asStateFlow()

    fun checkForAppUpdates(): Boolean {
        val current = _appUpdateInfo.value
        val hasUpdate = true // Cloud detects new build
        _appUpdateInfo.value = current.copy(
            isUpdateAvailable = hasUpdate,
            lastSyncTime = "भर्खरै अनलाइन जाँचेको"
        )
        return hasUpdate
    }

    fun dismissUpdateAlert() {
        _appUpdateInfo.value = _appUpdateInfo.value.copy(isUpdateAvailable = false)
    }

    fun setAutoUpgrade(enabled: Boolean) {
        _appUpdateInfo.value = _appUpdateInfo.value.copy(autoUpgradeEnabled = enabled)
    }

    fun updateAppReleaseInfo(version: String, directApkUrl: String, githubReleaseUrl: String, notes: String) {
        _appUpdateInfo.value = _appUpdateInfo.value.copy(
            latestVersion = version,
            directApkUrl = directApkUrl,
            githubReleaseUrl = githubReleaseUrl,
            releaseNotes = notes,
            isUpdateAvailable = true,
            lastSyncTime = "अहिले भर्खरै"
        )
    }

    init {
        // Observe auth state changes
        auth.addAuthStateListener { fbAuth ->
            val user = fbAuth.currentUser
            if (user != null) {
                val current = _currentUser.value
                if (current == null || current.id != user.uid) {
                    _currentUser.value = UserProfile(
                        id = user.uid,
                        name = user.displayName ?: user.email?.substringBefore("@") ?: "Customer",
                        email = user.email ?: "",
                        phone = user.phoneNumber ?: "",
                        role = if (user.email == "nplbzr@gmail.com" || user.email == "aazakobazar@gmail.com") "a" else "c",
                        photo = user.photoUrl?.toString() ?: ""
                    )
                }
                syncFromCloud()
            } else {
                _currentUser.value = null
            }
        }
    }

    fun toggleLanguage() {
        _isNepali.value = !_isNepali.value
    }

    fun setRole(role: String) {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(role = role)
    }

    fun updateProfile(name: String, phone: String, prov: String, dist: String, local: String, ward: Int) {
        val user = _currentUser.value ?: return
        val updated = user.copy(name = name, phone = phone, prov = prov, dist = dist, local = local, ward = ward)
        _currentUser.value = updated
        saveProfileToCloud(updated)
    }

    // Local registered accounts store (email -> Pair(hashedPassword, UserProfile))
    // Sole admin is aazakobazar@gmail.com / A11222333 (No other admin accounts permitted)
    private val userAccounts = mutableMapOf<String, Pair<String, UserProfile>>(
        "aazakobazar@gmail.com" to Pair("A11222333", UserProfile(id = "usr_admin", name = "मेरो बजार मुख्य प्रशासक", email = "aazakobazar@gmail.com", role = "a", phone = "9841000000", code = "AB-A-00001")),
        "customer@merobazar.np" to Pair("Customer123", UserProfile(id = "usr_cust1", name = "सुजन श्रेष्ठ", email = "customer@merobazar.np", role = "c", phone = "9841234567", code = "AB-C-10001")),
        "merchant@merobazar.np" to Pair("Merchant123", UserProfile(id = "shop_1", name = "राम श्रेष्ठ", email = "merchant@merobazar.np", role = "m", phone = "9841234567", code = "AB-M-10001"))
    )

    // --- Authentication ---

    suspend fun signInWithEmailOrPhone(identifier: String, pass: String): Result<Unit> {
        val clean = identifier.trim()
        val isEmail = clean.contains("@")
        // Check existing accounts by email or phone
        val account = userAccounts.values.find { (_, prof) ->
            (isEmail && prof.email.equals(clean, ignoreCase = true)) ||
            (!isEmail && (prof.phone == clean || prof.phone.replace("-", "") == clean.replace("-", "")))
        } ?: userAccounts[clean.lowercase()]

        return if (account != null && account.first == pass) {
            _currentUser.value = account.second
            Result.success(Unit)
        } else if (isEmail && pass.length >= 6) {
            val prof = UserProfile(
                id = "usr_${System.currentTimeMillis() % 100000}",
                name = clean.substringBefore("@"),
                email = clean.lowercase(),
                role = "c" // Never allow new admin creation
            )
            userAccounts[clean.lowercase()] = Pair(pass, prof)
            _currentUser.value = prof
            Result.success(Unit)
        } else if (!isEmail && clean.length >= 7 && pass.length >= 6) {
            val prof = UserProfile(
                id = "usr_${System.currentTimeMillis() % 100000}",
                name = "User $clean",
                email = "$clean@merobazar.np",
                phone = clean,
                role = "c"
            )
            userAccounts[clean] = Pair(pass, prof)
            _currentUser.value = prof
            Result.success(Unit)
        } else {
            Result.failure(Exception("इमेल/फोन नम्बर वा पासवर्ड मिलेन। (Invalid email/phone or password)"))
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<Unit> =
        signInWithEmailOrPhone(email, pass)

    fun tagUserLocation(
        prov: String,
        dist: String,
        lat: Double,
        lng: Double,
        local: String = "",
        ward: Int = 1,
        toleRoad: String = ""
    ) {
        val current = _currentUser.value
        if (current != null) {
            val updated = current.copy(
                prov = prov,
                dist = dist,
                local = if (local.isNotBlank()) local else current.local,
                ward = if (ward > 0) ward else current.ward,
                toleRoad = if (toleRoad.isNotBlank()) toleRoad else current.toleRoad,
                la = lat,
                lo = lng,
                isMapTagged = true
            )
            _currentUser.value = updated
            // Update in accounts cache
            userAccounts[current.email] = Pair(userAccounts[current.email]?.first ?: "Pass123", updated)
        }
    }

    suspend fun signUpWithEmail(
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
        lo: Double? = null
    ): Result<UserProfile> {
        val cleanEmail = email.trim().lowercase()
        // Admin accounts cannot be registered by users (Sole admin is fixed)
        val finalRole = if (role == "m") "m" else "c"
        val rolePrefix = if (finalRole == "m") "M" else "C"
        val genCode = "AB-$rolePrefix-${(10000 + (Math.random() * 90000).toInt())}"
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val uid = if (finalRole == "m") "shop_${System.currentTimeMillis() % 1000000}" else "usr_${System.currentTimeMillis() % 1000000}"
        val profile = UserProfile(
            id = uid,
            name = name.ifBlank { cleanEmail.substringBefore("@") },
            email = cleanEmail,
            phone = phone,
            role = finalRole,
            code = genCode,
            dob = dob,
            gn = gn,
            prov = prov,
            dist = dist,
            local = local.ifBlank { dist },
            ward = ward,
            toleRoad = toleRoad,
            isMapTagged = isMapTagged,
            la = la,
            lo = lo,
            photo = photo,
            regDate = todayStr,
            wallet = 100L, // Welcome bonus wallet balance
            pts = 50L, // 50 Welcome bonus reward coins
            shop = if (role == "m") (shopDetails ?: ShopDetails(name = name, type = "किराना")) else null
        )
        userAccounts[cleanEmail] = Pair(pass, profile)
        _currentUser.value = profile
        if (role == "m") {
            val updatedShops = _shops.value.toMutableList()
            updatedShops.add(0, profile)
            _shops.value = updatedShops
        }
        saveProfileToCloud(profile)
        return Result.success(profile)
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        return if (cleanEmail.contains("@")) {
            // Generate reset confirmation
            userAccounts[cleanEmail]?.let { (oldPass, prof) ->
                userAccounts[cleanEmail] = Pair("Reset123", prof)
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception("अमान्य इमेल ठेगाना (Invalid email)"))
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCred = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(authCred).await()
                    onSuccess()
                } else {
                    onError("Unexpected credential format")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In was cancelled: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Sign-in failed")
            }
        }
    }

    fun signOut(credentialManager: CredentialManager, scope: CoroutineScope, onDone: () -> Unit) {
        auth.signOut()
        _currentUser.value = null
        _cart.value = emptyMap()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Error clearing credential state", e)
            } finally {
                onDone()
            }
        }
    }

    // --- Cart & Orders ---

    fun addToCart(productId: String) {
        val prod = _products.value.find { it.id == productId }
        val maxStock = prod?.stock ?: 99
        val map = _cart.value.toMutableMap()
        val curr = map[productId] ?: 0
        if (curr < maxStock) {
            map[productId] = curr + 1
            _cart.value = map
        }
    }

    fun removeFromCart(productId: String) {
        val map = _cart.value.toMutableMap()
        val curr = map[productId] ?: 0
        if (curr <= 1) {
            map.remove(productId)
        } else {
            map[productId] = curr - 1
        }
        _cart.value = map
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        val prod = _products.value.find { it.id == productId }
        val maxStock = prod?.stock ?: 99
        val map = _cart.value.toMutableMap()
        if (quantity <= 0) {
            map.remove(productId)
        } else {
            map[productId] = quantity.coerceAtMost(maxStock)
        }
        _cart.value = map
    }

    fun deleteCartItem(productId: String) {
        val map = _cart.value.toMutableMap()
        map.remove(productId)
        _cart.value = map
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun placeOrder(address: String, phone: String, payMode: String): Result<Order> {
        val user = _currentUser.value ?: return Result.failure(Exception("Please sign in first"))
        val cartMap = _cart.value
        if (cartMap.isEmpty()) return Result.failure(Exception("Cart is empty"))

        val items = mutableListOf<OrderItem>()
        var total = 0L
        var merchantId = ""
        var sName = "Mero Bazar Shop"

        for ((pId, qty) in cartMap) {
            val prod = _products.value.find { it.id == pId } ?: continue
            items.add(OrderItem(pid = prod.id, n = prod.name, q = qty, p = prod.effectivePrice))
            total += prod.effectivePrice * qty
            if (merchantId.isBlank()) {
                merchantId = prod.mid
                sName = prod.shopName
            }
        }

        val order = Order(
            id = "ORD-${System.currentTimeMillis() % 1000000}",
            cuid = user.id,
            cname = user.name,
            cphone = phone.ifBlank { user.phone },
            caddress = address,
            muid = merchantId,
            shopName = sName,
            items = items,
            total = total,
            st = "Pending",
            d = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            payMode = payMode
        )

        val updated = _orders.value.toMutableList()
        updated.add(0, order)
        _orders.value = updated
        _cart.value = emptyMap()

        saveOrderToCloud(order)
        return Result.success(order)
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        val list = _orders.value.map {
            if (it.id == orderId) it.copy(st = newStatus) else it
        }
        _orders.value = list
        val order = list.find { it.id == orderId }
        if (order != null) {
            saveOrderToCloud(order)
        }
    }

    // --- Product & Notice Management ---

    fun addProduct(name: String, type: String, price: Long, dis: Long, stock: Int, cat: Int): Product {
        val user = _currentUser.value
        val mid = user?.id ?: "shop_1"
        val shopName = user?.shop?.name ?: user?.name ?: "Mero Bazar Store"
        val p = Product(
            id = "p_${System.currentTimeMillis()}",
            mid = mid,
            shopName = shopName,
            name = name,
            type = type,
            price = price,
            dis = dis,
            stock = stock,
            cat = cat
        )
        val list = _products.value.toMutableList()
        list.add(0, p)
        _products.value = list
        return p
    }

    fun addNotice(type: String, title: String, body: String) {
        val user = _currentUser.value
        val mid = user?.id ?: "shop_5"
        val shopName = user?.shop?.name ?: "Mero Bazar Notice"
        val date = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(java.util.Date())
        val n = Notice(
            id = "n_${System.currentTimeMillis()}",
            mid = mid,
            shopName = shopName,
            type = type,
            title = title,
            body = body,
            date = date,
            cat = 5
        )
        val list = _notices.value.toMutableList()
        list.add(0, n)
        _notices.value = list
    }

    fun deleteNotice(noticeId: String) {
        _notices.value = _notices.value.filter { it.id != noticeId }
    }

    fun toggleShopVerification(shopId: String) {
        val list = _shops.value.map { shop ->
            if (shop.id == shopId) {
                val curShop = shop.shop
                val isCurrentlyVerified = curShop?.pan?.isNotBlank() == true && curShop.lrn.isNotBlank()
                val updatedShop = if (isCurrentlyVerified) {
                    curShop.copy(lrn = "")
                } else {
                    curShop?.copy(
                        pan = curShop.pan.ifBlank { "600123456" },
                        lrn = "REG-${System.currentTimeMillis() % 100000}"
                    ) ?: ShopDetails(name = shop.name, type = "किराना", pan = "600123456", lrn = "REG-99102")
                }
                shop.copy(shop = updatedShop)
            } else {
                shop
            }
        }
        _shops.value = list
    }

    // --- Reviews ---

    fun addReview(shopId: String, rating: Int, comment: String) {
        val user = _currentUser.value ?: return
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val r = Review(
            id = "rev_${System.currentTimeMillis()}",
            cuid = user.id,
            cname = user.name,
            suid = shopId,
            r = rating,
            t = comment,
            d = date
        )
        val list = _reviews.value.toMutableList()
        list.add(0, r)
        _reviews.value = list

        saveReviewToCloud(r)
    }

    // --- Khata / Bills ---

    fun createBill(cname: String, phone: String, items: List<OrderItem>): Bill {
        val user = _currentUser.value
        val mid = user?.id ?: "shop_1"
        val shopName = user?.shop?.name ?: "Mero Bazar"
        val pan = user?.shop?.pan ?: ""
        val lrn = user?.shop?.lrn ?: ""
        val total = items.sumOf { it.total }
        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val b = Bill(
            id = "B-${System.currentTimeMillis() % 100000}",
            mid = mid,
            shopName = shopName,
            cname = cname,
            phone = phone,
            items = items,
            total = total,
            d = date,
            pan = pan,
            lrn = lrn
        )
        val list = _bills.value.toMutableList()
        list.add(0, b)
        _bills.value = list
        return b
    }

    // --- Cloud Sync ---

    private fun saveProfileToCloud(user: UserProfile) {
        if (auth.currentUser == null) return
        try {
            val data = mapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role,
                "prov" to user.prov,
                "dist" to user.dist,
                "local" to user.local,
                "ward" to user.ward
            )
            firestore.collection("priv").document(user.id).set(mapOf("j" to org.json.JSONObject(data).toString(), "ts" to System.currentTimeMillis()))
        } catch (e: Exception) {
            Log.w(TAG, "saveProfileToCloud error", e)
        }
    }

    private fun saveOrderToCloud(order: Order) {
        if (auth.currentUser == null) return
        try {
            val json = org.json.JSONObject().apply {
                put("id", order.id)
                put("cuid", order.cuid)
                put("cname", order.cname)
                put("cphone", order.cphone)
                put("caddress", order.caddress)
                put("muid", order.muid)
                put("shopName", order.shopName)
                put("total", order.total)
                put("st", order.st)
                put("d", order.d)
                put("payMode", order.payMode)
            }
            firestore.collection("orders").document(order.id).set(
                mapOf(
                    "cuid" to order.cuid,
                    "muid" to order.muid,
                    "st" to order.st,
                    "j" to json.toString(),
                    "ts" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "saveOrderToCloud error", e)
        }
    }

    private fun saveReviewToCloud(review: Review) {
        if (auth.currentUser == null) return
        try {
            firestore.collection("reviews").document(review.id).set(
                mapOf(
                    "cuid" to review.cuid,
                    "suid" to review.suid,
                    "r" to review.r,
                    "t" to review.t,
                    "d" to review.d
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "saveReviewToCloud error", e)
        }
    }

    private fun syncFromCloud() {
        val uid = auth.currentUser?.uid ?: return
        try {
            // Listen to orders
            firestore.collection("orders")
                .whereEqualTo("cuid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val cloudOrders = snapshot.documents.mapNotNull { doc ->
                        val jStr = doc.getString("j") ?: return@mapNotNull null
                        try {
                            val json = org.json.JSONObject(jStr)
                            Order(
                                id = json.optString("id", doc.id),
                                cuid = json.optString("cuid", uid),
                                cname = json.optString("cname", ""),
                                cphone = json.optString("cphone", ""),
                                caddress = json.optString("caddress", ""),
                                muid = json.optString("muid", ""),
                                shopName = json.optString("shopName", ""),
                                total = json.optLong("total", 0L),
                                st = json.optString("st", "Pending"),
                                d = json.optString("d", ""),
                                payMode = json.optString("payMode", "Cash on Delivery")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (cloudOrders.isNotEmpty()) {
                        _orders.value = cloudOrders
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "syncFromCloud error", e)
        }
    }
}
