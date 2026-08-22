package com.example.fnotepro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fnotepro.data.model.Guide
import com.example.fnotepro.data.model.SavedNumber
import com.example.fnotepro.data.model.SendHistoryItem
import com.example.fnotepro.data.model.UserProfile

@Database(
    entities = [
        Guide::class,
        SavedNumber::class,
        SendHistoryItem::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun guideDao(): GuideDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fnote_pro_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
