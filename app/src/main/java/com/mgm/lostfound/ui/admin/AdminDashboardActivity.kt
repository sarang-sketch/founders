package com.mgm.lostfound.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgm.lostfound.data.model.LostFoundItem
import com.mgm.lostfound.data.model.User
import com.mgm.lostfound.data.repository.ItemRepository
import com.mgm.lostfound.databinding.ActivityAdminDashboardBinding
import com.mgm.lostfound.ui.adapters.ItemAdapter
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var user: User
    private val itemRepository = ItemRepository()
    private lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getParcelableExtra("user") ?: return
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        loadAllReports()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter { item ->
            // Show item details dialog with admin actions
            showAdminActions(item)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadAllReports() {
        lifecycleScope.launch {
            val lostResult = itemRepository.getLostItems()
            val foundResult = itemRepository.getFoundItems()

            val allItems = mutableListOf<LostFoundItem>()
            lostResult.onSuccess { allItems.addAll(it) }
            foundResult.onSuccess { allItems.addAll(it) }

            adapter.submitList(allItems.sortedByDescending { it.createdAt })
        }
    }

    private fun setupClickListeners() {
        binding.btnBroadcast.setOnClickListener {
            // Broadcast alert to all students
            Toast.makeText(this, "Broadcast feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAdminActions(item: LostFoundItem) {
        // Show dialog with approve, close, flag options
        android.app.AlertDialog.Builder(this)
            .setTitle("Admin Actions")
            .setMessage("Item: ${item.title}")
            .setPositiveButton("Close Case") { _, _ ->
                closeCase(item.id)
            }
            .setNeutralButton("Flag") { _, _ ->
                flagCase(item.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun closeCase(itemId: String) {
        lifecycleScope.launch {
            val result = itemRepository.updateItemStatus(itemId, "CLOSED")
            result.onSuccess {
                Toast.makeText(this@AdminDashboardActivity, "Case closed", Toast.LENGTH_SHORT).show()
                loadAllReports()
            }.onFailure { exception ->
                Toast.makeText(this@AdminDashboardActivity, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun flagCase(itemId: String) {
        lifecycleScope.launch {
            val result = itemRepository.updateItemStatus(itemId, "FLAGGED")
            result.onSuccess {
                Toast.makeText(this@AdminDashboardActivity, "Case flagged", Toast.LENGTH_SHORT).show()
                loadAllReports()
            }.onFailure { exception ->
                Toast.makeText(this@AdminDashboardActivity, exception.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

