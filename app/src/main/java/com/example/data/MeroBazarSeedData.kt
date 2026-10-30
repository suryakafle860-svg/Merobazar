package com.example.data

import com.example.data.model.Category
import com.example.data.model.Notice
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.ShopDetails
import com.example.data.model.UserProfile

object MeroBazarSeedData {

    val categories = listOf(
        Category(
            id = 1,
            name = "आधारभूत तथा अत्यावश्यक",
            emoji = "🧺",
            subTypes = listOf("लत्ता कपडा", "खाद्यान्न", "किराना", "सब्जी तथा फलफुल", "शिक्षा", "स्वास्थ्य", "फर्निचर", "घरायसी सामग्री", "खाजा तथा खाना", "गिफ्ट तथा सजावट", "दैनिक प्रयोग सामग्री")
        ),
        Category(
            id = 2,
            name = "निर्माण तथा उद्योग",
            emoji = "🏗️",
            subTypes = listOf("हार्डवेयर सामग्री", "हार्डवेयर सम्बन्धी व्यवसाय", "विभिन्न उद्योग तथा व्यावसायिक", "स्थानीय उत्पादन")
        ),
        Category(
            id = 3,
            name = "इलेक्ट्रिक तथा अटोमेकानिकल",
            emoji = "⚡",
            subTypes = listOf("सवारी साधन तथा पाटपुर्जा", "विद्युतीय पसल", "घरायसी इलेक्ट्रिक सामग्री", "अन्य इलेक्ट्रिक सामग्री")
        ),
        Category(
            id = 4,
            name = "अन्य",
            emoji = "🧩",
            subTypes = listOf("बैंक तथा वित्तीय स्रोत", "बहुउद्देश्य सामग्री", "अन्य सामग्री")
        ),
        Category(
            id = 5,
            name = "विज्ञापन तथा सूचना",
            emoji = "📢",
            subTypes = listOf("सरकारी विज्ञापन", "गैरसरकारी विज्ञापन", "व्यावसायिक विज्ञापन", "रोजगारमूलक सूचना", "अन्य महत्वपूर्ण सूचना")
        )
    )

    val provinces = mapOf(
        "Bagmati" to listOf("Kathmandu", "Bhaktapur", "Lalitpur", "Nuwakot", "Rasuwa", "Dhading", "Makwanpur", "Ramechhap", "Sindhuli", "Sindhupalchok", "Kavrepalanchok", "Dolakha", "Chitwan"),
        "Gandaki" to listOf("Gorkha", "Lamjung", "Tanahun", "Kaski", "Manang", "Mustang", "Myagdi", "Baglung", "Parbat", "Syangja", "Nawalpur"),
        "Koshi" to listOf("Taplejung", "Panchthar", "Ilam", "Sankhuwasabha", "Terhathum", "Dhankuta", "Bhojpur", "Solukhumbu", "Okhaldhunga", "Khotang", "Udayapur", "Sunsari", "Morang", "Jhapa"),
        "Madhesh" to listOf("Saptari", "Siraha", "Dhanusha", "Mahottari", "Sarlahi", "Rautahat", "Bara", "Parsa"),
        "Lumbini" to listOf("Rupandehi", "Kapilvastu", "Parasi", "Arghakhanchi", "Gulmi", "Palpa", "Banke", "Bardiya", "Dang", "Rolpa", "Pyuthan", "Rukum East"),
        "Karnali" to listOf("Surkhet", "Jajarkot", "Dolpa", "Humla", "Jumla", "Mugu", "Kalikot", "Rukum West", "Salyan", "Dailekh"),
        "Sudurpashchim" to listOf("Kailali", "Kanchanpur", "Dadeldhura", "Baitadi", "Doti", "Achham", "Bajhang", "Bajura", "Darchula")
    )

