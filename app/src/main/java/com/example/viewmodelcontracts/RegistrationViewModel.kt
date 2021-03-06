package com.example.viewmodelcontracts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.viewmodelcontracts.email.EmailEntryViewModel
import com.example.viewmodelcontracts.interest.InterestSubmissionViewModel
import com.example.viewmodelcontracts.progress.Progress
import com.example.viewmodelcontracts.progress.ProgressViewModelContract
import com.example.viewmodelcontracts.username.UsernameEntryViewModel


class RegistrationViewModel : ViewModel(),
    ProgressViewModelContract,
    UsernameEntryViewModel,
    EmailEntryViewModel,
    InterestSubmissionViewModel {

    private val _registrationState = MutableLiveData<RegistrationState>()
    private val _shouldSubmitGenereSelections = MutableLiveData<Boolean>()

    val registrationState: LiveData<RegistrationState> = _registrationState

    override val registrationProgress: LiveData<Progress> =
        Transformations.map(_registrationState) { state: RegistrationState ->
            Progress(
                userName = state.userData.username.isNotBlank(),
                email = state.userData.email.isNotBlank(),
                genres = state.userData.interests.isNotEmpty()
            )
        }

    init {
        _registrationState.value = RegistrationState.UserNameEntry(RegistrationData())
    }

    override fun updateUsername(username: String) {
        _registrationState.value?.userData?.username = username
    }

    override fun updateEmail(email: String) {
        _registrationState.value?.userData?.email = email
    }

    override val shouldSubmitGenreSelections: LiveData<Boolean>
        get() = _shouldSubmitGenereSelections

    override fun submitGenreSelections(genres: List<String>) {
        val userData = _registrationState.value?.userData.apply {
            this?.interests = genres
        }
        _registrationState.value = userData?.let { RegistrationState.Complete(it) }
    }

    fun onNext() {
        val userData = _registrationState.value?.userData ?: RegistrationData()
        when (_registrationState.value) {
            is RegistrationState.UserNameEntry -> {
                _registrationState.value = RegistrationState.EmailEntry(userData)
            }
            is RegistrationState.EmailEntry -> {
                _registrationState.value = RegistrationState.GenreSelection(userData)
            }
            is RegistrationState.GenreSelection -> {
                _shouldSubmitGenereSelections.value = true
            }
        }
    }

    fun onBack() {
        val userData = _registrationState.value?.userData ?: RegistrationData()
        when (_registrationState.value) {
            is RegistrationState.EmailEntry -> {
                _registrationState.value = RegistrationState.UserNameEntry(userData)
            }
            is RegistrationState.GenreSelection -> {
                _registrationState.value = RegistrationState.EmailEntry(userData)
            }
            else -> { /* no-op */ }
        }
    }
}