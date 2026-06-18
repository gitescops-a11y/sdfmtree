package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,
    val fatherName: String = "",
    val grandFatherName: String = "",
    val familyName: String,
    val gender: String, // "ذكر" أو "أنثى"
    val birthDate: String = "",
    val birthPlace: String = "",
    val isAlive: Boolean = true,
    val deathDate: String? = null,
    val phoneNumber: String = "",
    val address: String = "",
    val notes: String = "",
    
    // Family Relationships links
    val fatherId: Int? = null,
    val motherId: Int? = null,
    val spouseId: Int? = null,

    // Civil Registry Extension Fields
    val nationalNumber: String = "", // الرقم الوطني الموحد
    val occupation: String = "", // المهنة / الحرفة
    val bloodType: String = "", // زمرة الدم
    val registrationDate: String = "", // تاريخ القيد المدني
    val registryNumber: String = "", // رقم الصحيفة والسجل
    val villageName: String = "", // اسم قرية المحمية
    
    // Mini-State Added Fields
    val bankBalance: Double = 125000.0, // رصيد الحساب البنكي
    val bankAccountNumber: String = "", // رقم الحساب البنكي
    val passportNumber: String = "", // رقم جواز السفر
    val licenseNumber: String = "", // رقم رخصة القيادة
    val militaryId: String = "", // الرقم العسكري للقوات المسلحة
    val affiliationId: String = "", // رقم بطاقة الانتساب
    val maritalStatus: String = "أعزب", // "أعزب" أو "متزوج" أو "مطلق" أو "أرمل"
    val marriageDate: String? = null, // تاريخ الزواج
    val divorceDate: String? = null, // تاريخ الطلاق
    val isArchived: Boolean = false, // حالة الأرشفة وحفظ السجلات المدنية التاريخية
    val profileImage: String? = null // صورة الملف الشخصي أو الرابط الرمزي للأيقونة الخاصة
) {
    // Helper to get formatted Bank Account or generate one
    fun getDisplayBankAccount(): String {
        if (bankAccountNumber.isNotBlank()) return bankAccountNumber
        return "NLE-BNK-${id + 104320}"
    }

    // Helper to get formatted Passport or generate one
    fun getDisplayPassport(): String {
        if (passportNumber.isNotBlank()) return passportNumber
        return "P${id + 405321}"
    }

    // Helper to get formatted Driver's License or generate one
    fun getDisplayLicense(): String {
        if (licenseNumber.isNotBlank()) return licenseNumber
        return "DL-${id + 88120}"
    }

    // Helper to get formatted Military ID or generate one
    fun getDisplayMilitaryId(): String {
        if (militaryId.isNotBlank()) return militaryId
        return "SAF-${id + 7420}"
    }

    // Helper to get formatted Affiliation or generate one
    fun getDisplayAffiliationId(): String {
        if (affiliationId.isNotBlank()) return affiliationId
        return "AFF-${id + 9102}"
    }

    // Helper function to get full name
    fun getFullName(): String {
        val middle = listOf(fatherName, grandFatherName).filter { it.isNotBlank() }.joinToString(" ")
        return listOf(firstName, middle, familyName).filter { it.isNotBlank() }.joinToString(" ")
    }

    // Helper to get formatted National ID or generate automatic valid ID based on fields
    fun getDisplayNationalNumber(): String {
        if (nationalNumber.isNotBlank()) return nationalNumber
        
        // Let's generate a realistic national id for Al-Mahmiya State:
        // Format: [GenderDigit]-[BirthYear]-[VillageCode]-[RecordID]
        // Gender: 1 for male, 2 for female
        val gCode = if (gender == "ذكر") "1" else "2"
        
        // Birth year Extraction
        val bYear = if (birthDate.length >= 4) {
            birthDate.substring(0, 4)
        } else {
            "2000"
        }
        
        // Village Code (derived from name hash)
        val vCode = if (villageName.isNotBlank()) {
            val h = kotlin.math.abs(villageName.hashCode() % 100)
            String.format("%02d", h)
        } else {
            "07" // Default "Al-Hurra center"
        }
        
        // Record Number
        val rNum = String.format("%04d", id + 1205)
        
        return "$gCode-$bYear-$vCode-$rNum"
    }

    // Official reference number for documents
    fun getDisplayRegistryInfo(): String {
        if (registryNumber.isNotBlank()) return registryNumber
        // Default official serial based on registration
        return "س-${id + 328}/ق-م"
    }
}
