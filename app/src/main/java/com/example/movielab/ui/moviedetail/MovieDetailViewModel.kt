package com.example.movielab.ui.moviedetail

import androidx.lifecycle.ViewModel
import com.example.movielab.data.repository.MovieRepository
import com.example.movielab.internal.lazyDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private  val movieRepository: MovieRepository
) : ViewModel() {

    val movie by lazyDeferred {
        movieRepository.getMovie()
    }

    fun fetchMovie(movieId: Double){
        GlobalScope.launch {
            movieRepository.fetchMovie(movieId)
        }
    }

    val cast by lazyDeferred {
        movieRepository.getCast()
    }

    fun fetchCast(movieId: Double){
        GlobalScope.launch {
            movieRepository.fetchCast(movieId)
        }
    }
}