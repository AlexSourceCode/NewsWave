package com.example.newswave.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentNewsDetailsBinding
import com.example.newswave.presentation.state.AuthState
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.viewModels.NewsDetailsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.presentation.adapters.CustomArrayAdapter
import com.example.newswave.utils.DateUtils
import com.example.newswave.utils.LocaleHelper
import com.example.newswave.utils.NetworkUtils
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class NewsDetailsFragment : Fragment() {
    private lateinit var binding: FragmentNewsDetailsBinding
    private val args by navArgs<NewsDetailsFragmentArgs>()

    private lateinit var player: ExoPlayer


    private val viewModel: NewsDetailsViewModel by viewModels { viewModelFactory }

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
        inflateFragment()
        observeViewModel()

        val menuItemId = args.currentBottomItem
        (activity as MainActivity).setSelectedMenuItem(menuItemId)
    }

    private fun inflateFragment() {
        setupTextViews()
        setupSpinner()
        setupImage()
        setupVideoPlayer()
        setupDefaultButton()
    }

    private fun setupDefaultButton() {
        binding.btSubscription.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        if ("RU" == LocaleHelper.getSystemLanguage()) {
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribe_rus)
        } else {
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribe)
        }
    }

    private fun setupTextViews() {
        with(binding) {
            tvText.text = args.news.text
            tvTitle.text = args.news.title
            tvDate.text = DateUtils.dateFormat(requireActivity().application, args.news.publishDate)
        }
    }

    private fun setupSpinner() {
        val list = args.news.author.split(",")
        val spinner = binding.srAuthors

        val customAdapter =
            CustomArrayAdapter(requireContext(), R.layout.spinner_item, list)
        customAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = customAdapter

        spinner.adapter = customAdapter
        if (!args.author.isNullOrBlank()) {
            for (pos in 0 until customAdapter.count) {
                if (customAdapter.getItem(pos).toString() == args.author) {
                    spinner.setSelection(pos)
                }
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("ResourceAsColor")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 != 0) {
                    Log.d("onItemSelectedListenerState", "fsfsd")
                    val selectedItem = p0?.getItemAtPosition(p2).toString()
                    viewModel.checkAuthorInRepository(selectedItem)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Log.d("TempLog", "TempLog")
            }
        }
    }

    private fun setupImage() {
        if (NetworkUtils.isNetworkAvailable(requireActivity().application)) {
            Picasso.get()
                .load(args.news.image)
                .resize(800, 600)
                .into(binding.ivImage)
        } else {
            binding.ivImage.setImageResource(R.drawable.error_placeholder)
        }
    }

    private fun setupVideoPlayer() {
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

    override fun onDestroy() { // crutch
        super.onDestroy()
        viewModel.clearState() // crutch
    }

    private fun observeViewModel() {
        if (args.news.author == EMPTY_CATEGORY) {
            updateUIForUnknownAuthor()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch { // где надо и не надо collectlatest
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.user.collectLatest { isAuth ->
                    Log.d("NewsDetailsFragmentState", isAuth.toString())
                    handleAuthState(isAuth)
                    if (isAuth is AuthState.LoggedOut) {
                        if (findNavController().currentDestination?.id == R.id.newsDetailsFragment3) {
                            findNavController().popBackStack(
                                R.id.subscribedAuthorsFragment,
                                false
                            )
                        }
                        binding.btSubscription.visibility = View.VISIBLE
                        return@collectLatest
                    }
                    viewModel.checkAuthorInRepository(args.news.author)
                    viewModel.stateAuthor.collectLatest { isFavorite -> // возможно нужен запуск в launch
                        Log.d("NewsDetailsFragmentState", isFavorite.toString())
                        if (viewModel.isInternetConnection(requireContext())){
                            if (isFavorite != null) {
                                updateSubscriptionButton(isFavorite)
                            }
                        } else{
                            updateSubscriptionButton(false)
                            Log.d("CheckErrorMessage", "execute toast in newsdetails")
                            showToastErrorInternetConnection()
                        }

                    }
                }
            }
        }
    }


    private fun handleAuthState(isAuth: AuthState) {
            binding.btSubscription.setOnClickListener {
                if (!viewModel.isInternetConnection(requireContext())){
                    showToastErrorInternetConnection()
                    return@setOnClickListener
                }
                    val checkSubscribed = binding.btSubscription.text.toString()
                val author = binding.srAuthors.selectedItem.toString()
                val btSub = requireActivity().getString(R.string.subscribe)
                when (isAuth) {
                    is AuthState.LoggedIn -> {
                        if (checkSubscribed == btSub) viewModel.subscribeOnAuthor(author)
                        else showUnsubscribeDialog(author)
                    }

                    is AuthState.LoggedOut -> requestLoginForSubscription()
                }
            }

    }

    private fun showToastErrorInternetConnection() {
        Toast.makeText(
            requireContext(),
            getString(R.string.no_internet_connection),
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun requestLoginForSubscription() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.subscription_prompt_message))
            .setMessage(getString(R.string.sign_in_required_message))
            .setPositiveButton(getString(R.string.sign_in_text)) { dialog, _ ->
                launchSignInFragment()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showUnsubscribeDialog(author: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.confirmation))
        builder.setMessage(getString(R.string.alert_dialog_question, author))

        builder.setPositiveButton(getString(R.string.unsubscribe)) { dialog, _ ->
            viewModel.unsubscribeFromAuthor(author)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun updateSubscriptionButton(isFavorite: Boolean) {
        if (isFavorite) {
            setSubscribedButton()
            binding.btSubscription.visibility = View.VISIBLE
        } else {
            setUnsubscribedButton()
            binding.btSubscription.visibility = View.VISIBLE
        }
    }

    private fun updateUIForUnknownAuthor() {
        binding.btSubscription.visibility = View.GONE
        binding.srAuthors.visibility = View.GONE
        binding.tvAuthorUnknown.visibility = View.VISIBLE
    }


    private fun setSubscribedButton() {
        binding.btSubscription.text = getString(R.string.subscribed)
        binding.btSubscription.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        if ("ru" == LocaleHelper.getSystemLanguage()) {
            Log.d("setSubscribedButton", "execute ed ru")
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribed_rus)
        } else {
            Log.d("setSubscribedButton", "execute ed en")
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribed)
        }
    }

    private fun setUnsubscribedButton() {
        binding.btSubscription.text = getString(R.string.subscribe)
        binding.btSubscription.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        if ("ru" == LocaleHelper.getSystemLanguage()) {
            Log.d("setSubscribedButton", "execute e ru")
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribe_rus)
        } else {
            Log.d("setSubscribedButton", "execute e ru")
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribe)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
        binding.playerView.player = null
    }

    private fun launchSignInFragment() {
        binding.btSubscription.visibility = View.GONE
        findNavController().navigate(
            NewsDetailsFragmentDirections.actionNewsDetailsFragmentToLoginFragment(R.id.topNewsFragment)
        )
    }

    companion object {
        private const val EMPTY_CATEGORY = "unknownAuthor"
    }
}
