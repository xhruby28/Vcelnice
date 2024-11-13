package com.hruby.stanovistedetailmodule

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.fragment.NavHostFragment
import com.hruby.databasemodule.databaseLogic.viewModel.StanovisteViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.StanovisteViewModelFactory
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.StanovisteRepository
import com.hruby.navmodule.Navigator
import com.hruby.stanovistedetailmodule.databinding.ActivityStanovisteDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StanovisteDetailActivity : AppCompatActivity(), Navigator {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityStanovisteDetailBinding

    private val stanovisteViewModel: StanovisteViewModel by viewModels {
        StanovisteViewModelFactory(StanovisteRepository(StanovisteDatabase.getDatabase(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStanovisteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val stanovisteId = intent.getIntExtra("stanovisteId", -1)
        // Zpracování stanovisteId podle potřeby

        // NavHostFragment pro správu fragmentů
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_stanoviste_detail) as NavHostFragment
        val navController = navHostFragment.navController

        // Základní konfigurace pro akční panel
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_info, R.id.nav_uly), binding.drawerLayoutStanoviste)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Nastavení NavigationView a jeho propojení s navController
        val navView: NavigationView = findViewById(R.id.nav_view_stanoviste)
        val headerView = navView.getHeaderView(0) // Získání hlavičky navigation view
        val navHeaderTitle = headerView.findViewById<TextView>(R.id.nav_stanoviste_detail_header_title) // Odkaz na TextView v headeru
        //val navHeaderImage = headerView.findViewById<ImageView>(R.id.nav_stanoviste_detail_header_image)

        navView.setupWithNavController(navController)

        stanovisteViewModel.getStanovisteById(stanovisteId).observe(this) { stanoviste ->
            navHeaderTitle.text = stanoviste?.name ?: "Stanoviště Detail"
            //navHeaderImage.src = stanoviste?.imageResId ?: "Stanoviště Detail"
        }

        val navMenu: MenuItem = navView.menu.findItem(R.id.nav_back_to_main)
        navMenu.setOnMenuItemClickListener {
            goBackToStanovisteList()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_stanoviste_detail)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Implementace metody pro návrat zpět na seznam stanovišť
    override fun goBackToStanovisteList() {
        finish()
    }

    // Prázdná metoda kvůli implementaci navigátoru
    override fun openStanovisteDetail(stanovisteId: Int) {
    }
}