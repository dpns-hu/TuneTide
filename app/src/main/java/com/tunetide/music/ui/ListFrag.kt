    package com.tunetide.music.ui


    import android.annotation.SuppressLint
    import android.os.Bundle
    import android.os.Handler
    import android.os.Looper
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.activityViewModels
    import androidx.lifecycle.Lifecycle
    import androidx.lifecycle.lifecycleScope
    import androidx.lifecycle.repeatOnLifecycle
    import androidx.media3.common.MediaMetadata
    import androidx.media3.common.Player
    import androidx.media3.session.MediaController
    import androidx.navigation.fragment.findNavController
    import com.bumptech.glide.Glide
    import com.bumptech.glide.load.engine.DiskCacheStrategy
    import com.google.common.util.concurrent.ListenableFuture
    import com.google.common.util.concurrent.MoreExecutors
    import com.tunetide.music.Preferences.LastPlayPreference
    import com.tunetide.music.R
    import com.tunetide.music.databinding.FragmentListBinding
    import com.tunetide.music.model.Music
    import com.tunetide.music.services.PlayerState

    import com.tunetide.music.util.backButtonActsLikeHomeButton
    import com.tunetide.music.util.songPlayedOnce
    import dagger.hilt.android.AndroidEntryPoint
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.collectLatest
    import kotlinx.coroutines.launch
    import timber.log.Timber
    import javax.inject.Inject

    @AndroidEntryPoint
    class ListFrag : Fragment() {
        private var _binding: FragmentListBinding? = null

        @Inject
        lateinit var lastPlayPreference: LastPlayPreference


        var canPlayNow = true
        private val binding get() = _binding!!
        private val musicVm: MusicViewmodel by activityViewModels()
        private var isPlayerListnerAdded = false
        private lateinit var musicAdapter: MusicAdapter

        @Inject
        lateinit var controllerFuture: ListenableFuture<MediaController>
        @Inject
        lateinit var playerState:PlayerState

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            Timber.d("onCreateView: ")
            _binding = FragmentListBinding.inflate(inflater, container, false)
            return binding.root
        }
        @SuppressLint("SuspiciousIndentation")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            backButtonActsLikeHomeButton(this, viewLifecycleOwner)
              Timber.d("onviewcreated")
            musicAdapter = MusicAdapter(this.requireContext(), musicItemClicked())
            binding.homeRcy.adapter = musicAdapter
            //it updates Ui when we return from nowPlaying screen
            musicVm.currentPosition.observe(viewLifecycleOwner) {
                        updateMiniPlayerUI()
                    }
