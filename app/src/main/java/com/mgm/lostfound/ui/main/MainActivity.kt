package com.mgm.lostfound.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mgm.lostfound.R
import com.mgm.lostfound.data.model.User
import com.mgm.lostfound.data.model.UserRole
import com.mgm.lostfound.data.repository.AuthRepository
import com.mgm.lostfound.databinding.ActivityMainBinding
import com.mgm.lostfound.ui.admin.AdminDashboardActivity
import com.mgm.lostfound.ui.auth.LoginActivity
import com.mgm.lostfound.ui.fragments.FoundItemsFragment
import com.mgm.lostfound.ui.fragments.LostItemsFragment
import com.mgm.lostfound.ui.fragments.MyReportsFragment
import com.mgm.lostfound.ui.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentUser: User
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getParcelableExtra("user") ?: return

        setupBottomNavigation()
        loadFragment(LostItemsFragment.newInstance())
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_lost -> {
                    loadFragment(LostItemsFragment.newInstance())
                    true
                }
                R.id.nav_found -> {
                    loadFragment(FoundItemsFragment.newInstance())
                    true
                }
                R.id.nav_my_reports -> {
                    loadFragment(MyReportsFragment.newInstance())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment.newInstance(currentUser))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if (currentUser.role != UserRole.ADMIN && currentUser.role != UserRole.SECURITY) {
            menu.findItem(R.id.menu_admin).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_admin -> {
                val intent = Intent(this, AdminDashboardActivity::class.java)
                intent.putExtra("user", currentUser)
                startActivity(intent)
                true
            }
            R.id.menu_logout -> {
                authRepository.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getCurrentUser(): User = currentUser
}

