package ca.josue_lubaki.kmovies.util

import ca.josue_lubaki.kmovies.di.getSharedModule
import org.koin.core.context.startKoin

/**
 * created by Josue Lubaki
 * date : 2023-05-02
 * version : 1.0.0
 */

fun initKoin() {
    startKoin {
        modules(getSharedModule())
    }
}