package com.hruby.ulydetailmodule

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.UlyViewModelFactory
import com.hruby.ulydetailmodule.databinding.ActivityUlDetailBinding

class UlDetailActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityUlDetailBinding

    private var stanovisteId: Int = -1
    private var ulId: Int = -1

    private val ulyViewModel: UlyViewModel by viewModels{
        UlyViewModelFactory(UlyRepository(StanovisteDatabase.getDatabase(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUlDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarUl)

        ulId = intent.getIntExtra("ulId", -1)
        stanovisteId = intent.getIntExtra("stanovisteId", -1)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_ul_detail) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_info_ul, R.id.nav_namerene_hodnoty_ul, R.id.nav_zaznam_ul//, R.id.nav_poznamky_ul, R.id.nav_problemy_historie_ul
            ),
            binding.drawerLayoutUl
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Nastavení NavigationView a jeho propojení s navController
        val navView: NavigationView = findViewById(R.id.nav_view_ul)
        val headerView = navView.getHeaderView(0)
        val navHeaderTitle = headerView.findViewById<TextView>(R.id.nav_ul_detail_header_title)


        navView.setupWithNavController(navController)

        val navMenu: MenuItem = navView.menu.findItem(R.id.nav_back_to_stanoviste)
        navMenu.setOnMenuItemClickListener {
            goBackToUlList()
            true
        }


        ulyViewModel.getUlWithOthersByStanovisteId(ulId, stanovisteId).observe(this){ ul ->
            val cisloUlu = ul.ul.cisloUlu
            navHeaderTitle.text = String.format("Úl číslo $cisloUlu")
            val navMereneHodnoty: MenuItem = navView.menu.findItem(R.id.nav_namerene_hodnoty_ul)
            navMereneHodnoty.isVisible = ul.ul.maMAC
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_ul_detail)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun goBackToUlList(){
        finish()
    }
}