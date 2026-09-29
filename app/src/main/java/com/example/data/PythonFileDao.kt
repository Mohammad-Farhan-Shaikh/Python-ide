package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PythonFileDao {

    @Query("SELECT * FROM python_files ORDER BY updatedAt DESC")
    fun getAllFiles(): Flow<List<PythonFile>>

    @Query("SELECT * FROM python_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): PythonFile?

    @Query("SELECT * FROM python_files WHERE isActive = 1 LIMIT 1")
    fun getActiveFileFlow(): Flow<PythonFile?>

    @Query("SELECT * FROM python_files WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveFileOnce(): PythonFile?

    @Query("SELECT COUNT(*) FROM python_files")
    suspend fun getFileCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: PythonFile): Long

    @Update
    suspend fun updateFile(file: PythonFile)

    @Delete
    suspend fun deleteFile(file: PythonFile)

    @Query("UPDATE python_files SET isActive = 0")
    suspend fun clearActiveStatus()

    @Query("UPDATE python_files SET isActive = 1 WHERE id = :fileId")
    suspend fun markActive(fileId: Long)

    @Query("UPDATE python_files SET content = :content, updatedAt = :updatedAt WHERE id = :fileId")
    suspend fun updateContent(fileId: Long, content: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE python_files SET name = :name, updatedAt = :updatedAt WHERE id = :fileId")
    suspend fun renameFile(fileId: Long, name: String, updatedAt: Long = System.currentTimeMillis())

    @Transaction
    suspend fun setActiveFile(fileId: Long) {
        clearActiveStatus()
        markActive(fileId)
    }
}