    val demoShops = listOf(
        UserProfile(
            id = "shop_1",
            name = "राम श्रेष्ठ",
            email = "kirana.demo@merobazar.np",
            phone = "9841234567",
            role = "m",
            prov = "Bagmati",
            dist = "Kathmandu",
            local = "Kathmandu",
            ward = 10,
            shop = ShopDetails(
                name = "डेमो किराना पसल",
                cat = 1,
                type = "किराना",
                open = "06:00",
                close = "21:00",
                rad = 10,
                min = 200,
                ok = true,
                demo = true,
                pan = "102938475",
                lrn = "KTM-304/2080",
                rating = 4.9,
                reviewCount = 48
            )
        ),
        UserProfile(
            id = "shop_2",
            name = "सुरेश गुरुङ",
            email = "hardware.demo@merobazar.np",
            phone = "9856012345",
            role = "m",
            prov = "Gandaki",
            dist = "Kaski",
            local = "Pokhara",
            ward = 8,
            shop = ShopDetails(
                name = "डेमो हार्डवेयर",
                cat = 2,
                type = "हार्डवेयर सामग्री",
                open = "08:00",
                close = "19:00",
                rad = 15,
                min = 500,
                ok = true,
                demo = true,
                pan = "203948576",
                lrn = "PKR-882/2079",
                rating = 4.7,
                reviewCount = 31
            )
        ),
        UserProfile(
            id = "shop_3",
            name = "विनोद राजवंशी",
            email = "electric.demo@merobazar.np",
            phone = "9804012345",
            role = "m",
            prov = "Koshi",
            dist = "Morang",
            local = "Biratnagar",
            ward = 4,
            shop = ShopDetails(
                name = "डेमो इलेक्ट्रिक सेन्टर",
                cat = 3,
                type = "विद्युतीय पसल",
                open = "09:00",
                close = "20:00",
                rad = 12,
                min = 300,
                ok = true,
                demo = true,
                pan = "304958671",
                lrn = "BRT-114/2081",
                rating = 4.8,
                reviewCount = 26
            )
        ),
        UserProfile(
            id = "shop_4",
            name = "गीता अधिकारी",
            email = "multi.demo@merobazar.np",
            phone = "9845012345",
            role = "m",
            prov = "Bagmati",
            dist = "Chitwan",
            local = "Bharatpur",
            ward = 6,
            shop = ShopDetails(
                name = "डेमो बहुउद्देश्य पसल",
                cat = 4,
                type = "बहुउद्देश्य सामग्री",
                open = "08:30",
                close = "20:00",
                rad = 10,
                min = 250,
                ok = true,
                demo = true,
                pan = "405968712",
                lrn = "BHP-521/2080",
                rating = 4.6,
                reviewCount = 19
            )
        ),
        UserProfile(
            id = "shop_5",
            name = "प्रकाश जोशी",
            email = "notice.demo@merobazar.np",
            phone = "9848012345",
            role = "m",
            prov = "Sudurpashchim",
            dist = "Kailali",
            local = "Dhangadhi",
            ward = 2,
            shop = ShopDetails(
                name = "डेमो सूचना केन्द्र",
                cat = 5,
                type = "अन्य महत्वपूर्ण सूचना",
                open = "10:00",
                close = "17:00",
                rad = 20,
                min = 0,
                ok = true,
                demo = true,
                pan = "506978123",
                lrn = "DGD-099/2081",
                rating = 5.0,
                reviewCount = 42
            )
        )
    )

