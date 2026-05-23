package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.NotificationDao
import com.example.data.dao.PostDao
import com.example.data.dao.ReportDao
import com.example.data.dao.UserDao
import com.example.data.entity.NotificationEntity
import com.example.data.entity.PostEntity
import com.example.data.entity.ReportEntity
import com.example.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        ReportEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun reportDao(): ReportDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unifind_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
