package com.tunetide.music.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tunetide.music.databinding.FragmentArtistBinding
import com.tunetide.music.model.Music
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistFrag : Fragment() {
    private var _binding: FragmentArtistBinding? = null
    private val binding get() = _binding!!

    private val musicVm: MusicViewmodel by activityViewModels()
    private lateinit var adapter: ArtistAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ArtistAdapter(this.requireContext(), navigateToNowPlaying(), updatePosition())
        binding.rcyView.adapter = adapter
        setUpRefreshAllMusic()
    }

    private fun setUpRefreshAllMusic() {
        musicVm.allMusicList.observe(viewLifecycleOwner) { musicList ->
            if (musicList.isNotEmpty()) {
                adapter.submitList(musicList)
            }
        }
    }

    private fun navigateToNowPlaying(): (Music) -> Unit = { music ->
        musicVm.updateCurrentMusic(music)
//        findNavController().navigate(R.id.action_listFrag_to_nowPlayingFrag)
    }

    private fun updatePosition(): (Int) -> Unit = {
        musicVm.updateCurrentPosition(it)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}