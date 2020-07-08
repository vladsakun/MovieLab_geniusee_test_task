package com.example.movielab.data.network

import androidx.lifecycle.LiveData
import com.example.movielab.data.db.entity.MovieEntity
import com.example.movielab.data.response.CastResponse
import com.example.movielab.data.response.MovieDetailResponse
import com.example.movielab.data.response.MovieListResponse

interface MovieNetworkDataSource {
    
    //Fetch trending movies
    val downloadedMovieList: LiveData<MovieListResponse>
    suspend fun fetchMovieList()

    //Search movies
    val searchedMovieList: LiveData<out List<MovieEntity>>
    suspend fun searchMovie(query: String)

    //Get movie details
    val movie:LiveData<out MovieDetailResponse>
    suspend fun getMovie(movieId: Double)

    //Get cast details of movie
    val cast:LiveData<out CastResponse>
    suspend fun getCast(movieId: Double)
}