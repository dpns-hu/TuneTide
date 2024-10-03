package com.tunetide.music.services

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerState @Inject constructor(@ApplicationContext context: Context) {
         val playBackState = MutableLiveData<Boolean>(false)
         val isCallActive = MutableLiveData<Boolean>(false)

    fun changePlayBack(state:Boolean) {
        isCallActive.postValue(state)
       playBackState.value = state
    }
    fun changeCallStatus(state:Boolean){
        isCallActive.postValue(state)
        isCallActive.value = state
    }
    fun getCallStatus():Boolean{
        return isCallActive.value!!
    }
    fun getPlayBackState():Boolean{
        return playBackState.value!!
    }
    private var callStatusPref: SharedPreferences =
        context.getSharedPreferences("callStatusPref", Context.MODE_PRIVATE)

    fun updateCall(state:Boolean) {
        val editor = callStatusPref.edit()
        editor.putBoolean("LastPlayed", state)
        editor.apply()
    }
    fun getActCallStatus(): Boolean {
        return callStatusPref.getBoolean("LastPlayed", false)
    }
}