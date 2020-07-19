package dev.steelahhh.mapful.data

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalCoroutinesApi
class SocketRepository(
    private val socketBuilder: TcpSocketBuilder
) {
    companion object {
        private const val REMOTE_ADDRESS = "ios-test.printful.lv"
        private const val PORT = 6111

        const val AUTHORIZE_MESSAGE = "AUTHORIZE"
        const val USER_LIST_MESSAGE = "USERLIST"
        const val UPDATE_MESSAGE = "UPDATE"

        const val EMAIL = "a.efimenko72@gmail.com"
    }

    private val shouldConsume: AtomicBoolean = AtomicBoolean(false)
    private var socket: Socket? = null
    private val messages: MutableStateFlow<String> = MutableStateFlow("")

    private lateinit var input: ByteReadChannel
    private lateinit var output: ByteWriteChannel

    suspend fun startWithAuthorization(credentials: String) {
        shouldConsume.set(true)
        startClient { output ->
            output.writeStringUtf8("$AUTHORIZE_MESSAGE $credentials\n")
        }
    }

    fun stop() {
        shouldConsume.set(false)
        socket?.dispose()
    }

    fun messages(): Flow<String> = messages.filter { it.isNotEmpty() }

    private suspend fun startClient(doOnConnect: suspend (output: ByteWriteChannel) -> Unit) {
        socket = socketBuilder.connect(REMOTE_ADDRESS, PORT).apply {
            input = openReadChannel()
            output = openWriteChannel(autoFlush = true)

            doOnConnect(output)

            while (shouldConsume.get()) {
                try {
                    messages.value = input.readUTF8Line() ?: ""
                } catch (e: Exception) {
                    e.printStackTrace()
                    // fail silently here
                }
            }
        }
    }
}
