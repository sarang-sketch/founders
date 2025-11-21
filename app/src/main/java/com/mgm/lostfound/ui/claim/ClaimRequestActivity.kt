package com.mgm.lostfound.ui.claim

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mgm.lostfound.data.model.ClaimRequest
import com.mgm.lostfound.data.model.ClaimStatus
import com.mgm.lostfound.data.model.LostFoundItem
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.data.repository.ClaimRepository
import com.mgm.lostfound.databinding.ActivityClaimRequestBinding
import java.util.UUID

class ClaimRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClaimRequestBinding
    private lateinit var item: LostFoundItem
    private val authRepository = AuthRepository()
    private val claimRepository = ClaimRepository(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClaimRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item = intent.getParcelableExtra("item") ?: return
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvItemTitle.text = item.title
        binding.btnSubmitClaim.setOnClickListener {
            submitClaimRequest()
        }
    }

    private fun submitClaimRequest() {
        val ownerId = authRepository.getCurrentUser()?.uid ?: return
        val finderId = item.userId

        val claimRequest = ClaimRequest(
            id = UUID.randomUUID().toString(),
            itemId = item.id,
            ownerId = ownerId,
            finderId = finderId,
            status = ClaimStatus.PENDING
        )

        binding.btnSubmitClaim.isEnabled = false
        lifecycleScope.launch {
            val result = claimRepository.createClaimRequest(claimRequest)
            result.onSuccess {
                Toast.makeText(this@ClaimRequestActivity, "Claim request sent", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { exception ->
                Toast.makeText(this@ClaimRequestActivity, exception.message, Toast.LENGTH_SHORT).show()
                binding.btnSubmitClaim.isEnabled = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

