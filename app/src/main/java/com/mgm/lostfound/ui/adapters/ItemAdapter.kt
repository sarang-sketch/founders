package com.mgm.lostfound.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mgm.lostfound.R
import com.mgm.lostfound.data.model.ItemCategory
import com.mgm.lostfound.data.model.ItemType
import com.mgm.lostfound.data.model.LostFoundItem
import java.text.SimpleDateFormat
import java.util.Locale

class ItemAdapter(
    private val onItemClick: (LostFoundItem) -> Unit
) : ListAdapter<LostFoundItem, ItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lost_found, parent, false)
        return ItemViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ItemViewHolder(
        itemView: View,
        private val onItemClick: (LostFoundItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.iv_photo)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)

        fun bind(item: LostFoundItem) {
            tvTitle.text = item.title.ifEmpty { item.description.take(50) }
            tvCategory.text = item.category.name.replace("_", " ")
            tvLocation.text = item.location?.address ?: "Location not specified"
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvDate.text = dateFormat.format(item.createdAt)

            tvType.text = item.type.name
            tvType.setBackgroundColor(
                itemView.context.getColor(
                    if (item.type == ItemType.LOST) R.color.lost_item else R.color.found_item
                )
            )

            if (item.photoUrls.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.photoUrls[0])
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivPhoto)
            } else {
                ivPhoto.setImageResource(R.drawable.ic_placeholder)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<LostFoundItem>() {
        override fun areItemsTheSame(oldItem: LostFoundItem, newItem: LostFoundItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LostFoundItem, newItem: LostFoundItem): Boolean {
            return oldItem == newItem
        }
    }
}