//            musicVm.isSongSuppressed.observe(viewLifecycleOwner){
//                if(it){
//
//                    binding.playButMini.setImageResource(R.drawable.small_play_icon)
//                }else{
//                    binding.playButMini.setImageResource(R.drawable.small_pause_icon)
//                }
//            }
            setupOnPermissionGranted()
            setupCurrentSongLd()
            setupAttachMusicAdapter()
            setupContentObserverLd()
            checkLastPlayed()
            setupMiniPlayerVisibility()
            updateMiniPlayerUI()
            setupMiniPlayer()
        }

       private fun checkLastPlayed(){
           if(!songPlayedOnce) {
               controllerFuture.addListener(
                   {

                       if (!songPlayedOnce)
                           doesLastSongExist()
                   },
                   MoreExecutors.directExecutor()
               )
           }
       }

        private fun doesLastSongExist() {
            if(lastPlayPreference.getLastPlayed()!=-1 ){
                songPlayedOnce = true
                lastSongInExoPlayer(lastPlayPreference.getLastPlayed()!!)
                 updateLastPlayedWork()
                setupMiniPlayerVisibility()
            }
        }

        override fun onStart() {
            super.onStart()
            Timber.d("onStart: start ")
            Timber.d("onStart: Done")
        }

        override fun onResume() {
            super.onResume()
        }

        private fun updateLastPlayedWork() {
            if (songPlayedOnce && !isPlayerListnerAdded) {
                controllerFuture.get().addListener(playerListener)
                isPlayerListnerAdded = true
            }
        }
        // listening the changes in metadata

        private val playerListener = object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                Timber.d("this is metadata called")
                Timber.tag("Searchwithme").d("This is metadataChanged")
                updateMiniPlayerUI()
            }
            val handler = Handler(Looper.getMainLooper())
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
               updateMiniPlayerUI()
            }


            override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
                super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
                Timber.tag("Searchwithme").d("This is me from suppressed")
                // Check if playback is suppressed due to audio focus loss

                if (playbackSuppressionReason == Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS) {
                    // Handle the situation when playback is suppressed (e.g., pause playback)
                    // For example:
                    Timber.tag("Searchwithme").d("Auido Suppressed Due to call")

                    handler.post {
                        binding.playButMini.setImageResource(R.drawable.small_play_icon)
                    }
                   musicVm.isSongSuppressed.value = true
                    }
              else {
                    handler.post {
                        binding.playButMini.setImageResource(R.drawable.small_pause_icon)
                    }
                    musicVm.isSongSuppressed.value = false
                    Timber.tag("Searchwithme").d("Auido back Due to call")

                }
            }
        }

        private fun setupContentObserverLd() {
            musicVm.contentObserverUpdated.observe(viewLifecycleOwner) {
                Timber.d("setupContentObserverLd: updated")
                callRefreshAdapter()
            }
        }

        private fun setupAttachMusicAdapter() {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    musicVm.items.collectLatest { pagingData ->
                        musicAdapter.submitData(pagingData)
                        Timber.d("onViewCreated: Item count:${musicAdapter.itemCount}")
                    }
                }
            }
        }

        private fun setupOnPermissionGranted() {
            musicVm.permissionGranted.observe(viewLifecycleOwner) { isGranted ->
                if (isGranted) {
                    callRefreshAdapter()
                }
            }
        }

        private fun callRefreshAdapter() {
            musicAdapter.refresh()
        }

        private fun musicItemClicked(): (Music, Int) -> Unit = { music, position ->
            musicVm.updateCurrentMusic(music)
            musicVm.markSongAsSelected()
            Timber.d("musicItemClicked: ${music.title}")
            playExoPlayer(position)
            navigateToNowPlaying()
        }

        private fun updatePosition(): (Int) -> Unit = {
            musicVm.updateCurrentPosition(it)
        }

        private fun navigateToNowPlaying() {
            findNavController().navigate(R.id.action_listFrag_to_nowPlayingFrag)
        }

        private fun setupCurrentSongLd() {
            Timber.d("setupCurrentSongLd: DataStoredInLD ${musicVm.currentMusic.value} ")
            musicVm.currentMusic.observe(viewLifecycleOwner) {
                Timber.d("setupCurrentSongLd: music:$it")
            }
        }

        private fun setupMiniPlayerVisibility() {
            if (songPlayedOnce) {
                binding.miniPlayerLayout.visibility = View.VISIBLE
            } else {
                binding.miniPlayerLayout.visibility = View.GONE
            }
        }

        private fun setupMiniPlayer() {
            binding.miniPlayerLayout.setOnClickListener {
                navigateToNowPlaying()
            }

            binding.playButMini.setOnClickListener {
                if (songPlayedOnce) {
                    if(musicVm.isSongSuppressed.value==true){
                        Toast.makeText(requireContext(),"Can't Play During Call",Toast.LENGTH_SHORT).show()
                    }else if (controllerFuture.get().isPlaying()) {
                        controllerFuture.get().pause()
                        binding.playButMini.setImageResource(R.drawable.small_play_icon)
                    } else {
                            controllerFuture.get().play()
                            binding.playButMini.setImageResource(R.drawable.small_pause_icon)
                        }
                    }
                }

        }

        private fun updateMiniPlayerUI() {
            if (songPlayedOnce) {
                Glide.with(this)
                    .load(controllerFuture.get().mediaMetadata.artworkUri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.no_music_icon)
                    .into(binding.musicIconMini)
                binding.songTitleMini.text = controllerFuture.get().mediaMetadata.title

            if(musicVm.isSongSuppressed.value==true){
                binding.playButMini.setImageResource(R.drawable.small_play_icon)
            }else if (controllerFuture.get().isPlaying) {
                        binding.playButMini.setImageResource(R.drawable.small_pause_icon)
                    }
                else {
                    binding.playButMini.setImageResource(R.drawable.small_play_icon)
                }
            }
        }

        private fun playExoPlayer(position: Int) {
            // TODO: Maybe we don't need coroutines here
            CoroutineScope(Dispatchers.Main).launch {
                if (controllerFuture.get().isPlaying) {
                    controllerFuture.get().pause()
                    controllerFuture.get().seekTo(position, 0)

                } else {
                        CoroutineScope(Dispatchers.Main).launch {

                            controllerFuture.get().setMediaItems(musicVm.mediaItemForExoplayer())
                            controllerFuture.get().seekTo(position, 0)
                        }

                }
                controllerFuture.get().prepare()
                controllerFuture.get().play()

            }
        }

        private fun lastSongInExoPlayer(position: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                if (controllerFuture.get().isPlaying) {
                    controllerFuture.get().pause()
                    controllerFuture.get().seekTo(position, 0)

                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        controllerFuture.get().setMediaItems(musicVm.mediaItemForExoplayer())
                        controllerFuture.get().seekTo(position, 0)

                    }
                }
                controllerFuture.get().prepare()
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            if (isPlayerListnerAdded) {
                controllerFuture.get().removeListener(playerListener)
            }

            _binding = null
        }


    }
