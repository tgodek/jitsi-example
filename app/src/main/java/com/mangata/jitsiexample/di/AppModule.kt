package com.mangata.jitsiexample.di

import com.mangata.jitsiexample.featureEmbedded.MeetingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { MeetingViewModel() }
}