package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "python_files")
data class PythonFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)
