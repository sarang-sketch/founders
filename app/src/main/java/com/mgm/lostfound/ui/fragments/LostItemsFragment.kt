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
import com.mgm.lostfound.data.repository.ItemRepository
import com.mgm.lostfound.data.repository.HybridRepository
import kotlinx.coroutines.flow.collectLatest
import com.mgm.lostfound.databinding.FragmentLostItemsBinding
import com.mgm.lostfound.ui.item.ReportItemActivity
import com.mgm.lostfound.ui.item.ItemDetailActivity
import com.mgm.lostfound.ui.adapters.ItemAdapter
import kotlinx.coroutines.launch

class LostItemsFragment : Fragment() {
    private var _binding: FragmentLostItemsBinding? = null
    private val binding get() = _binding!!
    private val itemRepository = ItemRepository()
    private val hybridRepository = HybridRepository()
    private lateinit var adapter: ItemAdapter

    companion object {
        fun newInstance() = LostItemsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLostItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadLostItems()

        binding.fabReportLost.setOnClickListener {
            val intent = Intent(requireContext(), ReportItemActivity::class.java)
            intent.putExtra("type", "LOST")
            startActivity(intent)
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadLostItems()
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

    private fun loadLostItems() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            // Use HybridRepository for Supabase real-time data
            val result = hybridRepository.getItems(ItemType.LOST)
            result.onSuccess { items ->
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
            hybridRepository.subscribeToItems(ItemType.LOST).collectLatest { item ->
                // Update adapter when new item arrives
                val currentList = adapter.currentList.toMutableList()
                val index = currentList.indexOfFirst { it.id == item.id }
                if (index >= 0) {
                    currentList[index] = item
                } else {
                    currentList.add(0, item)
                }
                adapter.submitList(currentList)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadLostItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

