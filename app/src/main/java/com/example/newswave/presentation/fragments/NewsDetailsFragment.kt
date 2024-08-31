package com.example.newswave.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.databinding.FragmentNewsDetailsBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.utils.DateAndTextUtils
import com.squareup.picasso.Picasso


class NewsDetailsFragment : Fragment() {
    private lateinit var binding: FragmentNewsDetailsBinding
    private val args by navArgs<NewsDetailsFragmentArgs>()

    private lateinit var player: ExoPlayer



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsDetailsBinding.inflate(layoutInflater)
        (activity as MainActivity).setSelectedMenuItem(R.id.topNewsFragment)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player

        inflateFragment()
    }

    private fun inflateFragment(){
        with(binding) {
            tvText.text = args.news.text
            tvTitle.text = args.news.title
            tvDate.text =
                DateAndTextUtils.dateFormat(requireActivity().application, args.news.publishDate)
            tvAuthors.text = args.news.author
            Picasso.get().load(args.news.image).into(ivImage)

            if ((args.news.video != null) && args.news.video.toString().takeLast(4) != "m3u8") {
                playerView.visibility = View.VISIBLE
                val mediaItem = MediaItem.fromUri(Uri.parse(args.news.video))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
    }

}