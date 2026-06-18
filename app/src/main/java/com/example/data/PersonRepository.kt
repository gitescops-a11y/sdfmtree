package com.example.data

import kotlinx.coroutines.flow.Flow

class PersonRepository(private val personDao: PersonDao) {
    val allPeople: Flow<List<PersonEntity>> = personDao.getAllPeople()

    suspend fun getPersonById(id: Int): PersonEntity? {
        return personDao.getPersonById(id)
    }

    fun getPersonByIdFlow(id: Int): Flow<PersonEntity?> {
        return personDao.getPersonByIdFlow(id)
    }

    fun getChildrenOf(fatherId: Int?, motherId: Int?): Flow<List<PersonEntity>> {
        return personDao.getChildrenOf(fatherId, motherId)
    }

    suspend fun insert(person: PersonEntity): Long {
        return personDao.insertPerson(person)
    }

    suspend fun update(person: PersonEntity) {
        personDao.updatePerson(person)
    }

    suspend fun delete(person: PersonEntity) {
        personDao.deletePerson(person)
    }
}
