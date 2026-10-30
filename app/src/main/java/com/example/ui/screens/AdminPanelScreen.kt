package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Notice
import com.example.data.model.Order
import com.example.data.model.UserProfile
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    onNavigateToCustomerMode: () -> Unit = {},
    onNavigateToMerchantMode: () -> Unit = {}
) {
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val notices by viewModel.notices.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPublishNoticeDialog by remember { mutableStateOf(false) }

    // New Notice Form State
    var noticeTitle by remember { mutableStateOf("") }
    var noticeType by remember { mutableStateOf("सरकारी विज्ञापन") }
    var noticeBody by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val totalRevenue = orders.sumOf { it.total }
    val verifiedShopsCount = shops.count { it.shop?.lrn?.isNotBlank() == true || it.shop?.pan?.isNotBlank() == true }

    // --- Publish Notice Dialog ---
    if (showPublishNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showPublishNoticeDialog = false },
            title = {
                Text(
                    text = if (isNepali) "नयाँ सूचना / टेन्डर प्रकाशित गर्नुहोस्" else "Publish New Notice / Tender",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = noticeTitle,
                        onValueChange = { noticeTitle = it },
                        label = { Text(if (isNepali) "शीर्षक (Title) *" else "Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (isNepali) "प्रकार (Type):" else "Type:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val types = listOf(
                        "सरकारी विज्ञापन",
                        "रोजगारमूलक सूचना",
                        "व्यावसायिक विज्ञापन",
                        "गैरसरकारी विज्ञापन",
                        "अन्य महत्वपूर्ण सूचना"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        types.forEach { t ->
                            FilterChip(
                                selected = noticeType == t,
                                onClick = { noticeType = t },
                                label = { Text(t, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonRed.copy(alpha = 0.15f),
                                    selectedLabelColor = CrimsonRed
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = noticeBody,
                        onValueChange = { noticeBody = it },
                        label = { Text(if (isNepali) "विस्तृत विवरण (Details) *" else "Details *") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noticeTitle.isNotBlank() && noticeBody.isNotBlank()) {
                            viewModel.addNotice(noticeType, noticeTitle, noticeBody)
                            showPublishNoticeDialog = false
                            noticeTitle = ""
                            noticeBody = ""
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isNepali) "सूचना सफलतापूर्वक प्रकाशित गरियो!" else "Notice successfully published!"
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "प्रकाशित गर्नुहोस्" else "Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishNoticeDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = DarkCrimson,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNepali) "👑 मेरो बजार एडमिन प्यानल" else "👑 Admin Control Panel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Admin: ${currentUser?.email ?: "nplbzr@gmail.com"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Software & APK Update
                    IconButton(onClick = { viewModel.navigateTo("app_update") }) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = "APK Update",
                            tint = CrimsonRed
                        )
                    }

                    // Language Switcher
                    IconButton(onClick = { viewModel.toggleLanguage() }) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isNepali) "EN" else "नेपाली",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Admin Navigation Tabs
            val tabs = listOf(
                if (isNepali) "📊 अवलोकन" else "Overview",
                if (isNepali) "🏪 पसल प्रमाणीकरण" else "Shops KYC",
                if (isNepali) "📢 सूचना तथा टेन्डर" else "Notices",
                if (isNepali) "📦 अर्डर अडिट" else "Orders Audit"
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) CrimsonRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> AdminOverviewTab(
                    orders = orders,
                    shops = shops,
                    notices = notices,
                    totalRevenue = totalRevenue,
                    verifiedShopsCount = verifiedShopsCount,
                    isNepali = isNepali,
                    onPublishNoticeClick = { showPublishNoticeDialog = true },
                    onNavigateToCustomerMode = onNavigateToCustomerMode,
                    onNavigateToMerchantMode = onNavigateToMerchantMode
                )
                1 -> AdminShopsKycTab(
                    shops = shops,
                    isNepali = isNepali,
                    onToggleVerify = { shopId ->
                        viewModel.toggleShopVerification(shopId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "पसलको प्रमाणीकरण स्थिति परिवर्तन गरियो।" else "Shop verification status updated."
                            )
                        }
                    }
                )
                2 -> AdminNoticesTab(
                    notices = notices,
                    isNepali = isNepali,
                    onPublishNoticeClick = { showPublishNoticeDialog = true },
                    onDeleteNotice = { noticeId ->
                        viewModel.deleteNotice(noticeId)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "सूचना हटाइयो।" else "Notice deleted."
                            )
                        }
                    }
                )
                3 -> AdminOrdersAuditTab(
                    orders = orders,
                    isNepali = isNepali,
                    onUpdateStatus = { orderId, newStatus ->
                        viewModel.updateOrderStatus(orderId, newStatus)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isNepali) "अर्डर स्थिति अद्यावधिक गरियो: $newStatus" else "Order status updated to $newStatus"
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminOverviewTab(
    orders: List<Order>,
    shops: List<UserProfile>,
    notices: List<Notice>,
    totalRevenue: Long,
    verifiedShopsCount: Int,
    isNepali: Boolean,
    onPublishNoticeClick: () -> Unit,
    onNavigateToCustomerMode: () -> Unit,
    onNavigateToMerchantMode: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Platform Analytics Grid (4 Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isNepali) "प्लेटफर्म सारांश (Platform Analytics)" else "Platform Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = if (isNepali) "कुल बिक्री रकम" else "Total Revenue",
                        value = "₨ $totalRevenue",
                        icon = Icons.Default.Receipt,
                        color = JadeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = if (isNepali) "कुल अर्डरहरू" else "Total Orders",
                        value = "${orders.size}",
                        icon = Icons.Default.LocalShipping,
                        color = NepalBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = if (isNepali) "प्रमाणित पसलहरू" else "Verified Shops",
                        value = "$verifiedShopsCount / ${shops.size}",
                        icon = Icons.Default.Store,
                        color = AmberGold,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = if (isNepali) "प्रकाशित सूचना" else "Active Notices",
                        value = "${notices.size}",
                        icon = Icons.Default.Campaign,
                        color = CrimsonRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Cloud & Telemetry System Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = JadeGreen)
                        Text(
                            text = if (isNepali) "फायरबेस तथा क्लाउड स्थिति (Cloud Telemetry)" else "Firebase & Cloud Telemetry",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "• Database: Cloud Firestore (Rules Deployed & Active)", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• App Check: Attested & Protected", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Gemini AI: Firebase AI Logic (gemini-3.8-flash Active)", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Auth Provider: Google Sign-In & Registered Accounts", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Quick Admin Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNepali) "द्रुत कार्यहरू (Quick Admin Actions)" else "Quick Admin Actions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onPublishNoticeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNepali) "नयाँ सार्वजनिक सूचना / टेन्डर प्रकाशित गर्नुहोस्" else "Publish New Notice / Tender")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToCustomerMode,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isNepali) "👤 ग्राहक मोड" else "Customer Mode", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onNavigateToMerchantMode,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isNepali) "🏪 व्यापारी मोड" else "Merchant Mode", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminShopsKycTab(
    shops: List<UserProfile>,
    isNepali: Boolean,
    onToggleVerify: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isNepali) "दर्ता भएका पसलहरूको प्रमाणीकरण (Merchant KYC & Verification)" else "Registered Shops KYC & Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(shops, key = { it.id }) { shop ->
            val isVerified = shop.shop?.lrn?.isNotBlank() == true || shop.shop?.pan?.isNotBlank() == true
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = shop.shop?.name ?: shop.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Badge(containerColor = if (isVerified) JadeGreen else AmberGold) {
                            Text(
                                text = if (isVerified) (if (isNepali) "✓ प्रमाणित (Verified)" else "Verified") else (if (isNepali) "पर्खाइमा (Pending)" else "Pending"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "प्रोप्राइटर: ${shop.name} (${shop.phone})", style = MaterialTheme.typography.bodySmall)
                    Text(text = "स्थान: ${shop.dist}, ${shop.local} (वडा ${shop.ward})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "PAN/VAT: ${shop.shop?.pan.takeIf { !it.isNullOrBlank() } ?: "दर्ता हुन बाँकी"}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "दर्ता नं. (LRN): ${shop.shop?.lrn.takeIf { !it.isNullOrBlank() } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onToggleVerify(shop.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVerified) AmberGold else JadeGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isVerified)
                                (if (isNepali) "प्रमाणीकरण रद्द गर्नुहोस् (Revoke)" else "Revoke Verification")
                            else
                                (if (isNepali) "प्रमाणीकरण स्वीकृत गर्नुहोस् (Approve KYC)" else "Approve KYC & Verify")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNoticesTab(
    notices: List<Notice>,
    isNepali: Boolean,
    onPublishNoticeClick: () -> Unit,
    onDeleteNotice: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isNepali) "सूचना तथा विज्ञापनहरू (${notices.size})" else "Notices & Tenders (${notices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onPublishNoticeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isNepali) "नयाँ सूचना" else "New Notice", fontSize = 12.sp)
                }
            }
        }

        items(notices, key = { it.id }) { notice ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CrimsonRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = notice.type,
                                color = CrimsonRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteNotice(notice.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = notice.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "📅 मिति: ${notice.date}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AdminOrdersAuditTab(
    orders: List<Order>,
    isNepali: Boolean,
    onUpdateStatus: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isNepali) "सबै प्लेटफर्म अर्डरहरूको अडिट (Platform Orders Audit)" else "Platform Orders Audit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(orders, key = { it.id }) { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "#${order.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        val badgeColor = when (order.st) {
                            "Delivered" -> JadeGreen
                            "Accepted" -> NepalBlue
                            "Cancelled" -> CrimsonRed
                            else -> AmberGold
                        }
                        Badge(containerColor = badgeColor) {
                            Text(text = order.st, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "पसल: ${order.shopName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text(text = "ग्राहक: ${order.cname} (${order.cphone})", style = MaterialTheme.typography.bodySmall)
                    Text(text = "ठेगाना: ${order.caddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "सामानहरू: " + order.items.joinToString { "${it.n} × ${it.q}" }, style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "कुल: ₨ ${order.total} (${order.payMode})", fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                        Text(text = order.d, fontSize = 11.sp, color = Color.Gray)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Admin override actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (order.st != "Delivered") {
                            Button(
                                onClick = { onUpdateStatus(order.id, "Delivered") },
                                colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isNepali) "डेलिभर भयो" else "Mark Delivered", fontSize = 11.sp)
                            }
                        }
                        if (order.st != "Cancelled") {
                            OutlinedButton(
                                onClick = { onUpdateStatus(order.id, "Cancelled") },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isNepali) "रद्द गर्नुहोस्" else "Cancel Order", fontSize = 11.sp, color = CrimsonRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
