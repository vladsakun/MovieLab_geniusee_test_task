package com.example.movielab.ui.movielist

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movielab.R
import com.example.movielab.adapters.ItemOffsetDecoration
import com.example.movielab.adapters.MovieListAdapter
import com.example.movielab.data.db.entity.MovieEntity
import com.example.movielab.data.network.ConnectivityReceiver
import com.example.movielab.ui.ScopedActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.custom_progress_bar.*
import kotlinx.android.synthetic.main.movie_list_activity.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance


class MovieListActivity : ScopedActivity(), KodeinAware,
    ConnectivityReceiver.ConnectivityReceiverListener {

    //Kodein for dependency injections
    override val kodein by closestKodein()

    //ViewModelFactory and ViewModel
    private val viewModelFactory: MovieListViewModelFactory by instance()
    private lateinit var viewModel: MovieListViewModel

    private lateinit var adapter: MovieListAdapter

    //List of movies
    private var movieEntityList = listOf<MovieEntity>()

    private var snackbar: Snackbar? = null

    //Connectivity status receiver
    private lateinit var receiver: ConnectivityReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.movie_list_activity)

        receiver = ConnectivityReceiver()

        registerReceiver(
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        //Init MovieListViewModel with MovieListViewModelFactory
        viewModel = ViewModelProvider(this, viewModelFactory).get(MovieListViewModel::class.java)

        adapter = MovieListAdapter(this, this, movieEntityList)

        //Initialize RecyclerView
        recyclerview.setHasFixedSize(true)
        recyclerview.adapter = adapter
        recyclerview.layoutManager =
            GridLayoutManager(this, resources.getInteger(R.integer.movie_columns))

        //Create ItemDecoration spacing between elements for GridLayoutManager
        val itemDecoration = ItemOffsetDecoration(this, R.dimen.item_offset)

        recyclerview.addItemDecoration(itemDecoration)

        //Bind UI for showing movies
        bindUI()

    }

    //Show snackbar on connectivity changed
    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {

            //Make snackbar
            snackbar = Snackbar.make(
                findViewById(R.id.root_layout),
                getString(R.string.offline_message),
                Snackbar.LENGTH_LONG
            )
            snackbar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            snackbar?.show()
        } else {
            snackbar?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()

        //Set listener for connectivity status change
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    private fun bindUI() = launch {
        val movieList = viewModel.movieList.await()

        movieList.observe(this@MovieListActivity, Observer {
            if (it == null) return@Observer

            updateMovieAdapter(it)
            progressBar.visibility = View.GONE
        })
    }

    private fun updateMovieAdapter(it: List<MovieEntity>) {
        adapter.moviesArrayList = it
        adapter.notifyDataSetChanged()
    }
}
