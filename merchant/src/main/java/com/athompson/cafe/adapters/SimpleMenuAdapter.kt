package com.athompson.cafe.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.athompson.cafe.R
import com.athompson.cafe.databinding.SimpleMenuItemBinding
import com.athompson.cafe.utils.GlideLoader
import com.athompson.cafelib.extensions.StringExtensions.safe
import com.athompson.cafelib.models.CafeQrMenu

open class SimpleMenuAdapter(
    private val context: Context,
    private var list: ArrayList<CafeQrMenu>,
) : RecyclerView.Adapter<SimpleMenuAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.simple_menu_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = list[position]
        if(menuItem.imageUrl.safe().isNotEmpty())
            GlideLoader(context).loadImagePicture(menuItem.imageUrl, holder.binding.image)
        else
            holder.binding.image.setImageResource(R.drawable.cafe_image)
        holder.binding.name.text = menuItem.name
        holder.binding.description.text = menuItem.description
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun dataChanged(cafeQrMenus: ArrayList<CafeQrMenu>) {
        list = cafeQrMenus
        notifyDataSetChanged()
    }


    class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val binding: SimpleMenuItemBinding =
            SimpleMenuItemBinding.bind(mView)

        override fun toString(): String {
            return super.toString() + " '"
        }
    }
}
