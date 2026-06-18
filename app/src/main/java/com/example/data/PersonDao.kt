package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY familyName ASC, firstName ASC")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: Int): PersonEntity?

    @Query("SELECT * FROM people WHERE id = :id")
    fun getPersonByIdFlow(id: Int): Flow<PersonEntity?>

    @Query("SELECT * FROM people WHERE fatherId = :fatherId OR motherId = :motherId")
    fun getChildrenOf(fatherId: Int?, motherId: Int?): Flow<List<PersonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Query("DELETE FROM people")
    suspend fun clearDatabase()
}
