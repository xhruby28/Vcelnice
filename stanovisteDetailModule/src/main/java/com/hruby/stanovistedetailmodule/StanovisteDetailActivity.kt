package com.hruby.stanovistedetailmodule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.fragment.NavHostFragment
import com.hruby.stanovistedetailmodule.databinding.ActivityStanovisteDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StanovisteDetailActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityStanovisteDetailBinding

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
        navView.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_stanoviste_detail)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}