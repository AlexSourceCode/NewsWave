package com.example.newswave.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.databinding.FragmentNewsDetailsBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.app.NewsApp
import com.example.newswave.presentation.viewModels.NewsDetailsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.utils.DateUtils
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import javax.inject.Inject


class NewsDetailsFragment : Fragment() {
    private lateinit var binding: FragmentNewsDetailsBinding
    private val args by navArgs<NewsDetailsFragmentArgs>()

    private lateinit var player: ExoPlayer


    private lateinit var viewModel: NewsDetailsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsDetailsBinding.inflate(layoutInflater)
        if (args.author.isNullOrBlank()) (activity as MainActivity).setSelectedMenuItem(R.id.topNewsFragment)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
        viewModel = ViewModelProvider(this, viewModelFactory)[NewsDetailsViewModel::class.java]

        observeViewModel()
        setOnClickListener()
        inflateFragment()
    }

    private fun inflateFragment() {
        setupTextViews()
        setupSpinner()
        setupImage()
        setupVideoPlayer()
    }

    private fun setupTextViews() {
        with(binding) {
            tvText.text = args.news.text
            tvTitle.text = args.news.title
            tvDate.text = DateUtils.dateFormat(requireActivity().application, args.news.publishDate)
        }
    }

    private fun setupSpinner(){
        val list = args.news.author.split(",")
        val spinner = binding.srAuthors
        val spinnerAdapter =
            ArrayAdapter(requireActivity().application, R.layout.spinner_item, list)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        if (!args.author.isNullOrBlank()) {
            for (pos in 0 until spinnerAdapter.count) {
                if (spinnerAdapter.getItem(pos).toString() == args.author) {
                    spinner.setSelection(pos)
                }
            }
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("ResourceAsColor")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                viewModel.checkAuthorInRepository(selectedItem)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.d("TempLog", "TempLog")
            }
        }
    }

    private fun setupImage(){
        Picasso.get()
            .load(args.news.image)
            .resize(800, 600)
            .into(binding.ivImage)
    }

    private fun setupVideoPlayer(){
        args.news.video?.let { videoUrl ->
            if (!videoUrl.endsWith("m3u8")) {
                binding.playerView.visibility = View.VISIBLE
                val mediaItem = MediaItem.fromUri(Uri.parse(args.news.video))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = false
            }
        }
    }

    private fun updateSubscriptionButton(isFavorite: Boolean) {
        if (isFavorite) {
            binding.btSubscription.text = getString(R.string.subscribed)
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribed)
            binding.btSubscription.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        } else {
            binding.btSubscription.text = getString(R.string.subscribe)
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribe)
            binding.btSubscription.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.stateAuthor.collect { isFavorite ->
                    updateSubscriptionButton(isFavorite)
                }
            }
        }
    }


    private fun setOnClickListener() {
        binding.btSubscription.setOnClickListener {
            val checkSubscribed = binding.btSubscription.text.toString()
            val author = binding.srAuthors.selectedItem.toString()
            val btSub = requireActivity().getString(R.string.subscribe)
            if (checkSubscribed == btSub) {
                viewModel.subscribeOnAuthor(author)
            } else {
                viewModel.unsubscribeFromAuthor(author)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
        binding.playerView.player = null
    }

}
