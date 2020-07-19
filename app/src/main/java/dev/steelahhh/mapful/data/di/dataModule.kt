package dev.steelahhh.mapful.data.di

import dev.steelahhh.mapful.data.SocketRepository
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val ASM_TAG = "SOCKET_ASM"

val dataModule = module {
    single(named(ASM_TAG)) { ActorSelectorManager(Dispatchers.IO) }

    single {
        aSocket(
            get<ActorSelectorManager>(named(ASM_TAG))
        ).tcp()
    }

    single { SocketRepository(get()) }
}
