package ca.josue_lubaki.kmovies.di

import ca.josue_lubaki.kmovies.data.api.MovieService
import ca.josue_lubaki.kmovies.data.datasource.RemoteDataSource
import ca.josue_lubaki.kmovies.data.datasourceimpl.RemoteDataSourceImpl
import ca.josue_lubaki.kmovies.data.repository.MovieRepositoryImpl
import ca.josue_lubaki.kmovies.domain.repository.MovieRepository
import ca.josue_lubaki.kmovies.domain.usecases.GetMovieUseCase
import ca.josue_lubaki.kmovies.domain.usecases.GetMoviesUseCase
import ca.josue_lubaki.kmovies.util.provideDispatcher
import org.koin.dsl.module

/**
 * created by Josue Lubaki
 * date : 2023-04-27
 * version : 1.0.0
 */

private val dataModule = module {
    factory { MovieService() }
    factory<RemoteDataSource> { RemoteDataSourceImpl(get(), get()) }
}

private val utilityModule = module {
    factory { provideDispatcher() }
}

private val domainModule = module {
    single<MovieRepository> { MovieRepositoryImpl(get()) }
    factory { GetMoviesUseCase() }
    factory { GetMovieUseCase() }
}

private val _sharedModule = listOf(dataModule, utilityModule, domainModule)

fun getSharedModule() = _sharedModule