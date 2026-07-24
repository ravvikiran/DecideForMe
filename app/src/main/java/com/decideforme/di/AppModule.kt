package com.decideforme.di

import android.content.Context
import com.decideforme.data.repository.DecisionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDecisionRepository(
        @ApplicationContext context: Context
    ): DecisionRepository = DecisionRepository(context)
}
