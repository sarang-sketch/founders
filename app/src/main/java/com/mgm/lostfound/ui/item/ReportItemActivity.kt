package com.mgm.lostfound.ui.item

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mgm.lostfound.data.model.ItemCategory
import com.mgm.lostfound.data.model.ItemType
import com.mgm.lostfound.data.model.LostFoundItem
import com.mgm.lostfound.data.model.Location as ItemLocation
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.data.repository.ItemRepository
import com.mgm.lostfound.data.repository.MatchingRepository
import com.mgm.lostfound.data.repository.HybridRepository
import com.mgm.lostfound.utils.LocationHelper
import com.mgm.lostfound.utils.QRCodeGenerator
import com.mgm.lostfound.databinding.ActivityReportItemBinding
import kotlinx.coroutines.launch
import java.util.UUID

class ReportItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportItemBinding
    private val authRepository = AuthRepository()
    private val itemRepository = ItemRepository()
    private val hybridRepository = HybridRepository()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedImageUri: Uri? = null
    private var currentLocation: ItemLocation? = null
    private var itemType: ItemType = ItemType.LOST

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true) {
            openCamera()
        } else if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getCurrentLocation()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            binding.ivPhoto.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemType = ItemType.valueOf(intent.getStringExtra("type") ?: "LOST")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        supportActionBar?.title = if (itemType == ItemType.LOST) "Report Lost Item" else "Report Found Item"
        
        val categories = ItemCategory.values().map { it.name.replace("_", " ") }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnTakePhoto.setOnClickListener {
            checkCameraPermission()
        }

        binding.btnGetLocation.setOnClickListener {
            checkLocationPermission()
        }

        binding.btnSubmit.setOnClickListener {
            submitItem()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    private fun openCamera() {
        val photoFile = java.io.File(getExternalFilesDir(null), "photo_${UUID.randomUUID()}.jpg")
        selectedImageUri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(selectedImageUri)
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    private fun getCurrentLocation() {
        lifecycleScope.launch {
            val location = LocationHelper.getCurrentLocation(this@ReportItemActivity)
            location?.let {
                currentLocation = it
                binding.tvLocation.text = it.address
                binding.tvLocation.visibility = android.view.View.VISIBLE
                Toast.makeText(this@ReportItemActivity, "Location captured", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this@ReportItemActivity, "Could not get location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitItem() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val category = ItemCategory.values()[binding.spinnerCategory.selectedItemPosition]
        val serialNumber = binding.etSerialNumber.text.toString().trim()
        val reward = binding.etReward.text.toString().trim()
        val locationText = binding.etLocation.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = authRepository.getCurrentUser()?.uid ?: return
        val itemId = UUID.randomUUID().toString()
        
        // Generate QR Code
        val qrCode = itemId // QR code will be generated when needed

        val item = LostFoundItem(
            id = itemId,
            userId = userId,
            type = itemType,
            category = category,
            title = title,
            description = description,
            location = currentLocation ?: ItemLocation(address = locationText),
            serialNumber = serialNumber,
            reward = reward,
            qrCode = qrCode
        )

        binding.btnSubmit.isEnabled = false
        lifecycleScope.launch {
            // Upload image if selected
            val photoUrls = selectedImageUri?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val imageBytes = inputStream?.readBytes()
                    imageBytes?.let {
                        val result = itemRepository.uploadImage(it, itemId)
                        if (result.isSuccess) {
                            listOf(result.getOrNull() ?: "")
                        } else {
                            emptyList()
                        }
                    } ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()

            val finalItem = item.copy(photoUrls = photoUrls)
            // Use HybridRepository to save to Supabase
            val result = hybridRepository.reportItem(finalItem)

            result.onSuccess {
                // Check for matches using MatchingRepository
                val matchingRepo = MatchingRepository(this@ReportItemActivity)
                matchingRepo.findAndNotifyMatches(finalItem)
                
                Toast.makeText(this@ReportItemActivity, "Item reported successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this@ReportItemActivity, exception.message, Toast.LENGTH_SHORT).show()
                binding.btnSubmit.isEnabled = true
            }
        }
    }
}

