package com.athompson.cafe.ui.fragments.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import androidx.viewpager2.widget.ViewPager2
import com.athompson.cafe.R
import com.athompson.cafe.adapters.SimpleMenuAdapter
import com.athompson.cafe.adapters.VenuesViewPagerAdapter
import com.athompson.cafe.databinding.FragmentDashboardBinding
import com.athompson.cafe.firestore.FireStoreMenu
import com.athompson.cafe.firestore.FireStoreVenue
import com.athompson.cafe.ui.activities.AddMenuActivity
import com.athompson.cafe.ui.activities.SettingsActivity
import com.athompson.cafe.ui.fragments.BaseFragment
import com.athompson.cafelib.extensions.ResourceExtensions.asString
import com.athompson.cafelib.extensions.ToastExtensions.showShortToast
import com.athompson.cafelib.extensions.ViewExtensions.hide
import com.athompson.cafelib.extensions.ViewExtensions.remove
import com.athompson.cafelib.extensions.ViewExtensions.setLayoutManagerVertical
import com.athompson.cafelib.extensions.ViewExtensions.show
import com.athompson.cafelib.models.CafeQrMenu
import com.athompson.cafelib.models.Venue


class DashboardFragment : BaseFragment() {


    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding
    private var cafeQrMenus: ArrayList<CafeQrMenu> = ArrayList()
    private var venues: ArrayList<Venue> = ArrayList()
    private var selectedMenu:CafeQrMenu? = null
    private lateinit var simpleMenuAdapter:SimpleMenuAdapter
    private lateinit var venuesViewPagerAdapter: VenuesViewPagerAdapter
    private var called = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        dashboardViewModel.text.observe(viewLifecycleOwner, {
            //textView.text = it
        })
        return root
    }


    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDashboardBinding.bind(view)
        binding.addMenu.setOnClickListener{
            startActivity(Intent(activity, AddMenuActivity::class.java))
        }

        setupRecycler()
        if(!called) {
            called = true
            getMenusList()
        }
    }


    private fun setupRecycler()
    {
        venuesViewPagerAdapter = VenuesViewPagerAdapter(requireContext(),venues,cafeQrMenus)
        simpleMenuAdapter = SimpleMenuAdapter(requireContext(),cafeQrMenus)

        binding.viewPager.adapter = venuesViewPagerAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                println(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                println(position)
            }


            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectedMenu = getSelectedMenu(position)
                renderSelectedMenu()
            }
        })


        binding.recyclerMenus.setLayoutManagerVertical()
        binding.recyclerMenus.itemAnimator = DefaultItemAnimator()
        binding.recyclerMenus.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.HORIZONTAL
            )
        )
        binding.recyclerMenus.adapter = simpleMenuAdapter

    }

    private fun renderSelectedMenu() {
        binding.menuTitle.show()
        binding.menuTitle.text = selectedMenu?.name
    }


    private fun getVenuesList() {
        showProgressDialog(R.string.please_wait.asString())
        FireStoreVenue().getVenues(::successVenuesList, ::failureVenueList)
    }

    private fun successVenuesList(venuesList: java.util.ArrayList<Venue>) {
        if (venuesList.isNullOrEmpty()) {
            noVenue()
        }
        else
        {
            venues.clear()
            venues.addAll(venuesList)
            venuesViewPagerAdapter.dataChanged()
        }
        hideProgressDialog()
    }

    private fun noVenue()
    {
        venues.clear()
        venues.add(Venue("fake_venue","","","",""))
        venuesViewPagerAdapter.dataChanged()
        hideProgressDialog()
    }

    private fun failureVenueList(e: Exception) {
        showShortToast("Failed To get venus",e)
        noVenue()
    }

    private fun getMenusList() {
        showProgressDialog(R.string.please_wait.asString())
        FireStoreMenu().getMenus(::successfulCafeQrMenuList,::failureCafeQrMenuList)
    }

    private fun successfulCafeQrMenuList(menuList: ArrayList<CafeQrMenu>) {
        if (menuList.size > 0) {
            cafeQrMenus.clear()
            cafeQrMenus.addAll(menuList)
            setMenuTitle()
            showMenu()
        } else {
            showNoMenu()
        }
        simpleMenuAdapter.dataChanged(cafeQrMenus)
        hideProgressDialog()
        getVenuesList()
    }

    fun getSelectedMenu(position: Int):CafeQrMenu?
    {
         val currentVenue = venuesViewPagerAdapter.itemAt(position)
         cafeQrMenus.forEach {
            if(it.uid.isNotBlank()&&it.uid==currentVenue?.muid)
            {
                return  it
            }
         }
        return null
    }

    private fun setMenuTitle() {
        binding.menuTitle.text = cafeQrMenus[0].name
    }

    private fun failureCafeQrMenuList(e: Exception) {
        hideProgressDialog()
        showNoMenu()
    }
    private fun showMenu()
    {
        binding.menusView.show()
        binding.noMenusView.remove()
    }
    private fun showNoMenu()
    {
        binding.menuTitle.hide()
        binding.menusView.remove()
        binding.noMenusView.show()
    }
}
