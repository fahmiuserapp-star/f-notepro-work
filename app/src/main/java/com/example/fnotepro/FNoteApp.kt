package com.example.fnotepro

import android.app.Application
import com.example.fnotepro.data.local.AppDatabase
import com.example.fnotepro.data.repository.GuideRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FNoteApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { GuideRepository(database.guideDao()) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            repository.seedInitialDataIfEmpty()
        }
    }
}
