package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PersonEntity
import com.example.data.PersonRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ActiveTab {
    object ListTab : ActiveTab
    object AddEditTab : ActiveTab
    object TreeTab : ActiveTab
    object DocumentsTab : ActiveTab
    object DashboardTab : ActiveTab
    object BankTab : ActiveTab
    object StateTab : ActiveTab
}

data class LandDeed(
    val id: String,
    val name: String,
    val location: String,
    val areaFeddans: Double,
    val cropType: String,
    val waterSource: String,
    val annualYieldSdg: Double,
    val ownerId: Int?
)

class FamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PersonRepository
    
    val allPeople: StateFlow<List<PersonEntity>>
    
    // UI state flows
    private val _activeTab = MutableStateFlow<ActiveTab>(ActiveTab.ListTab)
    val activeTab: StateFlow<ActiveTab> = _activeTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _genderFilter = MutableStateFlow<String?>(null) // "ذكر", "أنثى", or null
    val genderFilter: StateFlow<String?> = _genderFilter.asStateFlow()

    private val _aliveFilter = MutableStateFlow<Boolean?>(null) // true, false, or null
    val aliveFilter: StateFlow<Boolean?> = _aliveFilter.asStateFlow()

    private val _selectedPersonForTree = MutableStateFlow<PersonEntity?>(null)
    val selectedPersonForTree: StateFlow<PersonEntity?> = _selectedPersonForTree.asStateFlow()

    // State for selected person in Documents Center
    private val _selectedPersonForDocument = MutableStateFlow<PersonEntity?>(null)
    val selectedPersonForDocument: StateFlow<PersonEntity?> = _selectedPersonForDocument.asStateFlow()

    private val _personToEdit = MutableStateFlow<PersonEntity?>(null)
    val personToEdit: StateFlow<PersonEntity?> = _personToEdit.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Lands and agriculture sovereignty registry state
    private val _landDeeds = MutableStateFlow<List<LandDeed>>(
        listOf(
            LandDeed("L-101", "مشروع الزيداب الزراعي النموذجي", "الزيداب", 150.0, "القمح الفيدرالي الممتاز", "طلمبات النيل المباشرة", 8500000.0, 1),
            LandDeed("L-102", "مزارع شط طيبة الخواض", "طيبة الخواض", 85.0, "البصل والتمور السودانية", "شريان النيل والمياه الجوفية", 4800000.0, 2),
            LandDeed("L-103", "حيازة آل البشير النموذجية", "شندي", 45.0, "البرسيم الحجازي والأعلاف", "مضخات النيل الانسيابية", 2900000.0, 3),
            LandDeed("L-104", "جزر السرة للتمور والفاكهة", "المحمية", 60.0, "التمور السودانية الفاخرة", "آبار ارتوازية بالطاقة الشمسية", 3400000.0, 5)
        )
    )
    val landDeeds: StateFlow<List<LandDeed>> = _landDeeds.asStateFlow()

    fun transferLandOwnership(landId: String, newOwnerId: Int) {
        viewModelScope.launch {
            _landDeeds.update { current ->
                current.map { land ->
                    if (land.id == landId) land.copy(ownerId = newOwnerId) else land
                }
            }
        }
    }

    fun registerNewLandDeed(name: String, location: String, area: Double, crop: String, source: String, yield: Double, ownerId: Int?) {
        viewModelScope.launch {
            val newId = "L-${101 + _landDeeds.value.size}"
            _landDeeds.update { current ->
                current + LandDeed(newId, name, location, area, crop, source, yield, ownerId)
            }
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        val personDao = database.personDao()
        repository = PersonRepository(personDao)
        
        allPeople = repository.allPeople.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Children of the currently visualized tree person
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val treeChildren: StateFlow<List<PersonEntity>> = _selectedPersonForTree
        .flatMapLatest { person ->
            if (person == null) flowOf(emptyList())
            else repository.getChildrenOf(person.id, person.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setTab(tab: ActiveTab) {
        _activeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGenderFilter(gender: String?) {
        _genderFilter.value = gender
    }

    fun setAliveFilter(isAlive: Boolean?) {
        _aliveFilter.value = isAlive
    }

    fun selectPersonForTree(person: PersonEntity) {
        _selectedPersonForTree.value = person
        _activeTab.value = ActiveTab.TreeTab
    }

    fun selectPersonForDocument(person: PersonEntity) {
        _selectedPersonForDocument.value = person
        _activeTab.value = ActiveTab.DocumentsTab
    }

    fun clearSelectedDocumentPerson() {
        _selectedPersonForDocument.value = null
    }

    fun startAddPerson() {
        _personToEdit.value = null
        _activeTab.value = ActiveTab.AddEditTab
    }

    fun startEditPerson(person: PersonEntity) {
        _personToEdit.value = person
        _activeTab.value = ActiveTab.AddEditTab
    }

    fun deletePerson(person: PersonEntity) {
        viewModelScope.launch {
            try {
                // Clear reciprocal spouse links before deleting
                if (person.spouseId != null) {
                    val spouse = repository.getPersonById(person.spouseId)
                    if (spouse != null && spouse.spouseId == person.id) {
                        repository.update(spouse.copy(spouseId = null))
                    }
                }
                
                // Set dependents reference IDs to null or clean up
                allPeople.value.forEach { other ->
                    if (other.fatherId == person.id || other.motherId == person.id || other.spouseId == person.id) {
                        val updated = other.copy(
                            fatherId = if (other.fatherId == person.id) null else other.fatherId,
                            motherId = if (other.motherId == person.id) null else other.motherId,
                            spouseId = if (other.spouseId == person.id) null else other.spouseId
                        )
                        repository.update(updated)
                    }
                }

                repository.delete(person)
                
                if (_selectedPersonForTree.value?.id == person.id) {
                    _selectedPersonForTree.value = null
                }
                if (_selectedPersonForDocument.value?.id == person.id) {
                    _selectedPersonForDocument.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "حدث خطأ أثناء إلغاء قيد الفرد: ${e.localizedMessage}"
            }
        }
    }

    fun archivePerson(personId: Int, archive: Boolean) {
        viewModelScope.launch {
            try {
                val person = repository.getPersonById(personId)
                if (person != null) {
                    val updated = person.copy(isArchived = archive)
                    repository.update(updated)
                    if (_selectedPersonForDocument.value?.id == personId) {
                        _selectedPersonForDocument.value = updated
                    }
                    if (_selectedPersonForTree.value?.id == personId) {
                        _selectedPersonForTree.value = updated
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشلت عملية أرشفة السجل: ${e.localizedMessage}"
            }
        }
    }

    fun savePerson(
        firstName: String,
        fatherName: String,
        grandFatherName: String,
        familyName: String,
        gender: String,
        birthDate: String,
        birthPlace: String,
        isAlive: Boolean,
        deathDate: String?,
        phoneNumber: String,
        address: String,
        notes: String,
        fatherId: Int?,
        motherId: Int?,
        spouseId: Int?,
        // Civil attributes
        nationalNumber: String,
        occupation: String,
        bloodType: String,
        registrationDate: String,
        registryNumber: String,
        villageName: String,
        isArchived: Boolean = false,
        profileImage: String? = null,
        bankAccountNumber: String = "",
        bankBalance: Double = 125000.0,
        passportNumber: String = "",
        licenseNumber: String = "",
        militaryId: String = "",
        affiliationId: String = "",
        maritalStatus: String = "أعزب",
        marriageDate: String? = null,
        divorceDate: String? = null
    ) {
        viewModelScope.launch {
            if (firstName.isBlank() || familyName.isBlank()) {
                _errorMessage.value = "الاسم الأول واسم العائلة حقلان إلزاميان لإصدار السجل المدني!"
                return@launch
            }

            try {
                val currentEdit = _personToEdit.value
                val person = PersonEntity(
                    id = currentEdit?.id ?: 0,
                    firstName = firstName.trim(),
                    fatherName = fatherName.trim(),
                    grandFatherName = grandFatherName.trim(),
                    familyName = familyName.trim(),
                    gender = gender,
                    birthDate = birthDate.trim(),
                    birthPlace = birthPlace.trim(),
                    isAlive = isAlive,
                    deathDate = if (isAlive) null else deathDate?.trim(),
                    phoneNumber = phoneNumber.trim(),
                    address = address.trim(),
                    notes = notes.trim(),
                    fatherId = fatherId,
                    motherId = motherId,
                    spouseId = spouseId,
                    nationalNumber = nationalNumber.trim(),
                    occupation = occupation.trim(),
                    bloodType = bloodType.trim(),
                    registrationDate = registrationDate.trim().ifBlank { "2026-05-30" },
                    registryNumber = registryNumber.trim(),
                    villageName = villageName.trim().ifBlank { "الحرة - المركز" },
                    isArchived = isArchived,
                    profileImage = profileImage,
                    bankAccountNumber = bankAccountNumber.trim(),
                    bankBalance = bankBalance,
                    passportNumber = passportNumber.trim(),
                    licenseNumber = licenseNumber.trim(),
                    militaryId = militaryId.trim(),
                    affiliationId = affiliationId.trim(),
                    maritalStatus = maritalStatus.trim(),
                    marriageDate = marriageDate?.trim(),
                    divorceDate = divorceDate?.trim()
                )

                val savedId = if (currentEdit == null) {
                    repository.insert(person).toInt()
                } else {
                    repository.update(person)
                    person.id
                }

                // Reciprocal spouse sync logic:
                if (spouseId != null) {
                    val spouse = repository.getPersonById(spouseId)
                    if (spouse != null && spouse.spouseId != savedId) {
                        repository.update(spouse.copy(spouseId = savedId))
                    }
                } else if (currentEdit != null && currentEdit.spouseId != null) {
                    // Spouseless adjustment: if spouse was removed, clean the former spouse's link
                    val oldSpouse = repository.getPersonById(currentEdit.spouseId)
                    if (oldSpouse != null && oldSpouse.spouseId == currentEdit.id) {
                        repository.update(oldSpouse.copy(spouseId = null))
                    }
                }

                _personToEdit.value = null
                _activeTab.value = ActiveTab.ListTab
                
                // If we edited the person whose tree or document is currently displayed, refresh
                if (_selectedPersonForTree.value?.id == savedId) {
                    _selectedPersonForTree.value = repository.getPersonById(savedId)
                }
                if (_selectedPersonForDocument.value?.id == savedId) {
                    _selectedPersonForDocument.value = repository.getPersonById(savedId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشل في تقييد البيانات بالسجل الحكومي: ${e.localizedMessage}"
            }
        }
    }

    fun depositToAccount(personId: Int, amount: Double) {
        viewModelScope.launch {
            try {
                val person = repository.getPersonById(personId)
                if (person != null) {
                    val updated = person.copy(bankBalance = person.bankBalance + amount)
                    repository.update(updated)
                    if (_selectedPersonForDocument.value?.id == personId) {
                        _selectedPersonForDocument.value = updated
                    }
                    if (_selectedPersonForTree.value?.id == personId) {
                        _selectedPersonForTree.value = updated
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشلت عملية الإيداع: ${e.localizedMessage}"
            }
        }
    }

    fun withdrawFromAccount(personId: Int, amount: Double) {
        viewModelScope.launch {
            try {
                val person = repository.getPersonById(personId)
                if (person != null) {
                    if (person.bankBalance >= amount) {
                        val updated = person.copy(bankBalance = person.bankBalance - amount)
                        repository.update(updated)
                        if (_selectedPersonForDocument.value?.id == personId) {
                            _selectedPersonForDocument.value = updated
                        }
                        if (_selectedPersonForTree.value?.id == personId) {
                            _selectedPersonForTree.value = updated
                        }
                    } else {
                        _errorMessage.value = "الرصيد غير كافٍ لإتمام السحب بنجاح! الرصيد الحالي: ${person.bankBalance} ج.س"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشلت عملية السحب: ${e.localizedMessage}"
            }
        }
    }

    fun transferBetweenAccounts(fromId: Int, toId: Int, amount: Double) {
        viewModelScope.launch {
            try {
                val sender = repository.getPersonById(fromId)
                val receiver = repository.getPersonById(toId)
                if (sender != null && receiver != null) {
                    if (sender.bankBalance >= amount) {
                        val senderUpdated = sender.copy(bankBalance = sender.bankBalance - amount)
                        val receiverUpdated = receiver.copy(bankBalance = receiver.bankBalance + amount)
                        repository.update(senderUpdated)
                        repository.update(receiverUpdated)
                        
                        if (_selectedPersonForDocument.value?.id == fromId) {
                            _selectedPersonForDocument.value = senderUpdated
                        } else if (_selectedPersonForDocument.value?.id == toId) {
                            _selectedPersonForDocument.value = receiverUpdated
                        }
                    } else {
                        _errorMessage.value = "فشلت الحوالة الاستباقية: رصيد المرسل غير كافٍ!"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشل التحويل المصرفي الموحد: ${e.localizedMessage}"
            }
        }
    }

    fun disburseStateGrant(personId: Int, amount: Double) {
        viewModelScope.launch {
            try {
                val person = repository.getPersonById(personId)
                if (person != null) {
                    val updated = person.copy(bankBalance = person.bankBalance + amount)
                    repository.update(updated)
                    if (_selectedPersonForDocument.value?.id == personId) {
                        _selectedPersonForDocument.value = updated
                    }
                    if (_selectedPersonForTree.value?.id == personId) {
                        _selectedPersonForTree.value = updated
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشلت منحة دعم الدولة الفيدرالية: ${e.localizedMessage}"
            }
        }
    }

    fun registerMarriage(husbandId: Int, wifeId: Int, marriageDate: String, location: String) {
        viewModelScope.launch {
            try {
                val husband = repository.getPersonById(husbandId)
                val wife = repository.getPersonById(wifeId)
                if (husband != null && wife != null) {
                    val updatedHusband = husband.copy(
                        spouseId = wifeId,
                        maritalStatus = "متزوج",
                        marriageDate = marriageDate,
                        villageName = location
                    )
                    val updatedWife = wife.copy(
                        spouseId = husbandId,
                        maritalStatus = "متزوج",
                        marriageDate = marriageDate,
                        villageName = location
                    )
                    repository.update(updatedHusband)
                    repository.update(updatedWife)
                    
                    // Update state variables if they are currently opened
                    if (_selectedPersonForDocument.value?.id == husbandId) {
                        _selectedPersonForDocument.value = updatedHusband
                    } else if (_selectedPersonForDocument.value?.id == wifeId) {
                        _selectedPersonForDocument.value = updatedWife
                    }
                    
                    if (_selectedPersonForTree.value?.id == husbandId) {
                        _selectedPersonForTree.value = updatedHusband
                    } else if (_selectedPersonForTree.value?.id == wifeId) {
                        _selectedPersonForTree.value = updatedWife
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشل تسجيل قسيمة النكاح الفيدرالية: ${e.localizedMessage}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Load authentic, state-specific sample data directly representing "ولاية نهر النيل" and its cities/villages
    fun loadSampleData() {
        viewModelScope.launch {
            try {
                val database = AppDatabase.getDatabase(getApplication())
                database.personDao().clearDatabase()

                // ----------------------------------------------------
                // GENERATION 1: Patriarch & Matriarch of Al-Mahmiya State
                // ----------------------------------------------------
                val grandfather = PersonEntity(
                    firstName = "البشير",
                    fatherName = "الطيب",
                    grandFatherName = "فضل المولى",
                    familyName = "المحمية",
                    gender = "ذكر",
                    birthDate = "1932-11-20",
                    birthPlace = "شندي",
                    isAlive = false,
                    deathDate = "2018-04-05",
                    notes = "مؤسس أول مدرسة أهلية لتعليم علوم القرآن واللغة العربية على ضفاف نهر النيل بقرى المحمية.",
                    occupation = "شيخ وخطيب القرية",
                    bloodType = "O+",
                    registrationDate = "1960-03-01",
                    registryNumber = "س-101/م-ح",
                    villageName = "المحمية",
                    bankBalance = 0.0,
                    bankAccountNumber = "NLE-BNK-7001",
                    passportNumber = "P005112",
                    licenseNumber = "DL-10022",
                    militaryId = "SAF-0012",
                    affiliationId = "AFF-0001",
                    maritalStatus = "متزوج",
                    marriageDate = "1955-08-10",
                    isArchived = true,
                    profileImage = "man_traditional"
                )
                val grandfatherId = repository.insert(grandfather).toInt()

                val grandmother = PersonEntity(
                    firstName = "سكينة",
                    fatherName = "عبد الكريم",
                    grandFatherName = "عوض",
                    familyName = "الحرة",
                    gender = "أنثى",
                    birthDate = "1940-05-12",
                    birthPlace = "الدامر",
                    isAlive = true,
                    spouseId = grandfatherId,
                    notes = "رائدة الأعمال المنزلية اليدوية وتنسيخ الغزل التقليدي بالمنطقة.",
                    occupation = "معلمة نسيج متقاعدة",
                    bloodType = "A+",
                    registrationDate = "1960-03-01",
                    registryNumber = "س-102/م-ح",
                    villageName = "طيبة القوز",
                    bankBalance = 1450000.0,
                    bankAccountNumber = "NLE-BNK-7002",
                    passportNumber = "P005113",
                    licenseNumber = "",
                    militaryId = "",
                    affiliationId = "AFF-0002",
                    maritalStatus = "أرمل",
                    marriageDate = "1955-08-10",
                    isArchived = false,
                    profileImage = "woman_traditional"
                )
                val grandmotherId = repository.insert(grandmother).toInt()
                
                // Link spouse recipocral
                repository.update(grandfather.copy(id = grandfatherId, spouseId = grandmotherId))

                // ----------------------------------------------------
                // GENERATION 2: The Leaders/Pillars of the modern state
                // ----------------------------------------------------
                val father = PersonEntity(
                    firstName = "أنس",
                    fatherName = "البشير",
                    grandFatherName = "الطيب",
                    familyName = "المحمية",
                    gender = "ذكر",
                    birthDate = "1968-07-15",
                    birthPlace = "بربر",
                    isAlive = true,
                    phoneNumber = "0511112233",
                    address = "شندي - مربع 4",
                    notes = "مدير مصلحة الأراضي والتخطيط العمراني بولاية قرية المحمية، نهر النيل.",
                    fatherId = grandfatherId,
                    motherId = grandmotherId,
                    occupation = "مهندس مساحة وتطوير مدني",
                    bloodType = "B+",
                    registrationDate = "1990-10-12",
                    registryNumber = "س-328/م-ح",
                    villageName = "الزيداب",
                    bankBalance = 3800000.0,
                    bankAccountNumber = "NLE-BNK-9120",
                    passportNumber = "P112040",
                    licenseNumber = "DL-20412",
                    militaryId = "SAF-2910",
                    affiliationId = "AFF-1102",
                    maritalStatus = "متزوج",
                    marriageDate = "1993-02-14",
                    isArchived = false,
                    profileImage = "man"
                )
                val fatherId = repository.insert(father).toInt()

                val mother = PersonEntity(
                    firstName = "زينب",
                    fatherName = "عثمان",
                    grandFatherName = "تاج السر",
                    familyName = "الكردفاني",
                    gender = "أنثى",
                    birthDate = "1975-01-08",
                    birthPlace = "عطبرة",
                    isAlive = true,
                    phoneNumber = "0544558899",
                    address = "شندي - مربع 4",
                    notes = "طبيبة نساء وولادة بقرية المحمية ومستشفى الحرة التخصصي.",
                    spouseId = fatherId,
                    occupation = "طبيب استشاري",
                    bloodType = "O-",
                    registrationDate = "1995-12-15",
                    registryNumber = "س-329/م-ح",
                    villageName = "السرة",
                    bankBalance = 6200000.0,
                    bankAccountNumber = "NLE-BNK-9121",
                    passportNumber = "P112041",
                    licenseNumber = "DL-20413",
                    militaryId = "",
                    affiliationId = "AFF-1103",
                    maritalStatus = "متزوج",
                    marriageDate = "1993-02-14",
                    isArchived = false,
                    profileImage = "woman"
                )
                val motherId = repository.insert(mother).toInt()
                
                // Link father reciprocal spouse
                repository.update(father.copy(id = fatherId, spouseId = motherId))

                // Brother of Father (The Uncle of the main youth)
                val uncle = PersonEntity(
                    firstName = "مصطفى",
                    fatherName = "البشير",
                    grandFatherName = "الطيب",
                    familyName = "المحمية",
                    gender = "ذكر",
                    birthDate = "1972-03-30",
                    birthPlace = "المتمة",
                    isAlive = true,
                    phoneNumber = "0599887711",
                    address = "عطبرة - حي النيل",
                    notes = "مزارع رائد من مزارعي الضفاف، يستغل طرق الري الحديثة على نهر النيل المعطاء.",
                    fatherId = grandfatherId,
                    motherId = grandmotherId,
                    occupation = "مهندس زراعي ومزارع نيل",
                    bloodType = "AB+",
                    registrationDate = "1992-05-15",
                    registryNumber = "س-404/م-ح",
                    villageName = "العالياب",
                    bankBalance = 150000.0,
                    bankAccountNumber = "NLE-BNK-5049",
                    passportNumber = "P884210",
                    licenseNumber = "DL-00124",
                    militaryId = "SAF-4050",
                    affiliationId = "AFF-0044",
                    maritalStatus = "مطلق",
                    marriageDate = "1998-05-20",
                    divorceDate = "2015-09-12",
                    isArchived = false,
                    profileImage = "military_uniform"
                )
                repository.insert(uncle)

                // ----------------------------------------------------
                // GENERATION 3: The Young Aspiring Citizens (training users!)
                // ----------------------------------------------------
                val son1 = PersonEntity(
                    firstName = "مازن",
                    fatherName = "أنس",
                    grandFatherName = "البشير",
                    familyName = "المحمية",
                    gender = "ذكر",
                    birthDate = "1998-09-05",
                    birthPlace = "عطبرة",
                    isAlive = true,
                    phoneNumber = "0566332211",
                    address = "الدامر، المجمع الجامعي",
                    notes = "ضابط سجل مدني وأرشفة رقمية متدرب بمجمع الخدمات الحكومية.",
                    fatherId = fatherId,
                    motherId = motherId,
                    occupation = "تقني نظم معلومات جغرافية",
                    bloodType = "B+",
                    registrationDate = "2018-09-05",
                    registryNumber = "س-992/م-ح",
                    villageName = "طيبة الخواض",
                    bankBalance = 78000.0,
                    bankAccountNumber = "NLE-BNK-1002",
                    passportNumber = "P005230",
                    licenseNumber = "DL-20412",
                    militaryId = "SAF-88301",
                    affiliationId = "AFF-7720",
                    maritalStatus = "أعزب",
                    isArchived = false,
                    profileImage = "boy"
                )
                val son1Id = repository.insert(son1).toInt()

                val daughter1 = PersonEntity(
                    firstName = "نفيسة",
                    fatherName = "أنس",
                    grandFatherName = "البشير",
                    familyName = "المحمية",
                    gender = "أنثى",
                    birthDate = "2001-11-12",
                    birthPlace = "أبو حمد",
                    isAlive = true,
                    phoneNumber = "0555443322",
                    address = "الدامر - مربع 4",
                    notes = "باحثة في شؤون التراث لقرى نهر النيل ومحمية الحياة الفطرية بالسرة.",
                    fatherId = fatherId,
                    motherId = motherId,
                    occupation = "أخصائي تراث وتاريخ",
                    bloodType = "O+",
                    registrationDate = "2020-01-20",
                    registryNumber = "س-1051/م-ح",
                    villageName = "البجراوية",
                    bankBalance = 520000.0,
                    bankAccountNumber = "NLE-BNK-1003",
                    passportNumber = "P005231",
                    licenseNumber = "",
                    militaryId = "",
                    affiliationId = "AFF-7721",
                    maritalStatus = "أعزب",
                    isArchived = false,
                    profileImage = "girl"
                )
                repository.insert(daughter1)

                val son2 = PersonEntity(
                    firstName = "سيف الدين",
                    fatherName = "أنس",
                    grandFatherName = "البشير",
                    familyName = "المحمية",
                    gender = "ذكر",
                    birthDate = "2009-04-18",
                    birthPlace = "البحيرة",
                    isAlive = true,
                    address = "الدامر - مربع 4",
                    notes = "طالب متميز في ثانوية الدامر النموذجية المتكاملة.",
                    fatherId = fatherId,
                    motherId = motherId,
                    occupation = "طالب",
                    bloodType = "A-",
                    registrationDate = "2025-05-10",
                    registryNumber = "س-2041/م-ح",
                    villageName = "كنور",
                    bankBalance = 32000.0,
                    bankAccountNumber = "NLE-BNK-1004",
                    passportNumber = "",
                    licenseNumber = "",
                    militaryId = "",
                    affiliationId = "",
                    maritalStatus = "أعزب",
                    isArchived = false,
                    profileImage = "avatar_doc"
                )
                repository.insert(son2)

                // Select loaded central person
                val defaultCitizen = repository.getPersonById(son1Id)
                if (defaultCitizen != null) {
                    _selectedPersonForDocument.value = defaultCitizen
                    _selectedPersonForTree.value = defaultCitizen
                }
            } catch (e: Exception) {
                _errorMessage.value = "فشل في تسكين قاعدة السجل الوطني المدني: ${e.localizedMessage}"
            }
        }
    }
}