    val demoProducts = listOf(
        // Kirana products
        Product(id = "p_1_1", mid = "shop_1", shopName = "डेमो किराना पसल", name = "बासमती चामल (५ केजी)", type = "किराना", price = 650, dis = 50, stock = 40, cat = 1),
        Product(id = "p_1_2", mid = "shop_1", shopName = "डेमो किराना पसल", name = "मुसुरो दाल (१ केजी)", type = "किराना", price = 150, dis = 10, stock = 60, cat = 1),
        Product(id = "p_1_3", mid = "shop_1", shopName = "डेमो किराना पसल", name = "सरसोंको तेल (१ लिटर)", type = "किराना", price = 240, dis = 20, stock = 30, cat = 1),
        Product(id = "p_1_4", mid = "shop_1", shopName = "डेमो किराना पसल", name = "ताजा तरकारी सेट (अर्गानिक)", type = "सब्जी तथा फलफुल", price = 180, dis = 0, stock = 25, cat = 1),
        Product(id = "p_1_5", mid = "shop_1", shopName = "डेमो किराना पसल", name = "स्कुल कापी (१० वटा बन्डल)", type = "शिक्षा", price = 400, dis = 40, stock = 50, cat = 1),

        // Hardware products
        Product(id = "p_2_1", mid = "shop_2", shopName = "डेमो हार्डवेयर", name = "सिमेन्ट (५० केजी - OPC)", type = "हार्डवेयर सामग्री", price = 1250, dis = 50, stock = 100, cat = 2),
        Product(id = "p_2_2", mid = "shop_2", shopName = "डेमो हार्डवेयर", name = "फलामे छड (१ केजी)", type = "हार्डवेयर सामग्री", price = 140, dis = 5, stock = 500, cat = 2),
        Product(id = "p_2_3", mid = "shop_2", shopName = "डेमो हार्डवेयर", name = "वाटरप्रुफ रंग (४ लिटर)", type = "हार्डवेयर सामग्री", price = 2200, dis = 200, stock = 20, cat = 2),
        Product(id = "p_2_4", mid = "shop_2", shopName = "डेमो हार्डवेयर", name = "पीभिसी पाइप (१ मिटर)", type = "हार्डवेयर सामग्री", price = 180, dis = 0, stock = 80, cat = 2),
        Product(id = "p_2_5", mid = "shop_2", shopName = "डेमो हार्डवेयर", name = "औजार बाकस सेट (Tools Box)", type = "हार्डवेयर सामग्री", price = 1500, dis = 150, stock = 15, cat = 2),

        // Electric products
        Product(id = "p_3_1", mid = "shop_3", shopName = "डेमो इलेक्ट्रिक सेन्टर", name = "एलइडी बल्ब १२ वाट", type = "विद्युतीय पसल", price = 220, dis = 20, stock = 100, cat = 3),
        Product(id = "p_3_2", mid = "shop_3", shopName = "डेमो इलेक्ट्रिक सेन्टर", name = "कपर तार (१ मिटर)", type = "घरायसी इलेक्ट्रिक सामग्री", price = 60, dis = 0, stock = 300, cat = 3),
        Product(id = "p_3_3", mid = "shop_3", shopName = "डेमो इलेक्ट्रिक सेन्टर", name = "मोटरसाइकल ब्याट्री १२V", type = "सवारी साधन तथा पाटपुर्जा", price = 3200, dis = 300, stock = 10, cat = 3),
        Product(id = "p_3_4", mid = "shop_3", shopName = "डेमो इलेक्ट्रिक सेन्टर", name = "हेलमेट (ISI प्रमाणित)", type = "सवारी साधन तथा पाटपुर्जा", price = 1800, dis = 200, stock = 25, cat = 3),
        Product(id = "p_3_5", mid = "shop_3", shopName = "डेमो इलेक्ट्रिक सेन्टर", name = "इन्जिन अयल १ लिटर 4T", type = "सवारी साधन तथा पाटपुर्जा", price = 650, dis = 50, stock = 40, cat = 3),

        // Multi products
        Product(id = "p_4_1", mid = "shop_4", shopName = "डेमो बहुउद्देश्य पसल", name = "सोलार इमर्जेन्सी लाइट", type = "बहुउद्देश्य सामग्री", price = 1200, dis = 100, stock = 30, cat = 4),
        Product(id = "p_4_2", mid = "shop_4", shopName = "डेमो बहुउद्देश्य पसल", name = "फास्ट मोबाइल च्यार्जर (Type C)", type = "बहुउद्देश्य सामग्री", price = 450, dis = 50, stock = 60, cat = 4),
        Product(id = "p_4_3", mid = "shop_4", shopName = "डेमो बहुउद्देश्य पसल", name = "वाटरप्रुफ ट्राभल ब्याग", type = "बहुउद्देश्य सामग्री", price = 1500, dis = 200, stock = 20, cat = 4),
        Product(id = "p_4_4", mid = "shop_4", shopName = "डेमो बहुउद्देश्य पसल", name = "स्टेनलेस स्टील फ्लास्क (१ लिटर)", type = "बहुउद्देश्य सामग्री", price = 700, dis = 0, stock = 35, cat = 4),
        Product(id = "p_4_5", mid = "shop_4", shopName = "डेमो बहुउद्देश्य पसल", name = "पावर बैंक १०००० mAh", type = "बहुउद्देश्य सामग्री", price = 1800, dis = 300, stock = 15, cat = 4)
    )

    val demoNotices = listOf(
        Notice(id = "n_5_1", mid = "shop_5", shopName = "डेमो सूचना केन्द्र", type = "सरकारी विज्ञापन", title = "नगरपालिकामा निःशुल्क सिलाइ तालिम", body = "नगरपालिकामा महिला सशक्तीकरणका लागि निःशुल्क ३ महिने सिलाइ कटाइ तालिम सुरु हुँदैछ। आवेदन दिनुहोस्।", date = "२०८१/०७/१२", cat = 5),
        Notice(id = "n_5_2", mid = "shop_5", shopName = "डेमो सूचना केन्द्र", type = "रोजगारमूलक सूचना", title = "सेल्स सहयोगी ५ जना आवश्यक", body = "हाम्रो काठमाडौं र पोखरा शाखामा ५ जना ऊर्जावान सेल्स सहयोगी चाहिएको छ। आकर्षक तलब र भत्ता।", date = "२०८१/०७/१०", cat = 5),
        Notice(id = "n_5_3", mid = "shop_5", shopName = "डेमो सूचना केन्द्र", type = "व्यावसायिक विज्ञापन", title = "दशैँ-तिहार विशेष छुट अफर", body = "सबै घरायसी सामान तथा किरानामा १५% सम्मको भारी छुट! सीमित अवधिका लागि मात्र।", date = "२०८१/०७/०८", cat = 5),
        Notice(id = "n_5_4", mid = "shop_5", shopName = "डेमो सूचना केन्द्र", type = "गैरसरकारी विज्ञापन", title = "निःशुल्क आँखा तथा स्वास्थ्य शिविर", body = "यस शनिबार बिहान ८ बजेदेखि वडा प्राङ्गणमा विशेषज्ञ डाक्टरद्वारा स्वास्थ्य जाँच गरिनेछ।", date = "२०८१/०७/०५", cat = 5),
        Notice(id = "n_5_5", mid = "shop_5", shopName = "डेमो सूचना केन्द्र", type = "अन्य महत्वपूर्ण सूचना", title = "वडा कार्यालयको सेवा समय परिवर्तन", body = "नागरिकहरूको सुविधालाई ध्यानमा राखी वडा कार्यालय अब बिहान १०:०० देखि साँझ ५:०० बजेसम्म खुला रहनेछ।", date = "२०८१/०७/०२", cat = 5)
    )

