package com.turismap.app.di

import com.turismap.app.data.repository.AuthRepository
import com.turismap.app.data.repository.PlacesRepository
import org.koin.dsl.module

val appModule = module {
    // Repositorios
    single { AuthRepository() }
    single { PlacesRepository() }
    
    // ViewModels se añadirán aquí cuando se creen
}
