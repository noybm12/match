package com.example.match

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ParticipantAdapter(private val participantsList: List<Participant>) :
    RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.participant_TXT_name)
        val imgProfile: ImageView = itemView.findViewById(R.id.participant_IMG_profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_participant, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val currentParticipant = participantsList[position]

        holder.txtName.text = currentParticipant.name

        if (currentParticipant.profileImageUrl.isNotEmpty()) {
            holder.imgProfile.load(currentParticipant.profileImageUrl)
        }
    }

    override fun getItemCount(): Int {
        return participantsList.size
    }
}