package com.tunetide.music.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallReceiver : BroadcastReceiver() {


    @Inject
    lateinit var controllerFuture: ListenableFuture<MediaController>
    @Inject
    lateinit var playerState: PlayerState
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals("android.intent.action.PHONE_STATE")) {
            val telephony = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephony.listen(object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    super.onCallStateChanged(state, phoneNumber)
                    when (state) {
                        TelephonyManager.CALL_STATE_RINGING,
                        TelephonyManager.CALL_STATE_OFFHOOK -> {
                              playerState.changeCallStatus(true)
                            playerState.changePlayBack(false)
                            playerState.updateCall(true)


                        }

                        TelephonyManager.CALL_STATE_IDLE -> {
                             playerState.changeCallStatus(false)
                            playerState.updateCall(false)
                        }
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }
}
