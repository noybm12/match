package com.example.match

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class MatchAdapter(private var matchesList: List<MatchData>) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    fun updateData(newMatches: List<MatchData>) {
        matchesList = newMatches
        notifyDataSetChanged()
    }

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtLeague: TextView = itemView.findViewById(R.id.match_TXT_league)
        val txtDate: TextView = itemView.findViewById(R.id.match_TXT_date)
        val txtTeam1: TextView = itemView.findViewById(R.id.match_TXT_team1_name)
        val txtTeam2: TextView = itemView.findViewById(R.id.match_TXT_team2_name)
        val txtTime: TextView = itemView.findViewById(R.id.match_TXT_time)
        val imgTeam1 : ImageView = itemView.findViewById(R.id.match_IMG_team1)
        val imgTeam2 : ImageView = itemView.findViewById(R.id.match_IMG_team2)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match_card, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val currentMatch = matchesList[position]

        holder.txtLeague.text = currentMatch.league.name
        holder.txtTeam1.text = currentMatch.teams.home.name
        holder.txtTeam2.text = currentMatch.teams.away.name
        holder.imgTeam1.load(currentMatch.teams.home.logo)
        holder.imgTeam2.load(currentMatch.teams.away.logo)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DiscoverActivity::class.java)
            android.util.Log.d("MatchTrace", "Adapter is sending ID: ${currentMatch.fixture.id}")
            intent.putExtra("MATCH_ID", currentMatch.fixture.id)
            context.startActivity(intent)
        }


        val fullDate = currentMatch.fixture.date
        if (fullDate.length >= 16) {
            holder.txtDate.text = fullDate.substring(0, 10)
            holder.txtTime.text = fullDate.substring(11, 16)
        }
    }

    override fun getItemCount(): Int {
        return matchesList.size
    }
}