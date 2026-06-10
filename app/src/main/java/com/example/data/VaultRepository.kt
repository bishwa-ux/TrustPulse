package com.example.data

import kotlinx.coroutines.flow.Flow

class VaultRepository(private val vaultDao: VaultDao) {
    val allEntries: Flow<List<VaultEntry>> = vaultDao.getAllEntries()

    suspend fun insert(entry: VaultEntry) = vaultDao.insertEntry(entry)

    suspend fun deleteById(id: Int) = vaultDao.deleteEntryById(id)
}
