package com.example.match

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("response")
    val matches: List<MatchData>,
    val results: Int,
    val errors: Any?,
)

data class MatchData(
    val fixture: Fixture,
    val league: League,
    val teams: Teams
)

data class Fixture(
    val id : Int,
    val date: String
)

data class League(
    val name: String
)

data class Teams(
    val home: Team,
    val away: Team
)

data class Team(
    val name: String,
    val logo: String
)