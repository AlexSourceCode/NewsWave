package com.example.newswave.presentation.viewModels

import androidx.lifecycle.ViewModel
import com.example.newswave.domain.usecases.user.IsUserDataUpdatedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class SessionViewModel @Inject constructor(
    isUserDataUpdatedUseCase: IsUserDataUpdatedUseCase
): ViewModel() {

    val refreshEvent: SharedFlow<Unit> = isUserDataUpdatedUseCase()


}