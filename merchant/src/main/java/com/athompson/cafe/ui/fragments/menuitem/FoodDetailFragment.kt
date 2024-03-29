package com.athompson.cafe.ui.fragments.menuitem

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.athompson.cafe.R
import com.athompson.cafe.databinding.FragmentFoodDetailsBinding
import com.athompson.cafe.extensions.ViewExtensions.choose
import com.athompson.cafe.extensions.ViewExtensions.edit
import com.athompson.cafe.extensions.ViewExtensions.editCurrency
import com.athompson.cafe.extensions.ViewExtensions.editLong
import com.athompson.cafe.extensions.ViewExtensions.setImage
import com.athompson.cafe.firestore.FireStoreFoodMenuItem
import com.athompson.cafe.firestore.FireStoreImage
import com.athompson.cafe.ui.fragments.BaseFragment
import com.athompson.cafe.utils.GlideLoader
import com.athompson.cafelib.extensions.ActivityExtensions.showErrorSnackBar
import com.athompson.cafelib.extensions.ContextExtensions.themeColor
import com.athompson.cafelib.extensions.DoubleExtensions.DoubleFromCurrency
import com.athompson.cafelib.extensions.DoubleExtensions.priceValue
import com.athompson.cafelib.extensions.DoubleExtensions.toPrice
import com.athompson.cafelib.extensions.FragmentExtensions.logError
import com.athompson.cafelib.extensions.FragmentExtensions.toolBarSubTitle
import com.athompson.cafelib.extensions.FragmentExtensions.toolBarTitle
import com.athompson.cafelib.extensions.ResourceExtensions.asString
import com.athompson.cafelib.extensions.StringExtensions.safe
import com.athompson.cafelib.extensions.StringExtensions.trimmed
import com.athompson.cafelib.extensions.ToastExtensions.showShortToast
import com.athompson.cafelib.extensions.ViewExtensions.trimmed
import com.athompson.cafelib.extensions.getStoragePermissions
import com.athompson.cafelib.extensions.showImageChooser
import com.athompson.cafelib.models.FoodMenuItem
import com.athompson.cafelib.shared.SharedConstants
import com.google.android.material.transition.MaterialContainerTransform
import java.io.IOException

class FoodDetailFragment : BaseFragment() {
    private var saveMenuIcon: MenuItem? = null
    private var selectedFoodType: String = ""
    private val args: FoodDetailFragmentArgs by navArgs()
    private val selectedFood: FoodMenuItem? by lazy(LazyThreadSafetyMode.NONE) { args.selectedFood }
    private lateinit var binding: FragmentFoodDetailsBinding

    private var imageUrl: String?= null
    private var imageFileLocation: Uri? = null
    private var dataChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_food_details, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_details_fragment_menus, menu)
        saveMenuIcon = menu.findItem(R.id.save_food_details)
        saveMenuIcon?.isEnabled = false
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            findNavController().navigateUp()
        }
        if (id == R.id.save_food_details) {
            save()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFoodDetailsBinding.bind(view)

        val foodItem = selectedFood
        if (foodItem == null) {
            showError()
            return
        }

        selectedFoodType = selectedFood?.type.toString()
        if(selectedFoodType.isEmpty()) {
            selectedFoodType = SharedConstants.FOOD_TYPES[0]
            selectedFood?.type = selectedFoodType
        }

        setToolBar()

        binding.name.text = foodItem.name
        binding.description.text = foodItem.description
        binding.foodTypes.text = selectedFoodType
        binding.price.text = foodItem.price.toPrice()
        binding.image.setImage(foodItem.imageUrl,R.drawable.cafe_image)
        edit()
    }

    private fun setToolBar()
    {
        toolBarTitle(selectedFood?.name.safe())
        toolBarSubTitle(selectedFood?.description.safe())
    }


    private fun showError() {
        // Do nothing
    }

    private fun edit()
    {
        binding.name.setOnClickListener {
            binding.name.edit("Edit Name", "Type to edit name",::onChanged)
        }

        binding.price.setOnClickListener {
            binding.price.editCurrency("Edit Price", "Type to edit price",::onChanged)
        }

        binding.description.setOnClickListener {
            binding.description.editLong("Edit description", "Type to edit description",::onChanged)
        }

        binding.foodTypes.setOnClickListener {
            binding.foodTypes.choose("Edit Food Type", "Type to edit food type",SharedConstants.FOOD_TYPES,::onChanged)
        }

        binding.image.setOnClickListener{
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            } else {
                getStoragePermissions()
            }
        }

    }
    @Suppress("UNUSED_PARAMETER")
    private fun onChanged(view:View,oldValue:String?,newValue:String?)
    {
        changed()
    }
    private fun changed()
    {
        dataChanged = hasChanged()
        saveMenuIcon?.isEnabled = dataChanged
    }

    private fun hasChanged():Boolean
    {
        if(selectedFood?.name.trimmed()!=binding.name.text.toString())
            return true
        if(selectedFood?.description.trimmed()!=binding.description.trimmed())
            return true
        if(selectedFood?.price!=binding.price.text.toString().DoubleFromCurrency())
            return true
        if(selectedFood?.type!=binding.foodTypes.text.toString())
            return true
        if(imageFileLocation!=null)
            return true
        return false
    }

    private fun save()
    {
        if (validate()) {
            showProgressDialog(R.string.please_wait.asString())
            val uri = imageFileLocation
            if (uri != null) {
                FireStoreImage().uploadImageToCloudStorage(requireActivity(),uri,selectedFood?.imageUrl,
                    SharedConstants.VENUE_IMAGE_SUFFIX,::imageUploadSuccess,::imageUploadFailure)
            } else {
                update()
            }
        }
    }

    private fun imageUploadSuccess(url: String) {

        hideProgressDialog()
        imageUrl = url
        update()
    }

    private fun imageUploadFailure(e: Exception) {
        logError(e.message.toString())
        hideProgressDialog()
    }


    private fun validate(): Boolean {
        return when {
            binding.name.text.isEmpty() -> {
                requireActivity().showErrorSnackBar("The menu name cannot be empty", true)
                false
            }
            binding.description.text.isEmpty() -> {
                requireActivity().showErrorSnackBar("The menu description cannot be empty", true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun update() {

        val name = binding.name.text
        if (name != selectedFood?.name) {
            selectedFood?.name = name as String
        }

        val description = binding.description.text
        if (description != selectedFood?.description) {
            selectedFood?.description = description as String
        }

        val foodType = binding.description.text
        if (foodType != selectedFood?.type) {
            selectedFood?.type = foodType as String
        }

        val price = binding.price.text.toString().priceValue()
        if (price != selectedFood?.price) {
            selectedFood?.price = price
        }

        if (selectedFood?.imageUrl != imageUrl && !imageUrl.isNullOrEmpty()) {
            selectedFood?.imageUrl = imageUrl.toString()
        }

        selectedFood?.let {
            FireStoreFoodMenuItem().update(::updateSuccess,::updateFailure, selectedFood?.id.toString(), it)
        }
    }

    private fun updateSuccess() {
        imageFileLocation = null
        setToolBar()
        changed()
        hideProgressDialog()
        showShortToast(R.string.msg_menu_update_success)
    }

    private fun updateFailure(e:Exception) {
        logError(e.message.toString())
        hideProgressDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SharedConstants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    try {
                        imageFileLocation = data.data
                        val url = imageFileLocation
                        if (url != null) {
                            GlideLoader(requireContext()).loadImagePicture(url, binding.image)
                            changed()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        showShortToast(R.string.image_selection_failed.asString())
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }
}