    val demoReviews = listOf(
        Review(id = "r_1", cuid = "cust_1", cname = "अनिल पौडेल", suid = "shop_1", r = 5, t = "सामान निकै ताजा र डेलिभरी पनि छिटो भयो। बासमती चामल निकै राम्रो छ।", d = "2026-10-25"),
        Review(id = "r_2", cuid = "cust_2", cname = "मनिषा थापा", suid = "shop_1", r = 5, t = "दाल र तेल राम्रो मूल्यमा पाइयो। धन्यवाद मेरो बजार!", d = "2026-10-26"),
        Review(id = "r_3", cuid = "cust_3", cname = "राजेश शर्मा", suid = "shop_2", r = 4, t = "सिमेन्ट र छडको गुणस्तर राम्रो छ। समयमै आइपुग्यो।", d = "2026-10-27")
    )

    val demoOrders = listOf(
        Order(
            id = "ORD-849201",
            cuid = "usr_cust1",
            cname = "सुजन श्रेष्ठ",
            cphone = "9841234567",
            caddress = "न्यूरोड, काठमाडौं (वडा २२)",
            muid = "shop_1",
            shopName = "डेमो किराना पसल",
            items = listOf(
                OrderItem(pid = "p_1_1", n = "बासमती चामल (५ केजी)", q = 1, p = 600),
                OrderItem(pid = "p_1_2", n = "मुसुरो दाल (१ केजी)", q = 2, p = 140)
            ),
            total = 880,
            st = "Pending",
            d = "2026-10-29 14:30",
            payMode = "Cash on Delivery"
        ),
        Order(
            id = "ORD-739105",
            cuid = "usr_cust1",
            cname = "सुजन श्रेष्ठ",
            cphone = "9841234567",
            caddress = "लेकसाइड, पोखरा (वडा ६)",
            muid = "shop_2",
            shopName = "डेमो हार्डवेयर",
            items = listOf(
                OrderItem(pid = "p_2_5", n = "औजार बाकस सेट (Tools Box)", q = 1, p = 1350),
                OrderItem(pid = "p_2_3", n = "वाटरप्रुफ रंग (४ लिटर)", q = 1, p = 2000)
            ),
            total = 3350,
            st = "Delivered",
            d = "2026-10-27 11:15",
            payMode = "eSewa / Khalti / Fonepay"
        ),
        Order(
            id = "ORD-621890",
            cuid = "usr_cust1",
            cname = "सुजन श्रेष्ठ",
            cphone = "9841234567",
            caddress = "झाम्सिखेल, ललितपुर (वडा ३)",
            muid = "shop_4",
            shopName = "डेमो बहुउद्देश्य पसल",
            items = listOf(
                OrderItem(pid = "p_4_5", n = "पावर बैंक १०००० mAh", q = 1, p = 1500)
            ),
            total = 1500,
            st = "Cancelled",
            d = "2026-10-24 09:40",
            payMode = "Cash on Delivery"
        ),
        Order(
            id = "ORD-519284",
            cuid = "usr_cust1",
            cname = "सुजन श्रेष्ठ",
            cphone = "9841234567",
            caddress = "कालीमाटी, काठमाडौं (वडा १४)",
            muid = "shop_1",
            shopName = "डेमो किराना पसल",
            items = listOf(
                OrderItem(pid = "p_1_3", n = "सरसोंको तेल (१ लिटर)", q = 3, p = 220),
                OrderItem(pid = "p_1_5", n = "स्कुल कापी (१० वटा बन्डल)", q = 1, p = 360)
            ),
            total = 1020,
            st = "Delivered",
            d = "2026-10-20 16:50",
            payMode = "Bank Transfer"
        )
    )
}
