package com.boolder.boolder.view.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.boolder.boolder.R
import com.boolder.boolder.data.premium.repository.PremiumRepository
import com.boolder.boolder.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val premiumRepository by inject<PremiumRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            binding.root.post { premiumRepository.showInAppMessages(this) }
        }

        val navController = findNavController()

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.isVisible = destination.id in BOTTOM_BAR_DESTINATION_IDS
        }
    }

    override fun onResume() {
        super.onResume()
        premiumRepository.refreshPurchases()
        premiumRepository.loadProduct()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        findNavController().handleDeepLink(intent)
    }

    private fun findNavController(): NavController {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        return navHostFragment.navController
    }

    companion object {
        private val BOTTOM_BAR_DESTINATION_IDS = arrayOf(
            R.id.map_fragment,
            R.id.discover_fragment,
            R.id.tick_list_fragment,
            R.id.premium_fragment,
            R.id.dialog_circuit_filter,
            R.id.dialog_grades_filter
        )
    }
}
