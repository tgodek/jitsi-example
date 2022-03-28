package com.mangata.jitsiexample.di

import com.mangata.jitsiexample.feature_embedded.EmbeddedViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { EmbeddedViewModel(get()) }
}