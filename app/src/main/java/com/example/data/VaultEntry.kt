package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_entries")
data class VaultEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val secretData: String,  // Simulated encryption format
    val timestamp: Long = System.currentTimeMillis()
)
