package ca.josue_lubaki.kmovies.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

internal interface Dispatcher {
    val io : CoroutineDispatcher
}
internal expect fun provideDispatcher() : Dispatcher
