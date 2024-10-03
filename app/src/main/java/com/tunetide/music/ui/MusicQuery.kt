package com.tunetide.music.ui

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.tunetide.music.model.Music
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MusicQuery @Inject constructor(@ApplicationContext val context: Context) {
    fun queryAudioFilesWithPaging(startIndex: Int, itemsToLoad: Int): MutableList<Music> {
        val musicList = mutableListOf<Music>()
        var currentIndex = startIndex

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.SIZE,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES).toString()
        )

        // Display music in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val contentResolver = context.contentResolver
        val query = contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val titleIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            val displayNameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.SIZE)
            val albumColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val albumIDColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)

            while (cursor.moveToPosition(currentIndex)) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val audioTitle = cursor.getString(titleIndex)
                val audioDisplayName = cursor.getString(displayNameIndex)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val album = cursor.getString(albumColumn)
                val artist = cursor.getString(artistColumn)
                val albumID = cursor.getLong(albumIDColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val thumbnailURI = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumID
                )
                musicList += Music(
                    id = id,
                    title = audioTitle,
                    displayName = audioDisplayName,
                    artist = artist,
                    album = album,
                    uri = contentUri,
                    duration = duration,
                    size = size,
                    thumbnailURI = thumbnailURI
                )
                Timber.d("queryAudioFilesWithPaging: currentIndex:$currentIndex")
                currentIndex++

                if (currentIndex >= itemsToLoad + startIndex) {
                    break
                }

            }
        }
        return musicList
    }
}