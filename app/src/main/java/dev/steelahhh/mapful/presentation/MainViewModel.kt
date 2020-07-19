package dev.steelahhh.mapful.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dev.steelahhh.mapful.data.SocketRepository
import dev.steelahhh.mapful.data.SocketRepository.Companion.UPDATE_MESSAGE
import dev.steelahhh.mapful.data.SocketRepository.Companion.USER_LIST_MESSAGE
import dev.steelahhh.mapful.data.User
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class MainViewModel(private val repository: SocketRepository) : ViewModel() {

    sealed class State {
        object Idle : State()
        data class New(val users: List<User>) : State()
        data class Update(val users: List<User>) : State()
    }

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    val state: StateFlow<State> = _state

    init {
        viewModelScope.launch {
            repository.messages().collect {
                it.handleUserList()
                it.handleUpdate()
            }
        }
    }

    fun start() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.startWithAuthorization(SocketRepository.EMAIL)
            }
        }
    }

    fun stop() {
        repository.stop()
    }

    private fun String.handleUserList() {
        if (startsWith(USER_LIST_MESSAGE)) {
            val response =
                removePrefix(USER_LIST_MESSAGE).trim().split(";").filter { it.isNotEmpty() }

            val users = response.map { userString ->
                val info = userString.trim().split(",").filter { it.isNotBlank() }
                User(
                    id = info[0].toInt(),
                    name = info[1],
                    avatar = info[2],
                    latLng = LatLng(info[3].toDouble(), info[4].toDouble())
                )
            }

            _state.value = State.New(users)
        }
    }

    private fun String.handleUpdate() {
        if (startsWith(UPDATE_MESSAGE)) {
            val response = removePrefix(UPDATE_MESSAGE).trim().split(",")

            val id = response[0].toInt()
            val latLng = LatLng(response[1].toDouble(), response[2].toDouble())

            val previous = _state.value

            _state.value = when (previous) {
                State.Idle -> emptyList()
                is State.New -> previous.users
                is State.Update -> previous.users
            }.let { users ->
                State.Update(
                    users.map {
                        if (it.id == id) it.copy(latLng = latLng) else it
                    }
                )
            }
        }
    }
}
