package com.example.match

data class WatchParty(
    var documentId: String = "",
    val matchId: Int = -1,
    val title: String = "",
    val address: String = "",
    val capacity: String = "",
    val joined: Int = 0,
    val hour: String = "",
    val venueType: String = "",
    val hostName: String = "",
    val hostId: String = "",
    val participants: List<String> = emptyList(),
    val pendingRequests: List<String> = emptyList()
)

