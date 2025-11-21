package com.mgm.lostfound.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mgm.lostfound.data.model.User
import com.mgm.lostfound.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: User

    companion object {
        fun newInstance(user: User) = ProfileFragment().apply {
            arguments = Bundle().apply {
                putParcelable("user", user)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = arguments?.getParcelable("user") ?: return

        binding.tvName.text = user.name
        binding.tvEmail.text = user.email
        binding.tvStudentId.text = user.studentId
        binding.tvPhone.text = user.phone
        binding.tvDepartment.text = user.department
        binding.tvYear.text = user.year
        binding.tvRole.text = user.role.name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

