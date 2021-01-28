package com.khoben.autotitle.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.khoben.autotitle.database.entity.Project

@Dao
abstract class ProjectDao {
    @get:Query("SELECT * FROM project ORDER BY updated_at DESC")
    abstract val all: LiveData<List<Project>>

    @Query("SELECT * FROM project WHERE id = :id")
    abstract suspend fun getById(id: Long): Project

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(project: Project): Long

    suspend fun insertWithTimestamp(project: Project): Long {
        return insert(project.apply {
            createdAt = System.currentTimeMillis()
            updatedAt = System.currentTimeMillis()
        })
    }

    @Update
    abstract suspend fun update(project: Project)

    @Query("UPDATE project SET title = :title WHERE id = :id")
    abstract suspend fun updateTitle(id: Long, title: String)

    suspend fun updateWithTimestamp(project: Project) {
        update(project.apply {
            updatedAt = System.currentTimeMillis()
        })
    }

    @Query("DELETE FROM project WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Delete
    abstract suspend fun delete(project: Project)
}