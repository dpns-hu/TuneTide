package com.tunetide.music.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tunetide.music.R
import com.tunetide.music.databinding.FragmentBlankBinding

class BlankFragment : Fragment() {
    private var _binding: FragmentBlankBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        printScreenWidth()
        binding.but1.setOnClickListener {
            navigateToListFrag()
        }
        val title = "Adaptive Title"
        val subtitle = "Adaptive Subtitle"
        binding.includedSongItem.musicTitle.text = title
        binding.includedSongItem.musicSubtitle.text = subtitle

        binding.but2.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
        }
    }

    private fun printScreenWidth() {
        val configuration: Configuration = this.requireActivity().resources.configuration
        val screenWidthDp: Int = configuration.screenWidthDp
        val smallestScreenWidthDp: Int = configuration.smallestScreenWidthDp
        val data = "screenWidthDp:$screenWidthDp\n" +
                "smallestScreenWidthDp:$smallestScreenWidthDp"
        binding.tv1.text = data
    }

    private fun navigateToListFrag() {
        findNavController().navigate(R.id.action_blankFragment_to_listFrag)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}