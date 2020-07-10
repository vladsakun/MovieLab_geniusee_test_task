package com.example.movielab.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.movielab.convertMovieListResponseToListOfEntities
import com.example.movielab.data.db.MovieLabDatabase
import com.example.movielab.data.db.dao.MovieDao
import com.example.movielab.data.db.entity.MovieEntity
import com.example.movielab.data.network.MovieNetworkDataSource
import com.example.movielab.data.network.MovieNetworkDataSourceImpl
import com.example.movielab.data.response.CastResponse
import com.example.movielab.data.response.MovieDetailResponse
import com.example.movielab.data.response.MovieListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MovieRepositoryImpl(
    private var context: Context
) : MovieRepository {

    private val movieDao: MovieDao = MovieLabDatabase.getAppDataBase(context)!!.movieDao()
    private val movieNetworkDataSource: MovieNetworkDataSource = MovieNetworkDataSourceImpl(context.applicationContext)

    init {
        context = context.applicationContext

        movieNetworkDataSource.apply {

            //Set observer on fetched popular movies
            downloadedMovieList.observeForever { newMovieResponse ->
                persistFetchedMovies(newMovieResponse)
            }

        }
    }

    //Add new movies to local db
    private fun persistFetchedMovies(newMovieResponse: MovieListResponse) {
        GlobalScope.launch(Dispatchers.IO) {
            movieDao.upsert(convertMovieListResponseToListOfEntities(newMovieResponse))
        }
    }

    //Select all movies from db and return them
    override suspend fun getMovieList(): LiveData<List<MovieEntity>> {
        return withContext(Dispatchers.IO) {
            initMovieData()
            return@withContext movieDao.getListOfMovies()
        }
    }

    private suspend fun initMovieData() {
        fetchMovies()
    }

    //Fetch movies from api
    private suspend fun fetchMovies() {
        movieNetworkDataSource.fetchMovieList()
    }

    override suspend fun searchMovies(query: String) {
        movieNetworkDataSource.searchMovie(query)
    }

    override suspend fun fetchMovie(movieId: Double) {
        movieNetworkDataSource.getMovie(movieId)
    }

    override suspend fun getMovie(): LiveData<out MovieDetailResponse> {
        return withContext(Dispatchers.IO) {
            movieNetworkDataSource.movie
        }
    }

    override suspend fun getSearchedMovies(): LiveData<out List<MovieEntity>> {
        return withContext(Dispatchers.IO) {
            return@withContext movieNetworkDataSource.searchedMovieList
        }
    }

    override suspend fun fetchCast(movieId: Double) {
        movieNetworkDataSource.getCast(movieId)
    }

    override suspend fun getCast(): LiveData<out CastResponse> {
        return withContext(Dispatchers.IO) {
            movieNetworkDataSource.cast
        }
    }
}