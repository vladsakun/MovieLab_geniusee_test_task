package com.example.movielab.ui.movielist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.movielab.data.repository.MovieRepository
import com.example.movielab.data.repository.MovieRepositoryImpl

class MovieListViewModelFactory(
    val context: Context
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MovieListViewModel(context.applicationContext) as T
    }
}