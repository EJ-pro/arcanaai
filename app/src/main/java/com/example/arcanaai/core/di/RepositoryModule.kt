package com.example.arcanaai.core.di

import android.content.Context
import com.example.arcanaai.data.repository.TarotRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTarotRepository(
        @ApplicationContext context: Context
    ): TarotRepository {
        return TarotRepository(context)
    }
}
