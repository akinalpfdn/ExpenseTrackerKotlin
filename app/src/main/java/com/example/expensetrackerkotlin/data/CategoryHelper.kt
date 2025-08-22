package com.example.expensetrackerkotlin.data

object CategoryHelper {
    
    val subCategories = listOf(
        // Gıda ve İçecek
        ExpenseSubCategory("Restoran", ExpenseCategory.FOOD),
        ExpenseSubCategory("Market alışverişi", ExpenseCategory.FOOD),
        ExpenseSubCategory("Kafeler", ExpenseCategory.FOOD),
        
        // Konut
        ExpenseSubCategory("Kira", ExpenseCategory.HOUSING),
        ExpenseSubCategory("Aidat", ExpenseCategory.HOUSING),
        ExpenseSubCategory("Mortgage ödemesi", ExpenseCategory.HOUSING),
        ExpenseSubCategory("Elektrik faturası", ExpenseCategory.HOUSING),
        ExpenseSubCategory("Su faturası", ExpenseCategory.HOUSING),
        ExpenseSubCategory("Isınma (doğalgaz, kalorifer)", ExpenseCategory.HOUSING),
        ExpenseSubCategory("İnternet ve telefon", ExpenseCategory.HOUSING),
        ExpenseSubCategory("Diğer Faturalar", ExpenseCategory.HOUSING),
        ExpenseSubCategory("Temizlik malzemeleri", ExpenseCategory.HOUSING),
        
        // Ulaşım
        ExpenseSubCategory("Benzin/Dizel", ExpenseCategory.TRANSPORTATION),
        ExpenseSubCategory("Toplu taşıma", ExpenseCategory.TRANSPORTATION),
        ExpenseSubCategory("Araç bakımı", ExpenseCategory.TRANSPORTATION),
        ExpenseSubCategory("Oto kiralama", ExpenseCategory.TRANSPORTATION),
        ExpenseSubCategory("Taksi/Uber", ExpenseCategory.TRANSPORTATION),
        ExpenseSubCategory("Araç sigortası", ExpenseCategory.TRANSPORTATION),
        ExpenseSubCategory("MTV", ExpenseCategory.TRANSPORTATION),
        ExpenseSubCategory("Park ücretleri", ExpenseCategory.TRANSPORTATION),
        
        // Sağlık
        ExpenseSubCategory("Doktor randevusu", ExpenseCategory.HEALTH),
        ExpenseSubCategory("İlaçlar", ExpenseCategory.HEALTH),
        ExpenseSubCategory("Spor salonu üyeliği", ExpenseCategory.HEALTH),
        ExpenseSubCategory("Cilt bakım ürünleri", ExpenseCategory.HEALTH),
        ExpenseSubCategory("Diş bakımı", ExpenseCategory.HEALTH),
        
        // Eğlence
        ExpenseSubCategory("Sinema ve tiyatro", ExpenseCategory.ENTERTAINMENT),
        ExpenseSubCategory("Konser ve etkinlikler", ExpenseCategory.ENTERTAINMENT),
        ExpenseSubCategory("Abonelikler (Netflix, Spotify vb.)", ExpenseCategory.ENTERTAINMENT),
        ExpenseSubCategory("Kitaplar ve dergiler", ExpenseCategory.ENTERTAINMENT),
        ExpenseSubCategory("Seyahat ve tatil", ExpenseCategory.ENTERTAINMENT),
        ExpenseSubCategory("Oyunlar ve uygulamalar", ExpenseCategory.ENTERTAINMENT),
        
        // Eğitim
        ExpenseSubCategory("Kurs ücretleri", ExpenseCategory.EDUCATION),
        ExpenseSubCategory("Eğitim materyalleri", ExpenseCategory.EDUCATION),
        ExpenseSubCategory("Seminerler", ExpenseCategory.EDUCATION),
        ExpenseSubCategory("Online kurslar", ExpenseCategory.EDUCATION),
        
        // Alışveriş
        ExpenseSubCategory("Elektronik", ExpenseCategory.SHOPPING),
        ExpenseSubCategory("Giyim", ExpenseCategory.SHOPPING),
        ExpenseSubCategory("Ev eşyaları", ExpenseCategory.SHOPPING),
        ExpenseSubCategory("Hediyeler", ExpenseCategory.SHOPPING),
        ExpenseSubCategory("Takı ve aksesuar", ExpenseCategory.SHOPPING),
        ExpenseSubCategory("Parfüm", ExpenseCategory.SHOPPING),
        
        // Evcil Hayvan
        ExpenseSubCategory("Mama ve oyuncaklar", ExpenseCategory.PETS),
        ExpenseSubCategory("Veteriner hizmetleri", ExpenseCategory.PETS),
        ExpenseSubCategory("Evcil hayvan sigortası", ExpenseCategory.PETS),
        
        // İş
        ExpenseSubCategory("İş yemekleri", ExpenseCategory.WORK),
        ExpenseSubCategory("Ofis malzemeleri", ExpenseCategory.WORK),
        ExpenseSubCategory("İş seyahatleri", ExpenseCategory.WORK),
        ExpenseSubCategory("Eğitim ve seminerler", ExpenseCategory.WORK),
        ExpenseSubCategory("Freelance iş ödemeleri", ExpenseCategory.WORK),
        
        // Vergi
        ExpenseSubCategory("Vergi ödemeleri", ExpenseCategory.TAX),
        ExpenseSubCategory("Avukat ve danışman ücretleri", ExpenseCategory.TAX),
        
        // Bağışlar
        ExpenseSubCategory("Hayır kurumları", ExpenseCategory.DONATIONS),
        ExpenseSubCategory("Yardımlar ve bağışlar", ExpenseCategory.DONATIONS),
        ExpenseSubCategory("Çevre ve toplum projeleri", ExpenseCategory.DONATIONS),
        ExpenseSubCategory("Diğer Harcamalar", ExpenseCategory.OTHERS)
    )
    
