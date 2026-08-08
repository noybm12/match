package com.example.match

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private val eventList : List<WatchParty>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvEventTitle: TextView= itemView.findViewById(R.id.tvEventTitle)
        val tvHostName: TextView = itemView.findViewById(R.id.tvHostName)
        val tvTag: TextView = itemView.findViewById(R.id.tvTag)
        val tvCapacityCount: TextView = itemView.findViewById(R.id.tvCapacityCount)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): EventViewHolder {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.item_event_card ,p0,false)
        return EventViewHolder(view)

    }

    override fun onBindViewHolder(p0: EventViewHolder, p1: Int) {
        val event = eventList[p1]
        p0.tvEventTitle.text = event.title
        p0.tvHostName.text = event.hostName
        p0.tvTag.text = event.venueType
        p0.tvCapacityCount.text = "${event.joined}/${event.capacity}"
        p0.itemView.setOnClickListener {
            val context = p0.itemView.context
            val intent = Intent(context, PartyInfoActivity::class.java)
            intent.putExtra("MATCH_ID",event.matchId)
            intent.putExtra("PARTY_TITLE", event.title)
            intent.putExtra("PARTY_HOST",event.hostName)
            intent.putExtra("PARTY_ADDRESS",event.address)
            intent.putExtra("PARTY_HOUR", event.hour)
            intent.putExtra("PARTY_DOC_ID", event.documentId)
            intent.putExtra("PARTY_HOST_ID", event.hostId)
            context.startActivity(intent)
        }
        }

    override fun getItemCount(): Int {
        return eventList.size
    }
}