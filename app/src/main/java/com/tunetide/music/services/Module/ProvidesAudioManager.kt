package com.tunetide.music.services.Module

import android.content.Context
import android.media.AudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AudioManagerProvider {

    @Provides
    fun provideAudioManager(@ApplicationContext context: Context,): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}