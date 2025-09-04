package com.example.expensetrackerkotlin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.*

@Entity(
    tableName = "subcategories",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class SubCategory(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val categoryId: String,
    val isDefault: Boolean = false, // Mark if it's a default subcategory
    val isCustom: Boolean = false // Mark if it's user-created
) {
    companion object {
        // Default subcategories that will be inserted on first app launch
        fun getDefaultSubCategories(): List<SubCategory> {
            return listOf(
                // Gıda ve İçecek
                SubCategory(name = "Restoran", categoryId = "food", isDefault = true),
                SubCategory(name = "Market alışverişi", categoryId = "food", isDefault = true),
                SubCategory(name = "Kafeler", categoryId = "food", isDefault = true),
                
                // Konut
                SubCategory(name = "Kira", categoryId = "housing", isDefault = true),
                SubCategory(name = "Aidat", categoryId = "housing", isDefault = true),
                SubCategory(name = "Mortgage ödemesi", categoryId = "housing", isDefault = true),
                SubCategory(name = "Elektrik faturası", categoryId = "housing", isDefault = true),
                SubCategory(name = "Su faturası", categoryId = "housing", isDefault = true),
                SubCategory(name = "Isınma (doğalgaz, kalorifer)", categoryId = "housing", isDefault = true),
                SubCategory(name = "İnternet ve telefon", categoryId = "housing", isDefault = true),
                SubCategory(name = "Diğer Faturalar", categoryId = "housing", isDefault = true),
                SubCategory(name = "Temizlik malzemeleri", categoryId = "housing", isDefault = true),
                
                // Ulaşım
                SubCategory(name = "Benzin/Dizel", categoryId = "transportation", isDefault = true),
                SubCategory(name = "Toplu taşıma", categoryId = "transportation", isDefault = true),
                SubCategory(name = "Araç bakımı", categoryId = "transportation", isDefault = true),
                SubCategory(name = "Oto kiralama", categoryId = "transportation", isDefault = true),
                SubCategory(name = "Taksi/Uber", categoryId = "transportation", isDefault = true),
                SubCategory(name = "Araç sigortası", categoryId = "transportation", isDefault = true),
                SubCategory(name = "MTV", categoryId = "transportation", isDefault = true),
                SubCategory(name = "Park ücretleri", categoryId = "transportation", isDefault = true),
                
                // Sağlık
                SubCategory(name = "Doktor randevusu", categoryId = "health", isDefault = true),
                SubCategory(name = "İlaçlar", categoryId = "health", isDefault = true),
                SubCategory(name = "Spor salonu üyeliği", categoryId = "health", isDefault = true),
                SubCategory(name = "Cilt bakım ürünleri", categoryId = "health", isDefault = true),
                SubCategory(name = "Diş bakımı", categoryId = "health", isDefault = true),
                SubCategory(name = "Giyim ve aksesuar", categoryId = "health", isDefault = true),
                
                // Eğlence
                SubCategory(name = "Sinema ve tiyatro", categoryId = "entertainment", isDefault = true),
                SubCategory(name = "Konser ve etkinlikler", categoryId = "entertainment", isDefault = true),
                SubCategory(name = "Abonelikler (Netflix, Spotify vb.)", categoryId = "entertainment", isDefault = true),
                SubCategory(name = "Kitaplar ve dergiler", categoryId = "entertainment", isDefault = true),
                SubCategory(name = "Seyahat ve tatil", categoryId = "entertainment", isDefault = true),
                SubCategory(name = "Oyunlar ve uygulamalar", categoryId = "entertainment", isDefault = true),
                
                // Eğitim
                SubCategory(name = "Kurs ücretleri", categoryId = "education", isDefault = true),
                SubCategory(name = "Eğitim materyalleri", categoryId = "education", isDefault = true),
                SubCategory(name = "Seminerler", categoryId = "education", isDefault = true),
                SubCategory(name = "Online kurslar", categoryId = "education", isDefault = true),
                
                // Alışveriş
                SubCategory(name = "Elektronik", categoryId = "shopping", isDefault = true),
                SubCategory(name = "Giyim", categoryId = "shopping", isDefault = true),
                SubCategory(name = "Ev eşyaları", categoryId = "shopping", isDefault = true),
                SubCategory(name = "Hediyeler", categoryId = "shopping", isDefault = true),
                SubCategory(name = "Takı ve aksesuar", categoryId = "shopping", isDefault = true),
                SubCategory(name = "Parfüm", categoryId = "shopping", isDefault = true),
                
                // Evcil Hayvan
                SubCategory(name = "Mama ve oyuncaklar", categoryId = "pets", isDefault = true),
                SubCategory(name = "Veteriner hizmetleri", categoryId = "pets", isDefault = true),
                SubCategory(name = "Evcil hayvan sigortası", categoryId = "pets", isDefault = true),
                
                // İş
                SubCategory(name = "İş yemekleri", categoryId = "work", isDefault = true),
                SubCategory(name = "Ofis malzemeleri", categoryId = "work", isDefault = true),
                SubCategory(name = "İş seyahatleri", categoryId = "work", isDefault = true),
                SubCategory(name = "Eğitim ve seminerler", categoryId = "work", isDefault = true),
                SubCategory(name = "Freelance iş ödemeleri", categoryId = "work", isDefault = true),
                
                // Vergi
                SubCategory(name = "Vergi ödemeleri", categoryId = "tax", isDefault = true),
                SubCategory(name = "Avukat ve danışman ücretleri", categoryId = "tax", isDefault = true),
                
                // Bağışlar
                SubCategory(name = "Hayır kurumları", categoryId = "donations", isDefault = true),
                SubCategory(name = "Yardımlar ve bağışlar", categoryId = "donations", isDefault = true),
                SubCategory(name = "Çevre ve toplum projeleri", categoryId = "donations", isDefault = true),
                
                // Diğer
                SubCategory(name = "Diğer Harcamalar", categoryId = "others", isDefault = true)
            )
        }
    }
}
