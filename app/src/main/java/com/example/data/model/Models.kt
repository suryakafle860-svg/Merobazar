package com.example.data.model

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "c", // "c" = customer, "m" = merchant, "a" = admin
    val code: String = "", // e.g. "AB-C-45892", "AB-M-78214", "AB-A-10001"
    val dob: String = "",
    val gn: String = "Male", // "Male", "Female", "Other"
    val prov: String = "Bagmati",
    val dist: String = "Kathmandu",
    val local: String = "Kathmandu",
    val ward: Int = 10,
    val toleRoad: String = "",
    val isMapTagged: Boolean = false,
    val la: Double? = null,
    val lo: Double? = null,
    val photo: String = "",
    val wallet: Long = 0L,
    val pts: Long = 0L,
    val regDate: String = "",
    val shop: ShopDetails? = null
)

data class ShopDetails(
    val name: String = "",
    val cat: Int = 1, // 1 to 5
    val type: String = "किराना",
    val open: String = "08:00",
    val close: String = "20:00",
    val rad: Int = 5,
    val min: Long = 0L,
    val ok: Boolean = true,
    val exp: String = "2027-12-31",
    val demo: Boolean = false,
    val pan: String = "",
    val lrn: String = "",
    val doc: String = "",
    val pm: String = "eSewa",
    val ref: String = "",
    val rating: Double = 4.8,
    val reviewCount: Int = 12
)

data class AppUpdateInfo(
    val currentVersion: String = "1.0.0",
    val latestVersion: String = "1.0.1",
    val isUpdateAvailable: Boolean = false,
    val releaseNotes: String = "नयाँ दर्ता प्रमाण कार्ड, अद्यावधिक डिजाइन, सुदृढ कार्यसम्पादन तथा बग सुधारहरू।",
    val githubReleaseUrl: String = "https://github.com/aazakobazar/mero-bazar/releases",
    val directApkUrl: String = "https://github.com/aazakobazar/mero-bazar/releases/download/v1.0.1/mero-bazar.apk",
    val autoUpgradeEnabled: Boolean = true,
    val lastSyncTime: String = "भर्खरै"
)

data class Category(
    val id: Int,
    val name: String,
    val emoji: String,
    val subTypes: List<String>
)

data class Product(
    val id: String = "",
    val mid: String = "",
    val shopName: String = "",
    val name: String = "",
    val type: String = "",
    val price: Long = 0L,
    val dis: Long = 0L,
    val stock: Int = 0,
    val img: String = "",
    val cat: Int = 1
) {
    val effectivePrice: Long get() = (price - dis).coerceAtLeast(0L)
    val inStock: Boolean get() = stock > 0
}

data class Notice(
    val id: String = "",
    val mid: String = "",
    val shopName: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val date: String = "",
    val cat: Int = 5
)

data class OrderItem(
    val pid: String = "",
    val n: String = "",
    val q: Int = 1,
    val p: Long = 0L
) {
    val total: Long get() = q * p
}

data class Order(
    val id: String = "",
    val cuid: String = "",
    val cname: String = "",
    val cphone: String = "",
    val caddress: String = "",
    val muid: String = "",
    val shopName: String = "",
    val items: List<OrderItem> = emptyList(),
    val total: Long = 0L,
    val st: String = "Pending", // Pending, Accepted, Delivered, Cancelled
    val d: String = "",
    val payMode: String = "Cash on Delivery"
)

data class Review(
    val id: String = "",
    val cuid: String = "",
    val cname: String = "",
    val suid: String = "",
    val r: Int = 5,
    val t: String = "",
    val d: String = ""
)

data class Bill(
    val id: String = "",
    val mid: String = "",
    val shopName: String = "",
    val cname: String = "",
    val phone: String = "",
    val items: List<OrderItem> = emptyList(),
    val total: Long = 0L,
    val d: String = "",
    val pan: String = "",
    val lrn: String = ""
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val isAi: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
