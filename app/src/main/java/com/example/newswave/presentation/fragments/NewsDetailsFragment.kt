package com.example.newswave.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.databinding.FragmentNewsDetailsBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.utils.DateUtils
import com.squareup.picasso.Picasso


class NewsDetailsFragment : Fragment() {
    private lateinit var binding: FragmentNewsDetailsBinding
    private val args by navArgs<NewsDetailsFragmentArgs>()

    private lateinit var player: ExoPlayer

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
                DateUtils.dateFormat(requireActivity().application, args.news.publishDate)

            val list = args.news.author.split(",")
            Log.d("ListAuthors", args.news.author)
            val spinner = binding.srAuthors
            val spinnerAdapter = ArrayAdapter(requireActivity().application, R.layout.spinner_item,list)
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            spinner.adapter = spinnerAdapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    Log.d("TempLog", "TempLog")
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    Log.d("TempLog", "TempLog")
                }
            }

            Picasso.get()
                .load(args.news.image)
                .resize(800,600)
                .into(ivImage)

            args.news.video?.let { videoUrl ->
                if (!videoUrl.endsWith("m3u8")){
                    playerView.visibility = View.VISIBLE
                    val mediaItem = MediaItem.fromUri(Uri.parse(args.news.video))
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.playWhenReady = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
        binding.playerView.player = null
    }

}