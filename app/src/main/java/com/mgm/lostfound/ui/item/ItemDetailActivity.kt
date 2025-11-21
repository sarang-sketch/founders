package com.mgm.lostfound.ui.item

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mgm.lostfound.data.model.ClaimRequest
import com.mgm.lostfound.data.model.ItemType
import com.mgm.lostfound.data.model.LostFoundItem
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.data.repository.ItemRepository
import com.mgm.lostfound.databinding.ActivityItemDetailBinding
import com.mgm.lostfound.ui.claim.ClaimRequestActivity
import kotlinx.coroutines.launch

class ItemDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var item: LostFoundItem
    private val authRepository = AuthRepository()
    private val itemRepository = ItemRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent.getParcelableExtra("item") ?: return
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = item.title

        binding.tvTitle.text = item.title
        binding.tvDescription.text = item.description
        binding.tvCategory.text = item.category.name.replace("_", " ")
        binding.tvLocation.text = item.location?.address ?: "Location not specified"
        binding.tvSerialNumber.text = item.serialNumber.ifEmpty { "Not provided" }
        binding.tvReward.text = item.reward.ifEmpty { "No reward" }
        binding.tvStatus.text = item.status.name

        if (item.photoUrls.isNotEmpty()) {
            Glide.with(this)
                .load(item.photoUrls[0])
                .into(binding.ivPhoto)
        }

        val userId = authRepository.getCurrentUser()?.uid
        val isOwner = item.userId == userId
        val isFoundItem = item.type == ItemType.FOUND

        binding.btnClaim.visibility = if (isFoundItem && !isOwner) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnContact.visibility = if (item.contactShared) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun setupClickListeners() {
        binding.btnClaim.setOnClickListener {
            val intent = Intent(this, ClaimRequestActivity::class.java)
            intent.putExtra("item", item)
            startActivity(intent)
        }

        binding.btnScanQR.setOnClickListener {
            // Open QR scanner
            Toast.makeText(this, "QR Scanner feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out this ${item.type.name.lowercase()} item: ${item.title}\n${item.description}")
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

