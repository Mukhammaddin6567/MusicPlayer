package uz.gita.musicplayer.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.gita.musicplayer.data.local.MySharedPreferences
import javax.inject.Singleton

/*
@Module
@InstallIn(SingletonComponent::class)
class LocalDatabaseModule {

    @[Provides Singleton]
    fun provideMusicDatabase(@ApplicationContext context: Context): MusicDatabase =
        MusicDatabase.getDatabase(context)

    @[Provides Singleton]
    fun provideMusicDao(database: MusicDatabase): MusicDao = database.musicDao()

    @[Provides Singleton]
    fun provideMyPreferences(@ApplicationContext context: Context) = MySharedPreferences(context)

}*/