    private val categoryMapping = mapOf(
        // Gıda
        "Restoran" to ExpenseCategory.FOOD,
        "Market alışverişi" to ExpenseCategory.FOOD,
        "Kafeler" to ExpenseCategory.FOOD,
        
        // Konut
        "Kira" to ExpenseCategory.HOUSING,
        "Aidat" to ExpenseCategory.HOUSING,
        "Mortgage ödemesi" to ExpenseCategory.HOUSING,
        "Elektrik faturası" to ExpenseCategory.HOUSING,
        "Su faturası" to ExpenseCategory.HOUSING,
        "Isınma (doğalgaz, kalorifer)" to ExpenseCategory.HOUSING,
        "İnternet ve telefon" to ExpenseCategory.HOUSING,
        "Diğer Faturalar" to ExpenseCategory.HOUSING,
        "Temizlik malzemeleri" to ExpenseCategory.HOUSING,
        
        // Ulaşım
        "Benzin/Dizel" to ExpenseCategory.TRANSPORTATION,
        "Toplu taşıma" to ExpenseCategory.TRANSPORTATION,
        "Araç bakımı" to ExpenseCategory.TRANSPORTATION,
        "Oto kiralama" to ExpenseCategory.TRANSPORTATION,
        "Taksi/Uber" to ExpenseCategory.TRANSPORTATION,
        "Araç sigortası" to ExpenseCategory.TRANSPORTATION,
        "MTV" to ExpenseCategory.TRANSPORTATION,
        "Park ücretleri" to ExpenseCategory.TRANSPORTATION,
        
        // Sağlık
        "Doktor randevusu" to ExpenseCategory.HEALTH,
        "İlaçlar" to ExpenseCategory.HEALTH,
        "Spor salonu üyeliği" to ExpenseCategory.HEALTH,
        "Cilt bakım ürünleri" to ExpenseCategory.HEALTH,
        "Diş bakımı" to ExpenseCategory.HEALTH,
        "Giyim ve aksesuar" to ExpenseCategory.HEALTH,
        
        // Eğlence
        "Sinema ve tiyatro" to ExpenseCategory.ENTERTAINMENT,
        "Konser ve etkinlikler" to ExpenseCategory.ENTERTAINMENT,
        "Abonelikler (Netflix, Spotify vb.)" to ExpenseCategory.ENTERTAINMENT,
        "Kitaplar ve dergiler" to ExpenseCategory.ENTERTAINMENT,
        "Seyahat ve tatil" to ExpenseCategory.ENTERTAINMENT,
        "Oyunlar ve uygulamalar" to ExpenseCategory.ENTERTAINMENT,
        
        // Eğitim
        "Kurs ücretleri" to ExpenseCategory.EDUCATION,
        "Eğitim materyalleri" to ExpenseCategory.EDUCATION,
        "Seminerler" to ExpenseCategory.EDUCATION,
        "Online kurslar" to ExpenseCategory.EDUCATION,
        
        // Alışveriş
        "Elektronik" to ExpenseCategory.SHOPPING,
        "Giyim" to ExpenseCategory.SHOPPING,
        "Ev eşyaları" to ExpenseCategory.SHOPPING,
        "Hediyeler" to ExpenseCategory.SHOPPING,
        "Takı ve aksesuar" to ExpenseCategory.SHOPPING,
        "Parfüm" to ExpenseCategory.SHOPPING,
        
        // Evcil Hayvan
        "Mama ve oyuncaklar" to ExpenseCategory.PETS,
        "Veteriner hizmetleri" to ExpenseCategory.PETS,
        "Evcil hayvan sigortası" to ExpenseCategory.PETS,
        
        // İş
        "İş yemekleri" to ExpenseCategory.WORK,
        "Ofis malzemeleri" to ExpenseCategory.WORK,
        "İş seyahatleri" to ExpenseCategory.WORK,
        "Eğitim ve seminerler" to ExpenseCategory.WORK,
        "Freelance iş ödemeleri" to ExpenseCategory.WORK,
        
        // Vergi
        "Vergi ödemeleri" to ExpenseCategory.TAX,
        "Avukat ve danışman ücretleri" to ExpenseCategory.TAX,
        
        // Bağışlar
        "Hayır kurumları" to ExpenseCategory.DONATIONS,
        "Yardımlar ve bağışlar" to ExpenseCategory.DONATIONS,
        "Çevre ve toplum projeleri" to ExpenseCategory.DONATIONS,

        //Diger
        "Diğer Harcamalar" to ExpenseCategory.OTHERS
    )
    
    fun getCategoryForSubCategory(subCategory: String): ExpenseCategory {
        return categoryMapping[subCategory] ?: ExpenseCategory.FOOD
    }
}