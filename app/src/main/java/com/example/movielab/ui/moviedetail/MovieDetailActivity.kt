package com.example.movielab.ui.moviedetail

import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.movielab.MOVIE_MESSAGE
import com.example.movielab.R
import com.example.movielab.data.db.entity.MovieEntity
import com.example.movielab.data.network.ConnectivityReceiver
import com.example.movielab.data.response.Cast
import com.example.movielab.data.response.Crew
import com.example.movielab.data.response.Genre
import com.example.movielab.ui.ScopedActivity
import kotlinx.android.synthetic.main.activity_movie_detail.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder

class MovieDetailActivity : ScopedActivity(), KodeinAware{

    private val TAG = "MovieDetailActivity"

    //Kodein for dependency injections
    override val kodein by closestKodein()

    //ViewModelFactory
    private val viewModelFactory: MovieDetailViewModelFactory by instance()
    private lateinit var viewModel: MovieDetailViewModel

    private lateinit var mHeaderTitle: TextView
    private lateinit var mHeaderVoteAverage: TextView
    private lateinit var mOverview: TextView
    private lateinit var mGenresTextView: TextView
    private lateinit var mDirectorsTextView: TextView
    private lateinit var mCastTextView: TextView
    private lateinit var mHeaderImageView: ImageView
    private lateinit var mProgressDirectors:ProgressBar
    private lateinit var mProgressCast:ProgressBar
    private lateinit var mProgressGenres:ProgressBar

    private lateinit var movie: MovieEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        mHeaderImageView = findViewById(R.id.imageview_header)
        mHeaderTitle = findViewById(R.id.textview_title)
        mHeaderVoteAverage = findViewById(R.id.rating)
        mOverview = findViewById(R.id.overview_detail)
        mGenresTextView = findViewById(R.id.genres_detail)
        mDirectorsTextView = findViewById(R.id.directors_detail)
        mCastTextView = findViewById(R.id.cast_detail)
        mProgressDirectors = findViewById(R.id.progress_directors)
        mProgressCast = findViewById(R.id.progress_cast)
        mProgressGenres = findViewById(R.id.progress_genres)

        ViewCompat.setTransitionName(imageview_header, VIEW_NAME_HEADER_IMAGE)
        ViewCompat.setTransitionName(textview_title, VIEW_NAME_HEADER_TITLE)
        ViewCompat.setTransitionName(mHeaderVoteAverage, VIEW_NAME_HEADER_VOTE_AVERAGE)
        ViewCompat.setTransitionName(mOverview, VIEW_NAME_OVERVIEW)

        //Init view model
        viewModel = ViewModelProvider(this, viewModelFactory).get(MovieDetailViewModel::class.java)

        movie = intent.getSerializableExtra(MOVIE_MESSAGE) as MovieEntity

        loadItem()

        bindUI()
    }

    private fun bindUI() = launch {

        viewModel.fetchMovie(movie.id)
        viewModel.fetchCast(movie.id)

        val movie = viewModel.movie.await()

        movie.observe(this@MovieDetailActivity, Observer {
            if (it == null) return@Observer

            mGenresTextView.text = getGenresString(it.genres)
            mProgressGenres.visibility = View.GONE
        })

        val cast = viewModel.cast.await()

        cast.observe(this@MovieDetailActivity, Observer {
            if (it == null) return@Observer

            mCastTextView.text = getCastString(it.cast)
            mProgressCast.visibility = View.GONE

            mDirectorsTextView.text = getDirectorString(it.crew)
            mProgressDirectors.visibility = View.GONE

        })

    }

    private fun getDirectorString(crew: List<Crew>): String {

        var directorString = StringBuilder()
        var directorsCount = 0

        for (director in crew) {
            if (director.job.equals("Director")) {
                if (directorsCount > 1) {
                    directorString.append(", " + director.name)
                } else {
                    directorString.append(director.name)
                }
                directorsCount++
            }
        }

        if (directorsCount > 1) {
            directorString =
                StringBuilder(directorString.substring(0, directorString.length - 1).toString())
        }

        return if (directorString.toString().isEmpty()) {
            getString(R.string.empty)
        } else {
            directorString.toString().trim()
        }
    }

    private fun getGenresString(genres: List<Genre>): String {
        val genresString = StringBuilder()

        for ((index, genre) in genres.withIndex()) {
            if (index != genres.size - 1) {

                genresString.append(" ${genre.name.capitalize()}, ")
            } else {

                genresString.append(" ${genre.name.capitalize()}")
            }
        }

        return if (genresString.toString().isEmpty()) {
            getString(R.string.empty)
        } else {
            genresString.toString().trim()
        }
    }

    private fun getCastString(castList: List<Cast>): String {
        val castString = StringBuilder()
        for ((index, cast) in castList.withIndex()) {
            if (index != castList.size - 1) {
                castString.append(cast.name + ", ")
            } else {
                castString.append(cast.name)
            }
        }

        return if (castString.toString().isEmpty()) {
            getString(R.string.empty)
        } else {
            castString.toString().trim()
        }
    }


    private fun loadItem() {

        if (movie.overview!!.isEmpty()) {

            //Set empty value for overview if it is blank
            overview_detail.text = getString(R.string.empty)
        } else {

            // Set overview TextView to the item's overview
            overview_detail.text = movie.overview
        }

        // Set the title TextView to the item's name
        if (movie.title != getString(R.string.empty) && movie.release_date!! != getString(R.string.empty)) {
            val titleDate = movie.title + " (" + movie.release_date?.replace("-", ".") + ")"
            val spannableStringBuilder = SpannableStringBuilder(titleDate)

            val gray = ForegroundColorSpan(getColor(R.color.gray))

            spannableStringBuilder.setSpan(
                gray,
                movie.title.length,
                titleDate.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            mHeaderTitle.text = spannableStringBuilder
        } else {
            mHeaderTitle.text = movie.title
        }

        // Set vote average TextView to the item's vote average
        mHeaderVoteAverage.text = movie.vote_average.toString()


        // Set image ImageView to the item's image
        loadThumbnail()
    }

    private fun loadThumbnail() {

        val bm: Bitmap
        bm = if (movie.image == null) {
            val d: Drawable? = ContextCompat.getDrawable(this, R.mipmap.default_film)
            val bitmap = (d as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            bitmap
        } else {
            BitmapFactory.decodeByteArray(
                movie.image,
                0,
                movie.image!!.size
            )
        }

        imageview_header.setImageBitmap(bm)
    }

    companion object {
        // View name of the header image. Used for activity scene transitions
        const val VIEW_NAME_HEADER_IMAGE = "detail:header:image"

        // View name of the header title. Used for activity scene transitions
        const val VIEW_NAME_HEADER_TITLE = "detail:header:title"

        // View name of the vote average. Used for activity scene transitions
        const val VIEW_NAME_HEADER_VOTE_AVERAGE = "detail:header:vote_average"

        // View name of the overview. Used for activity scene transitions
        const val VIEW_NAME_OVERVIEW = "detail:overview"

    }

}
