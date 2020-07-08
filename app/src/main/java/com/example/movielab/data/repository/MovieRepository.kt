package com.example.movielab.data.repository

import androidx.lifecycle.LiveData
import com.example.movielab.data.db.entity.MovieEntity
import com.example.movielab.data.response.CastResponse
import com.example.movielab.data.response.MovieDetailResponse

interface MovieRepository {

    suspend fun getMovieList():LiveData<out List<MovieEntity>>
    suspend fun getSearchedMovies():LiveData<out List<MovieEntity>>
    suspend fun getMovie():LiveData<out MovieDetailResponse>
    suspend fun getCast():LiveData<out CastResponse>

    suspend fun searchMovies(query: String)
    suspend fun fetchMovie(movieId: Double)
    suspend fun fetchCast(movieId: Double)
}