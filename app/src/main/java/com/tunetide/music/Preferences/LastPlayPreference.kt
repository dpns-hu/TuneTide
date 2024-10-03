package com.tunetide.music.Preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LastPlayPreference @Inject constructor(@ApplicationContext context: Context) {
    private var lastPlayedPref: SharedPreferences =
        context.getSharedPreferences("LastPlayedSong", Context.MODE_PRIVATE)

    fun updateLastPlayed(currentMediaItemIndex: Int) {
        val editor = lastPlayedPref.edit()
        editor.putInt("LastPlayed", currentMediaItemIndex)
        editor.apply()
    }
    fun getLastPlayed(): Int? {
        return lastPlayedPref.getInt("LastPlayed", -1)
    }
}