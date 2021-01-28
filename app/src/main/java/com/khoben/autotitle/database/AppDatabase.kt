package com.khoben.autotitle.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.khoben.autotitle.database.dao.ProjectDao
import com.khoben.autotitle.database.entity.Project

@Database(entities = [Project::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        private val DB_NAME = "AutoTitleDB"
        private var INSTANCE: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }
    }
}