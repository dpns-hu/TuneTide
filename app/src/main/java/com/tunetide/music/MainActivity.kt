package com.tunetide.music

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.READ_PHONE_STATE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.util.concurrent.ListenableFuture
import com.tunetide.music.databinding.ActivityMainBinding
import com.tunetide.music.service.MusicService

import com.tunetide.music.ui.MusicViewmodel
import com.tunetide.music.ui.selectedMusicItemPosition
import com.tunetide.music.util.REQUEST_READ_MEDIA_AUDIO
import com.tunetide.music.util.REQUEST_READ_PHONE_STATE
import com.tunetide.music.util.REQUEST_WRITE_EXTERNAL_STORAGE
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val musicVm: MusicViewmodel by viewModels()


    companion object {
        lateinit var musicService: MusicService
        lateinit var controllerFuture: ListenableFuture<MediaController>
        var isActiveCall = false

    }

    //    lateinit var mediaSession: MediaSession
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timber.d("onCreate: selectedMusicItem:$selectedMusicItemPosition")
        Timber.d("onCreate: ")
        checkPermission()

    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart: Start")
        Timber.d("onStart: Done")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //    private fun checkPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (!hasPermission(READ_MEDIA_AUDIO)) {
//                requestPermission(REQUEST_READ_MEDIA_AUDIO, READ_MEDIA_AUDIO)
//            }
//
//        } else if (!hasPermission(READ_EXTERNAL_STORAGE)) {
//            requestPermission(REQUEST_WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
//        }
//
//            if (!hasPermission(READ_PHONE_STATE)) {
//                requestPermission(REQUEST_READ_PHONE_STATE, READ_PHONE_STATE)
//            }
//    }
    private fun checkPermission() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermission(READ_PHONE_STATE)) {
                permissionsToRequest.add(READ_PHONE_STATE)
            }
        }

        if (!hasPermission(READ_MEDIA_AUDIO)) {
            permissionsToRequest.add(READ_MEDIA_AUDIO)
        }
        if (!hasPermission(READ_EXTERNAL_STORAGE)) {
            permissionsToRequest.add(READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            val requestCode = if (permissionsToRequest.contains(READ_PHONE_STATE)) {
                REQUEST_READ_PHONE_STATE
            } else if (permissionsToRequest.contains(READ_PHONE_STATE)) {
                REQUEST_READ_MEDIA_AUDIO
            } else {
                REQUEST_WRITE_EXTERNAL_STORAGE
            }
            requestPermission(requestCode, *permissionsToRequest.toTypedArray())
        }
    }


    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED

    private fun requestPermission(requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty()) return
        when (requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE, REQUEST_READ_MEDIA_AUDIO, REQUEST_READ_PHONE_STATE -> if (grantResults[0] == PERMISSION_GRANTED) {
                musicVm.permissionGranted.postValue(true)
            } else {
//                showNeedPermissionDialog()
            }
        }
    }

    private fun showNeedPermissionDialog() {
        MaterialAlertDialogBuilder(this, R.style.AnimationDialog)
            .setTitle(getString(R.string.permission_requirement))
            .setMessage(getString(R.string.need_permission_to_access))
            .setPositiveButton(getString(R.string.agree)) { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
            .setNegativeButton(getString(R.string.disagree)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }


}
