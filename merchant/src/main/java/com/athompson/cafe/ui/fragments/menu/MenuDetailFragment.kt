package com.athompson.cafe.ui.fragments.menu

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.athompson.cafe.R
import com.athompson.cafe.adapters.SimpleMenuItemAdapter
import com.athompson.cafe.databinding.FragmentMenusDetailsBinding
import com.athompson.cafe.firestore.FireStoreMenu
import com.athompson.cafe.firestore.FireStoreMenuItem
import com.athompson.cafe.ui.fragments.BaseFragment
import com.athompson.cafe.utils.GlideLoader
import com.athompson.cafelib.extensions.ContextExtensions.themeColor
import com.athompson.cafelib.extensions.FragmentExtensions.logError
import com.athompson.cafelib.extensions.FragmentExtensions.toolBarSubTitle
import com.athompson.cafelib.extensions.FragmentExtensions.toolBarTitle
import com.athompson.cafelib.extensions.StringExtensions.safe
import com.athompson.cafelib.extensions.ViewExtensions.remove
import com.athompson.cafelib.extensions.ViewExtensions.show
import com.athompson.cafelib.models.CafeQrMenu
import com.athompson.cafelib.models.FoodMenuItem
import com.google.android.material.transition.MaterialContainerTransform


class MenuDetailFragment : BaseFragment(), AdapterView.OnItemSelectedListener {
    private var adapter: SimpleMenuItemAdapter? = null
    private val args: MenuDetailFragmentArgs by navArgs()
    private val selectedMenu: CafeQrMenu? by lazy(LazyThreadSafetyMode.NONE) { args.selectedMenu }
    private val menuListName = ArrayList<String?>()
    private val menus = ArrayList<CafeQrMenu?>()
    private val menuFoodItems = ArrayList<FoodMenuItem?>()
    private lateinit var binding: FragmentMenusDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(com.athompson.cafe.R.integer.reply_motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_menus_details, container, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            findNavController().navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMenusDetailsBinding.bind(view)

        if (selectedMenu?.imageUrl.safe().isNotEmpty())
            GlideLoader(requireContext()).loadImagePicture(selectedMenu?.imageUrl.safe(), binding.image)
        else
            binding.image.setImageResource(R.drawable.cafe_image)

        toolBarTitle(selectedMenu?.name.safe())
        toolBarSubTitle(selectedMenu?.description.safe())

        binding.name.text = selectedMenu?.name.safe()
        binding.description.text = selectedMenu?.description.safe()

        populateMenus()

    }

    private fun populateMenus()
    {
        showProgressDialog("Getting Menus")
        FireStoreMenu().getMenus(::successMenu, ::failureMenu)
    }

    private fun successMenu(m:ArrayList<CafeQrMenu?>)
    {
        hideProgressDialog()
        menuListName.clear()
        m.forEach { menuListName.add(it?.name)}

        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, menuListName)

        binding.menusAutoComplete.setAdapter(adapter)

        binding.menusAutoComplete.onItemSelectedListener = this
    }
    private fun failureMenu(e:Exception)
    {
        hideProgressDialog()
        logError(e.toString())
    }


    private fun showError() {
        // Do nothing
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        binding.recycler.show()
      //  selectedMenu = menus[position]
        populateMenu()
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        binding.recycler.remove()
 //       selectedMenu = null
    }

    private fun populateMenu()
    {
        val selected = selectedMenu
        if(selected!=null) {
            showProgressDialog("Getting Menu Items")
            adapter = SimpleMenuItemAdapter(requireContext(), menuFoodItems)
            binding.recycler.adapter = adapter
            FireStoreMenuItem().getMenuItems(selected.id, ::successMenuItem, ::failureMenuItem)
        }
    }

    private fun successMenuItem(m:ArrayList<FoodMenuItem?>)
    {
        hideProgressDialog()
        menuFoodItems.clear()
        menuFoodItems.addAll(m)
        adapter?.dataChanged(menuFoodItems)
    }

    private fun failureMenuItem(e:Exception)
    {
        hideProgressDialog()
        menuFoodItems.clear()
        adapter?.dataChanged(menuFoodItems)
        logError(e.toString())
    }
}
