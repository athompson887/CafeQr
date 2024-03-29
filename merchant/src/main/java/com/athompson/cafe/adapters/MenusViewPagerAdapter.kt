package com.athompson.cafe.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.athompson.cafe.R
import com.athompson.cafe.databinding.MenuDisplayCardBinding
import com.athompson.cafe.extensions.ViewExtensions.setImage
import com.athompson.cafelib.models.CafeQrMenu


open class MenusViewPagerAdapter(
    private val context: Context,
    private var menusList: ArrayList<CafeQrMenu?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MenuViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.menu_display_card,
                parent,
                false
            )
        )
    }


    @SuppressLint("NotifyDataSetChanged")
    fun dataChanged() {
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val menu = menusList[position]

        if (holder is MenuViewHolder && menu!=null) {
            holder.binding.image.setImage(menu.imageUrl,R.drawable.cafe_image)
            holder.binding.selectedMenuName.text = menu.name
            holder.binding.selectedMenuDescription.text = menu.description
        }
    }

    override fun getItemCount(): Int {
        return menusList.size
    }

    fun itemAt(currentItemIndex: Int): CafeQrMenu? {
        if(menusList.isNullOrEmpty())
            return null
        if(currentItemIndex >  menusList.size-1 )
            return null
        return menusList[currentItemIndex]
    }


    inner class MenuViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val binding: MenuDisplayCardBinding =
            MenuDisplayCardBinding.bind(mView)

        override fun toString(): String {
            return super.toString() + " '"
        }
    }
}
