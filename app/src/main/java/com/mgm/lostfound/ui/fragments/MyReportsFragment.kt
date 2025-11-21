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
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.data.repository.ItemRepository
import com.mgm.lostfound.databinding.FragmentMyReportsBinding
import com.mgm.lostfound.ui.item.ItemDetailActivity
import com.mgm.lostfound.ui.adapters.ItemAdapter
import kotlinx.coroutines.launch

class MyReportsFragment : Fragment() {
    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!
    private val itemRepository = ItemRepository()
    private val authRepository = AuthRepository()
    private lateinit var adapter: ItemAdapter

    companion object {
        fun newInstance() = MyReportsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadMyReports()

        binding.swipeRefresh.setOnRefreshListener {
            loadMyReports()
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

    private fun loadMyReports() {
        val userId = authRepository.getCurrentUser()?.uid ?: return
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val result = itemRepository.getMyReports(userId)
            result.onSuccess { items ->
                adapter.submitList(items)
                binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.swipeRefresh.isRefreshing = false
            }.onFailure { exception ->
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadMyReports()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

