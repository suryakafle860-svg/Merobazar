package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import com.example.data.model.OrderItem
import com.example.ui.MainViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import com.example.ui.theme.NepalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboardScreen(viewModel: MainViewModel) {
    val isNepali by viewModel.isNepali.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val merchantInsight by viewModel.merchantInsight.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showNewBillDialog by remember { mutableStateOf(false) }
    var selectedBillForView by remember { mutableStateOf<Bill?>(null) }

    // New product dialog fields
    var pName by remember { mutableStateOf("") }
    var pType by remember { mutableStateOf("किराना") }
    var pPriceStr by remember { mutableStateOf("") }
    var pDisStr by remember { mutableStateOf("0") }
    var pStockStr by remember { mutableStateOf("20") }
    var pCat by remember { mutableStateOf(1) }

    // New bill dialog fields
    var bCname by remember { mutableStateOf("") }
    var bPhone by remember { mutableStateOf("") }
    var bItemName by remember { mutableStateOf("") }
    var bItemRateStr by remember { mutableStateOf("") }
    var bItemQtyStr by remember { mutableStateOf("1") }

    val shopName = currentUser?.shop?.name ?: currentUser?.name ?: "Mero Bazar Store"
    val merchantOrders = orders.filter { it.muid == currentUser?.id || it.muid == "shop_1" }
    val todaySales = merchantOrders.sumOf { it.total }

    // --- Add Product Dialog ---
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text(if (isNepali) "नयाँ सामान थप्नुहोस्" else "Add New Product") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text(if (isNepali) "सामानको नाम" else "Product Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pType,
                        onValueChange = { pType = it },
                        label = { Text(if (isNepali) "प्रकार (Sub-category)" else "Sub-type") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pPriceStr,
                            onValueChange = { pPriceStr = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isNepali) "मूल्य (₨)" else "Price (₨)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pDisStr,
                            onValueChange = { pDisStr = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isNepali) "छुट (₨)" else "Discount") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = pStockStr,
                        onValueChange = { pStockStr = it.filter { c -> c.isDigit() } },
                        label = { Text(if (isNepali) "मौज्दात (Stock Qty)" else "Stock Units") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pr = pPriceStr.toLongOrNull() ?: 0L
                        val dis = pDisStr.toLongOrNull() ?: 0L
                        val st = pStockStr.toIntOrNull() ?: 10
                        if (pName.isNotBlank() && pr > 0) {
                            viewModel.addProduct(pName, pType, pr, dis, st, pCat)
                            pName = ""
                            pPriceStr = ""
                            showAddProductDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "थप्नुहोस्" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // --- New Bill Dialog ---
    if (showNewBillDialog) {
        AlertDialog(
            onDismissRequest = { showNewBillDialog = false },
            title = { Text(if (isNepali) "नयाँ खाता / बिल बनाउनुहोस्" else "Create Khata Bill") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bCname,
                        onValueChange = { bCname = it },
                        label = { Text(if (isNepali) "ग्राहकको नाम" else "Customer Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = bPhone,
                        onValueChange = { bPhone = it },
                        label = { Text(if (isNepali) "फोन नम्बर" else "Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    OutlinedTextField(
                        value = bItemName,
                        onValueChange = { bItemName = it },
                        label = { Text(if (isNepali) "सामान (Item Name)" else "Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bItemRateStr,
                            onValueChange = { bItemRateStr = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isNepali) "दर (Rate ₨)" else "Rate") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bItemQtyStr,
                            onValueChange = { bItemQtyStr = it.filter { c -> c.isDigit() } },
                            label = { Text(if (isNepali) "संख्या (Qty)" else "Qty") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = bItemRateStr.toLongOrNull() ?: 0L
                        val qty = bItemQtyStr.toIntOrNull() ?: 1
                        if (bItemName.isNotBlank() && rate > 0) {
                            val items = listOf(OrderItem(pid = "item_1", n = bItemName, q = qty, p = rate))
                            val bill = viewModel.createBill(bCname.ifBlank { "Customer" }, bPhone, items)
                            selectedBillForView = bill
                            bItemName = ""
                            bItemRateStr = ""
                            showNewBillDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "बिल बनाउनुहोस्" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBillDialog = false }) {
                    Text(if (isNepali) "रद्द" else "Cancel")
                }
            }
        )
    }

    // --- Bill View Dialog ---
    if (selectedBillForView != null) {
        val b = selectedBillForView!!
        AlertDialog(
            onDismissRequest = { selectedBillForView = null },
            title = { Text("🧾 ${b.id} - ${b.shopName}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("मिति (Date): ${b.d}")
                    Text("ग्राहक (Customer): ${b.cname} (${b.phone})")
                    if (b.pan.isNotBlank()) Text("PAN: ${b.pan}")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    b.items.forEach { itm ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${itm.n} × ${itm.q}")
                            Text("₨ ${itm.total}", fontWeight = FontWeight.Bold)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("कुल जम्मा (Total):", fontWeight = FontWeight.Bold)
                        Text("₨ ${b.total}", fontWeight = FontWeight.ExtraBold, color = CrimsonRed, fontSize = 18.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBillForView = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(if (isNepali) "बन्द गर्नुहोस्" else "Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 $shopName (ड्यास Venice)", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(if (isNepali) "आजको बिक्री" else "Today's Sales", style = MaterialTheme.typography.bodySmall)
                            Text("₨ $todaySales", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                            Text("${merchantOrders.size} " + if (isNepali) "अर्डरहरू" else "orders", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(if (isNepali) "खाता / बिल संख्या" else "Bills Issued", style = MaterialTheme.typography.bodySmall)
                            Text("${bills.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = NepalBlue)
                            Text("₨ ${bills.sumOf { it.total }}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Gemini AI Business Insights Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CrimsonRed)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isNepali) "AI व्यापार सल्लाहकार (Gemini)" else "Gemini Business Insights",
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonRed
                                )
                            }
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrimsonRed, strokeWidth = 2.dp)
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.loadMerchantAiInsights(shopName) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isNepali) "सुझाव लिनुहोस्" else "Get Tips", fontSize = 12.sp)
                                }
                            }
                        }

                        if (merchantInsight != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = merchantInsight ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF3E2723)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isNepali) "Gemini ले तपाईंको पसलको मौज्दात, माग र छुटको विश्लेषण गरी बिक्री बढाउन सुझाव दिन्छ।" else "Gemini will predict demand and recommend restock items.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showAddProductDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("merchant_add_product_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNepali) "सामान थप्नुहोस्" else "Add Product")
                    }

                    Button(
                        onClick = { showNewBillDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("merchant_new_bill_button")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNepali) "नयाँ बिल" else "New Bill")
                    }
                }
            }

            // Customer Orders List Section
            item {
                Text(
                    text = if (isNepali) "📦 आएका ग्राहक अर्डरहरू (${merchantOrders.size})" else "Customer Orders (${merchantOrders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (merchantOrders.isEmpty()) {
                item {
                    Text(
                        text = if (isNepali) "अहिले कुनै नयाँ अर्डर छैन।" else "No customer orders yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(merchantOrders) { order ->
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
                                Text(order.id, fontWeight = FontWeight.Bold)
                                Badge(containerColor = if (order.st == "Delivered") JadeGreen else AmberGold) {
                                    Text(order.st, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ग्राहक: ${order.cname} · 📞 ${order.cphone}", style = MaterialTheme.typography.bodyMedium)
                            Text("ठेगाना: ${order.caddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("सामान संख्या: ${order.items.size} वटा · कुल: ₨ ${order.total}", fontWeight = FontWeight.Bold, color = CrimsonRed)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (order.st == "Pending") {
                                    Button(
                                        onClick = { viewModel.updateOrderStatus(order.id, "Accepted") },
                                        colors = ButtonDefaults.buttonColors(containerColor = NepalBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isNepali) "स्वीकार (Accept)" else "Accept")
                                    }
                                }
                                if (order.st == "Accepted") {
                                    Button(
                                        onClick = { viewModel.updateOrderStatus(order.id, "Delivered") },
                                        colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isNepali) "डेलिभर भयो (Deliver)" else "Deliver")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Issued Bills List Section
            item {
                Text(
                    text = if (isNepali) "📒 जारी गरिएका बिल तथा खाता (${bills.size})" else "Issued Bills / Khata (${bills.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(bills) { b ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "🧾 ${b.id} · ${b.cname}", fontWeight = FontWeight.Bold)
                            Text(text = "${b.d} · ₨ ${b.total}", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { selectedBillForView = b },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                        ) {
                            Text(if (isNepali) "हेर्नुहोस्" else "View")
                        }
                    }
                }
            }
        }
    }
}
