package com.example.movielab.data.response

data class CastResponse(
    val cast: List<Cast>,
    val crew: List<Crew>,
    val id: Int
)