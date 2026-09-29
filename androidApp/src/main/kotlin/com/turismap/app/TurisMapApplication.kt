package com.turismap.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.turismap.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TurisMapApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Firebase explícitamente
        // Nota: El plugin google-services también inicializa automáticamente,
        // pero esta inicialización explícita es un best practice
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        
        // Inicializar Koin para inyección de dependencias
        startKoin {
            androidContext(this@TurisMapApplication)
            modules(appModule)
        }
    }
}
