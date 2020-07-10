package com.example.movielab.ui.movielist

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.movielab.data.repository.MovieRepository
import com.example.movielab.data.repository.MovieRepositoryImpl
import com.example.movielab.internal.lazyDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Callback

class MovieListViewModel(
    context: Context
) : ViewModel() {

    private val movieRepository: MovieRepository = MovieRepositoryImpl(context.applicationContext)

    //Get movie list from db
    val movieList by lazyDeferred {
        movieRepository.getMovieList()
    }

    //Get searched movies list from api
    val searchedMovies by lazyDeferred {
        movieRepository.getSearchedMovies()
    }

    fun searchMovie(query: String) {
        GlobalScope.launch {
            movieRepository.searchMovies(query)
        }
    }
}