package com.example.newswave.presentation.fragments

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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentForgotPasswordBinding
import com.example.newswave.databinding.FragmentNewsDetailsBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.adapters.CustomArrayAdapter
import com.example.newswave.presentation.state.AuthState
import com.example.newswave.presentation.viewModels.NewsDetailsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.utils.DateUtils
import com.example.newswave.utils.LocaleHelper
import com.example.newswave.utils.NetworkUtils
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Класс, представляющий экран с деталями новости
 */
class NewsDetailsFragment : Fragment() {

    private var _binding: FragmentNewsDetailsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<NewsDetailsFragmentArgs>()
    private val viewModel: NewsDetailsViewModel by viewModels { viewModelFactory }
    private lateinit var player: ExoPlayer

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
        _binding = FragmentNewsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlayer()
        setupUI()
        observeViewModel()
    }

    // Инициализация видеоплеера
    private fun initializePlayer() {
        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
    }

    // Настройка пользовательского интерфейса
    private fun setupUI() {
        setupTextViews()
        setupSpinner()
        setupImage()
        setupVideoPlayer()
        setupDefaultButton()
        (activity as MainActivity).setSelectedMenuItem(args.currentBottomItem)
    }

    // Настройка кнопки подписки
    private fun setupDefaultButton() {
        binding.btSubscription.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        if (LANGUAGE_RU == LocaleHelper.getSystemLanguage()) {
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribe_rus)
        } else {
            binding.btSubscription.setBackgroundResource(R.drawable.button_subscribe)
        }
    }

    // Настройка текстовых полей
    private fun setupTextViews() {
        with(binding) {
            tvText.text = args.news.text
            tvTitle.text = args.news.title
            tvDate.text = DateUtils.dateFormat(requireActivity().application, args.news.publishDate)
        }
    }

    // Настройка выпадающего списка с авторами
    private fun setupSpinner() {
        val authorsList = args.news.author.split(",")
        val customAdapter =
            CustomArrayAdapter(requireContext(), R.layout.spinner_item, authorsList).apply {
                setDropDownViewResource(R.layout.spinner_dropdown_item)
            }

        with(binding.srAuthors) {
            adapter = customAdapter
            args.author?.let { preSelectAuthor(it, customAdapter) }
            onItemSelectedListener = createSpinnerItemSelectedListener()
            createSpinnerItemSelectedListener()
        }
    }

    // Предварительный выбор автора в списке
    private fun preSelectAuthor(author: String, adapter: CustomArrayAdapter) {
        for (pos in 0 until adapter.count) {
            if (adapter.getItem(pos) == author) {
                binding.srAuthors.setSelection(pos)
                break
            }
        }
    }

    // Создание слушателя выбора элемента в списке
    private fun createSpinnerItemSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position != 0) {
                val selectedAuthor = parent?.getItemAtPosition(position).toString()
                viewModel.checkAuthorInRepository(selectedAuthor)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            Log.d(LOG_TAG, "No author selected in spinner");
        }
    }

    // Настройка изображения
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

    // Настройка видеоплеера
    private fun setupVideoPlayer() {
        args.news.video?.let { videoUrl ->
            if (!videoUrl.endsWith(M3U8_FORMAT)) {
                binding.playerView.visibility = View.VISIBLE
                val mediaItem = MediaItem.fromUri(Uri.parse(args.news.video))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = false
            }
        }
    }

    // Наблюдение за состоянием ViewModel
    private fun observeViewModel() {
        if (args.news.author == EMPTY_CATEGORY) {
            updateUIForUnknownAuthor()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collectLatest { isAuth ->
                    when (isAuth) {
                        is AuthState.LoggedIn -> {
                            handleLoggedInState()
                            launch { observeAuthorState() }
                        }

                        is AuthState.LoggedOut -> handleLoggedOutState()
                    }
                }
            }
        }
    }

    // Действия при авторизованном пользователе
    private fun handleLoggedInState() {
        binding.btSubscription.setOnClickListener {
            if (!viewModel.isInternetConnection(requireContext())) {
                showToastErrorInternetConnection()
                return@setOnClickListener
            }
            val author = binding.srAuthors.selectedItem.toString()
            val isSubscribed =
                binding.btSubscription.text.toString() != getString(R.string.subscribe)
            if (isSubscribed) showUnsubscribeDialog(author)
            else viewModel.subscribeOnAuthor(author)
        }
    }

    // Действия при неавторизованном пользователе
    private fun handleLoggedOutState() {
        binding.btSubscription.visibility = View.VISIBLE
        binding.btSubscription.setOnClickListener { requestLoginForSubscription() }
        if (findNavController().currentDestination?.id == R.id.newsDetailsFragment3) {
            findNavController().popBackStack(R.id.subscribedAuthorsFragment, false)
        }
    }

    // Показ сообщения об отсутствии подключения к интернету
    private fun showToastErrorInternetConnection() {
        Toast.makeText(
            requireContext(),
            getString(R.string.no_internet_connection),
            Toast.LENGTH_SHORT
        ).show()
    }

    // Запрос входа для подписки
    private fun requestLoginForSubscription() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.subscription_prompt_message))
            .setMessage(getString(R.string.sign_in_required_message))
            .setPositiveButton(getString(R.string.sign_in_text)) { dialog, _ ->
                launchSignInFragment()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Наблюдение за состоянием автора
    private suspend fun observeAuthorState() {
        viewModel.checkAuthorInRepository(args.news.author)
        viewModel.authorState.collectLatest { isFavorite -> // возможно нужен запуск в launch
            if (viewModel.isInternetConnection(requireContext())) {
                if (isFavorite != null) {
                    updateSubscriptionButton(isFavorite)
                }
            } else {
                updateSubscriptionButton(false)
                showToastErrorInternetConnection()
            }
        }
    }

    // Показ диалога для отмены подписки
    private fun showUnsubscribeDialog(author: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirmation))
            .setMessage(getString(R.string.alert_dialog_question, author))
            .setPositiveButton(getString(R.string.unsubscribe)) { dialog, _ ->
                viewModel.unsubscribeFromAuthor(author)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Обновление состояния кнопки подписки
    private fun updateSubscriptionButton(isFavorite: Boolean) {
        if (isFavorite) setSubscribedButton() else setUnsubscribedButton()
        binding.btSubscription.visibility = View.VISIBLE
    }

    // Обновление интерфейса для неизвестного автора
    private fun updateUIForUnknownAuthor() {
        with(binding) {
            btSubscription.visibility = View.GONE
            srAuthors.visibility = View.GONE
            tvAuthorUnknown.visibility = View.VISIBLE
        }
    }

    // Настройка кнопки для автора, на которого подписан пользователь
    private fun setSubscribedButton() {
        with(binding.btSubscription) {
            text = getString(R.string.subscribed)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            setBackgroundResource(
                if (LocaleHelper.getSystemLanguage() == LANGUAGE_RU) {
                    R.drawable.button_subscribed_rus
                } else {
                    R.drawable.button_subscribed
                }
            )
        }
    }

    // Настройка кнопки для автора, на которого не подписан пользователь
    private fun setUnsubscribedButton() {
        with(binding.btSubscription) {
            text = getString(R.string.subscribe)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            setBackgroundResource(
                if (LocaleHelper.getSystemLanguage() == LANGUAGE_RU) {
                    R.drawable.button_subscribe_rus
                } else {
                    R.drawable.button_subscribe
                }
            )
        }
    }

    // Освобождение ресурсов при уничтожении представления
    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
        binding.playerView.player = null
        _binding = null
    }

    override fun onDestroy() { // crutch
        super.onDestroy()
        viewModel.clearState()
    }

    // Запуск фрагмента авторизации
    private fun launchSignInFragment() {
        binding.btSubscription.visibility = View.GONE
        findNavController().navigate(
            NewsDetailsFragmentDirections.actionNewsDetailsFragmentToLoginFragment(R.id.topNewsFragment)
        )
    }

    companion object {
        private const val EMPTY_CATEGORY = "unknownAuthor"
        private const val LANGUAGE_RU = "ru";
        private const val M3U8_FORMAT = "m3u8";
        private const val LOG_TAG = "NewsDetailsLog"
    }
}
