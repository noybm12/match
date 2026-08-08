package com.example.match

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class RequestAdapter (
    private val requestList: MutableList<RequestItem>,
    private val onApproveClick: (RequestItem,Int)-> Unit
): RecyclerView.Adapter<RequestAdapter.RequestViewHolder>()
{

    class RequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgProfile: ImageView = itemView.findViewById(R.id.request_IMG_profile)
        val txtName : TextView = itemView.findViewById(R.id.request_TXT_name)
        val txtPartyName: TextView = itemView.findViewById(R.id.request_TXT_party_name)
        val btnApprove : Button = itemView.findViewById(R.id.request_BTN_approve)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }
    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val currentRequest = requestList[position]

        holder.txtName.text = currentRequest.userName
        holder.txtPartyName.text = "Wants to join: ${currentRequest.partyTitle}"

        if (currentRequest.userPhotoUrl.isNotEmpty()) {
            holder.imgProfile.load(currentRequest.userPhotoUrl)
        }


        holder.btnApprove.setOnClickListener {
            onApproveClick(currentRequest, position)
        }
    }

    override fun getItemCount(): Int = requestList.size
    fun removeItem(position: Int) {
        requestList.removeAt(position)
        notifyItemRemoved(position)
    }


}