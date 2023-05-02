package ca.josue_lubaki.kmovies.android.di

import ca.josue_lubaki.kmovies.android.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { HomeViewModel(get()) }
}