package com.example.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.example.R
import com.example.model.Destination
import java.util.Locale

class DestinationAdapter(
    private var items: List<Destination> = emptyList(),
    private val onEditClick: (Destination) -> Unit,
    private val onDeleteClick: (Destination) -> Unit
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    fun updateData(newItems: List<Destination>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size
            override fun getNewListSize(): Int = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].id == newItems[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == newItems[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destination_card, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = items[position]
        holder.bind(destination, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = items.size

    class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivDestinationPhoto)
        val tvCountry: TextView = itemView.findViewById(R.id.tvDestinationCountry)
        val tvPrice: TextView = itemView.findViewById(R.id.tvDestinationPrice)
        val tvName: TextView = itemView.findViewById(R.id.tvDestinationName)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDestinationDescription)
        val btnEdit: Button = itemView.findViewById(R.id.btnEditDestination)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeleteDestination)

        fun bind(
            destination: Destination,
            onEdit: (Destination) -> Unit,
            onDelete: (Destination) -> Unit
        ) {
            tvName.text = destination.name
            tvCountry.text = destination.country
            tvPrice.text = String.format(Locale.US, "$%.2f USD", destination.price)
            tvDescription.text = destination.description

            val context = itemView.context
            val imageSource = if (destination.imageUri.isNotBlank()) {
                destination.imageUri
            } else {
                R.drawable.travel_cancun
            }

            val request = ImageRequest.Builder(context)
                .data(imageSource)
                .crossfade(true)
                .placeholder(R.drawable.travel_cancun)
                .error(R.drawable.travel_cancun)
                .target(ivPhoto)
                .build()

            Coil.imageLoader(context).enqueue(request)

            btnEdit.setOnClickListener { onEdit(destination) }
            btnDelete.setOnClickListener { onDelete(destination) }
        }
    }
}
