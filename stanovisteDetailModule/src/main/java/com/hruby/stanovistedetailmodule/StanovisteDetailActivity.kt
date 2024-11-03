package com.hruby.stanovistedetailmodule

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import com.hruby.stanovistedetailmodule.databinding.ActivityStanovisteDetailBinding

class StanovisteDetailActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityStanovisteDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStanovisteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarStanovisteDetail.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        Log.d("NavController", "Initializace navController...")
        //val navController = findNavController(R.id.nav_host_fragment_content_stanoviste_detail)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_stanoviste_detail) as NavHostFragment
        val navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_info
            ), drawerLayout
        )
        Log.d("NavController", "Initializace navbaru...")
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_stanoviste_detail)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}