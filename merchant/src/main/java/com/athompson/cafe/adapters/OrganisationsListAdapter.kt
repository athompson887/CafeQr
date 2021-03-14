package com.athompson.cafe.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.athompson.cafe.R
import com.athompson.cafe.databinding.FragmentOrganisationsListItemBinding
import com.athompson.cafelib.models.Organisation
import com.athompson.cafe.ui.fragments.organisations.OrganisationsFragment
import com.athompson.cafe.utils.GlideLoader

/**
 * A adapter class for products list items.
 */
open class OrganisationsListAdapter(
    private val context: Context,
    private var list: ArrayList<Organisation>,
    private val fragment: OrganisationsFragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrgViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.fragment_organisations_list_item,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is OrgViewHolder) {

            GlideLoader(context).loadImagePicture(model.imageUrl, holder.binding.image)

            holder.binding.tvName.text = model.name
            holder.binding.tvAddress1.text = model.address1
            holder.binding.tvAddress2.text = model.address2
            holder.binding.tvCity.text = model.city
            holder.binding.tvEmail.text = model.email
            holder.binding.tvType.text = model.type
            holder.binding.tvTelephone.text = model.telephone.toString()

            //     holder.itemView.ib_delete_product.setOnClickListener {

            //       fragment.deleteOrganisation(model.product_id)
            //  }
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }


    inner class OrgViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {
        val binding: FragmentOrganisationsListItemBinding = FragmentOrganisationsListItemBinding.bind(mView)
        override fun toString(): String {
            return super.toString() + " '"
        }
    }
}