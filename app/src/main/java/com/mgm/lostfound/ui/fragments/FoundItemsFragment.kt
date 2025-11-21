package com.mgm.lostfound.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgm.lostfound.data.model.ItemType
import com.mgm.lostfound.data.model.LostFoundItem
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.data.repository.ItemRepository
import com.mgm.lostfound.data.repository.HybridRepository
import com.mgm.lostfound.utils.NotificationHelper
import kotlinx.coroutines.flow.collectLatest
import com.mgm.lostfound.databinding.FragmentFoundItemsBinding
import com.mgm.lostfound.ui.item.ReportItemActivity
import com.mgm.lostfound.ui.item.ItemDetailActivity
import com.mgm.lostfound.ui.adapters.ItemAdapter
import kotlinx.coroutines.launch

class FoundItemsFragment : Fragment() {
    private var _binding: FragmentFoundItemsBinding? = null
    private val binding get() = _binding!!
    private val itemRepository = ItemRepository()
    private val hybridRepository = HybridRepository()
    private val authRepository = AuthRepository()
    private lateinit var adapter: ItemAdapter
    private val existingItemIds = mutableSetOf<String>()

    companion object {
        fun newInstance() = FoundItemsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoundItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadFoundItems()

        binding.fabReportFound.setOnClickListener {
            val intent = Intent(requireContext(), ReportItemActivity::class.java)
            intent.putExtra("type", "FOUND")
            startActivity(intent)
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadFoundItems()
        }
    }

    private fun setupRecyclerView() {
        adapter = ItemAdapter { item ->
            val intent = Intent(requireContext(), ItemDetailActivity::class.java)
            intent.putExtra("item", item)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun loadFoundItems() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            // Use HybridRepository for Supabase real-time data
            val result = hybridRepository.getItems(ItemType.FOUND)
            result.onSuccess { items ->
                // Store existing item IDs to detect new items
                existingItemIds.clear()
                existingItemIds.addAll(items.map { it.id })
                
                adapter.submitList(items)
                binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = false
            }.onFailure { exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
                binding.swipeRefresh.isRefreshing = false
            }
        }
        
        // Subscribe to real-time updates
        lifecycleScope.launch {
            hybridRepository.subscribeToItems(ItemType.FOUND).collectLatest { item ->
                val currentUserId = authRepository.getCurrentUser()?.uid
                val isNewItem = !existingItemIds.contains(item.id)
                val isNotMyItem = item.userId != currentUserId
                
                // Update adapter when new item arrives
                val currentList = adapter.currentList.toMutableList()
                val index = currentList.indexOfFirst { it.id == item.id }
                if (index >= 0) {
                    currentList[index] = item
                } else {
                    currentList.add(0, item)
                    existingItemIds.add(item.id)
                    
                    // Show notification for new found items (not created by current user)
                    if (isNewItem && isNotMyItem && currentUserId != null) {
                        NotificationHelper.showNotification(
                            requireContext(),
                            "Found Item: ${item.title}",
                            "Found ${item.title} - is it yours?",
                            "found"
                        )
                    }
                }
                adapter.submitList(currentList)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadFoundItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

