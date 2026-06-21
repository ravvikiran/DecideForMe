package com.decideforme.di

import android.content.Context
import com.decideforme.data.repository.DecisionRepository
import com.decideforme.domain.DecisionEngine
import com.decideforme.domain.MoodTracker
import com.decideforme.domain.SmartSuggestions
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

    @Provides
    @Singleton
    fun provideDecisionEngine(): DecisionEngine = DecisionEngine()

    @Provides
    @Singleton
    fun provideMoodTracker(): MoodTracker = MoodTracker()

    @Provides
    @Singleton
    fun provideSmartSuggestions(): SmartSuggestions = SmartSuggestions()
}
