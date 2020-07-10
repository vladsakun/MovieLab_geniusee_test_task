package com.example.movielab.ui.moviedetail

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.movielab.data.repository.MovieRepository
import com.example.movielab.data.repository.MovieRepositoryImpl
import com.example.movielab.internal.lazyDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    context: Context
) : ViewModel() {

    private val movieRepository: MovieRepository = MovieRepositoryImpl(context.applicationContext)

    val movie by lazyDeferred {
        movieRepository.getMovie()
    }

    fun fetchMovie(movieId: Double) {
        GlobalScope.launch {
            movieRepository.fetchMovie(movieId)
        }
    }

    val cast by lazyDeferred {
        movieRepository.getCast()
    }

    fun fetchCast(movieId: Double) {
        GlobalScope.launch {
            movieRepository.fetchCast(movieId)
        }
    }
}