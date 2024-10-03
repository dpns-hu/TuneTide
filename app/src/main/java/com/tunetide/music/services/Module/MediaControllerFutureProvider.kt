package com.tunetide.music.services.Module

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.tunetide.music.service.MusicService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaControllerFutureProvider {
    @SuppressLint("UnsafeOptInUsageError")
    @Provides
    @Singleton
    fun provideMediaControllerFuture(@ApplicationContext context: Context): ListenableFuture<MediaController> {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        return MediaController.Builder(context, sessionToken).buildAsync()
    }
}
