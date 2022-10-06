package com.besirkaraoglu.locationsharingsampleii.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.besirkaraoglu.locationsharingsampleii.R
import com.besirkaraoglu.locationsharingsampleii.model.Users
import com.besirkaraoglu.locationsharingsampleii.util.loadImage
import com.bumptech.glide.Glide

class UsersAdapter(
    private val listener: OnItemClickListener
): RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    private var usersList = listOf<Users>()

    @SuppressLint("NotifyDataSetChanged")
    fun setUserList(usersList: List<Users>){
        this.usersList = usersList
        notifyDataSetChanged()
    }

    class UsersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val TAG = "UsersViewHolder"

        val imageView = itemView.findViewById<ImageView>(R.id.iv)
        val tv = itemView.findViewById<TextView>(R.id.tv)

        fun bind(users: Users, listener: OnItemClickListener){
            loadImage(imageView,users.photoUrl)
            tv.text = users.name

            itemView.setOnClickListener {
                listener.onItemClicked(users)
            }
        }

    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val currentItem = usersList[position]
        holder.bind(currentItem,listener)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_rv_item,parent,false)
        return UsersViewHolder(view)
    }

    interface OnItemClickListener{
        fun onItemClicked(users: Users)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

}