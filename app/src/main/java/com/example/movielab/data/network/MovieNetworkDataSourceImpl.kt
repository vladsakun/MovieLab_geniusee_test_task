package com.example.movielab.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movielab.convertMovieListResponseToListOfEntities
import com.example.movielab.data.db.entity.MovieEntity
import com.example.movielab.data.exceptions.NoConnectivityException
import com.example.movielab.data.response.CastResponse
import com.example.movielab.data.response.MovieDetailResponse
import com.example.movielab.data.response.MovieListResponse

class MovieNetworkDataSourceImpl(
    private val movieApi: MovieApiService
) : MovieNetworkDataSource {

    private val CONNECTIVITY_TAG = "Connectivity"

    private val _downloadedMovieList = MutableLiveData<MovieListResponse>()
    override val downloadedMovieList: LiveData<MovieListResponse>
        get() = _downloadedMovieList

    //Fetch popular movie list
    override suspend fun fetchMovieList() {
        try {
            val fetchedSunInfo = movieApi.getTrendingMovies().await()
            _downloadedMovieList.postValue(fetchedSunInfo)
        } catch (e: NoConnectivityException) {
            Log.e(CONNECTIVITY_TAG, "No internet connection", e)
        }
    }

    private val _searchedMovieList = MutableLiveData<List<MovieEntity>>()
    override val searchedMovieList: LiveData<out List<MovieEntity>>
        get() = _searchedMovieList

    //Search movie
    override suspend fun searchMovie(query: String) {
        try {
            val fetchedMovies = movieApi.serchMovie(query).await()
            _searchedMovieList.postValue(convertMovieListResponseToListOfEntities(fetchedMovies))
        } catch (e: NoConnectivityException) {
            Log.e(CONNECTIVITY_TAG, "No internet connection: ", e)
        }
    }

    private val _movie = MutableLiveData<MovieDetailResponse>()
    override val movie: LiveData<out MovieDetailResponse>
        get() = _movie

    //Get movie
    override suspend fun getMovie(movieId: Double) {
        try {
            val fetchedMovie = movieApi.getMovie(movieId).await()
            _movie.postValue(fetchedMovie)
        } catch (e: NoConnectivityException) {
            Log.e(CONNECTIVITY_TAG, "No internet connection: ", e)
        }
    }

    private val _cast = MutableLiveData<CastResponse>()
    override val cast: LiveData<out CastResponse>
        get() = _cast

    //Get cast
    override suspend fun getCast(movieId: Double) {
        try {
            val fetchedCast = movieApi.getCast(movieId).await()
            _cast.postValue(fetchedCast)
        } catch (e: NoConnectivityException) {
            Log.e(CONNECTIVITY_TAG, "No internet connection: ", e)
        }
    }
}