package com.example.expensetrackerkotlin.data

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.*
import com.example.expensetrackerkotlin.R

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
        fun getDefaultSubCategories(context: Context): List<SubCategory> {
            return listOf(
                // Gıda ve İçecek
                SubCategory(name = context.getString(R.string.subcategory_restaurant), categoryId = "food", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_kitchen_shopping), categoryId = "food", isDefault = true),
                
                // Konut
                SubCategory(name = context.getString(R.string.subcategory_rent), categoryId = "housing", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_dues), categoryId = "housing", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_mortgage), categoryId = "housing", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_electricity), categoryId = "housing", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_water), categoryId = "housing", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_heating), categoryId = "housing", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_internet_phone), categoryId = "housing", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_other_bills), categoryId = "housing", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_general_shopping), categoryId = "housing", isDefault = true),
                
                // Ulaşım
                SubCategory(name = context.getString(R.string.subcategory_fuel), categoryId = "transportation", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_public_transport), categoryId = "transportation", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_car_maintenance), categoryId = "transportation", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_car_rental), categoryId = "transportation", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_taxi_uber), categoryId = "transportation", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_car_insurance), categoryId = "transportation", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_mtv), categoryId = "transportation", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_parking_fees), categoryId = "transportation", isDefault = true),
                
                // Sağlık
                SubCategory(name = context.getString(R.string.subcategory_doctor_appointment), categoryId = "health", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_medicines), categoryId = "health", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_gym_membership), categoryId = "health", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_cosmetics), categoryId = "health", isDefault = true),
                
                // Eğlence
                SubCategory(name = context.getString(R.string.subcategory_cinema_theater), categoryId = "entertainment", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_concerts_events), categoryId = "entertainment", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_subscriptions), categoryId = "entertainment", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_books_magazines), categoryId = "entertainment", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_travel_vacation), categoryId = "entertainment", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_games_apps), categoryId = "entertainment", isDefault = true),
                
                // Eğitim
                SubCategory(name = context.getString(R.string.subcategory_course_fees), categoryId = "education", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_education_materials), categoryId = "education", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_seminars), categoryId = "education", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_online_courses), categoryId = "education", isDefault = true),
                
                // Alışveriş
                SubCategory(name = context.getString(R.string.subcategory_electronics), categoryId = "shopping", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_clothing), categoryId = "shopping", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_home_goods), categoryId = "shopping", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_gifts), categoryId = "shopping", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_perfume), categoryId = "shopping", isDefault = true),
                
                // Evcil Hayvan
                SubCategory(name = context.getString(R.string.subcategory_pet_food_toys), categoryId = "pets", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_vet_services), categoryId = "pets", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_pet_insurance), categoryId = "pets", isDefault = true),
                
                // İş
                SubCategory(name = context.getString(R.string.subcategory_work_meals), categoryId = "work", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_office_supplies), categoryId = "work", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_business_travel), categoryId = "work", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_work_education), categoryId = "work", isDefault = true),
                SubCategory(name = context.getString(R.string.subcategory_freelance_payments), categoryId = "work", isDefault = true),
                
                // Vergi
                SubCategory(name = context.getString(R.string.subcategory_tax_payments), categoryId = "tax", isDefault = true),
                // Diğer
                SubCategory(name = context.getString(R.string.subcategory_other_expenses), categoryId = "others", isDefault = true)
            )
        }
    }
}
