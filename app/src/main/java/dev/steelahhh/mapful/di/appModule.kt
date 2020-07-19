package dev.steelahhh.mapful.di

import dev.steelahhh.mapful.data.di.dataModule

val appModule = dataModule + viewModelModule
