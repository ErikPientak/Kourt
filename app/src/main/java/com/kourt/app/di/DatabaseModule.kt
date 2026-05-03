package com.kourt.app.di

import android.content.Context
import androidx.room.Room
import com.kourt.app.data.local.KourtDatabase
import com.kourt.app.data.local.PendingMatchStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKourtDatabase(@ApplicationContext context: Context): KourtDatabase =
        Room.databaseBuilder(context, KourtDatabase::class.java, "kourt.db").build()

    @Provides
    @Singleton
    fun providePendingMatchStatsDao(db: KourtDatabase): PendingMatchStatsDao =
        db.pendingMatchStatsDao()
}
