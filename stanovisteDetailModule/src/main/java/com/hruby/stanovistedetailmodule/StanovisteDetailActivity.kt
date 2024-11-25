package com.hruby.stanovistedetailmodule

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
import com.hruby.ulydetailmodule.UlDetailActivity
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class StanovisteDetailActivity : AppCompatActivity(), Navigator {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityStanovisteDetailBinding

    private var stanovisteId: Int = -1
    private var previousImagePath: String? = null

    private val stanovisteViewModel: StanovisteViewModel by viewModels {
        StanovisteViewModelFactory(StanovisteRepository(StanovisteDatabase.getDatabase(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStanovisteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        stanovisteId = intent.getIntExtra("stanovisteId", -1)
        // Zpracování stanovisteId podle potřeby

        // NavHostFragment pro správu fragmentů
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_stanoviste_detail) as NavHostFragment
        val navController = navHostFragment.navController

        // Základní konfigurace pro akční panel
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_info, R.id.nav_uly
            ),
            binding.drawerLayoutStanoviste
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Nastavení NavigationView a jeho propojení s navController
        val navView: NavigationView = findViewById(R.id.nav_view_stanoviste)
        val headerView = navView.getHeaderView(0) // Získání hlavičky navigation view
        val navHeaderTitle = headerView.findViewById<TextView>(R.id.nav_stanoviste_detail_header_title) // Odkaz na TextView v headeru
        val navHeaderImage = headerView.findViewById<ImageView>(R.id.nav_stanoviste_detail_header_image)

        navView.setupWithNavController(navController)

        stanovisteViewModel.getStanovisteById(stanovisteId).observe(this) { stanoviste ->
            navHeaderTitle.text = stanoviste?.name ?: "Stanoviště Detail"

            if (stanoviste.imagePath != previousImagePath) {
                previousImagePath = stanoviste.imagePath
                loadImage(stanoviste.imagePath, navHeaderImage)
            }
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

    override fun openUlDetail(ulId: Int) {
        val intent = Intent(this, UlDetailActivity::class.java)
        intent.putExtra("ulId", ulId)
        intent.putExtra("stanovisteId", stanovisteId)
        startActivity(intent)
    }

    // Prázdná metoda kvůli implementaci navigátoru
    override fun openStanovisteDetail(stanovisteId: Int) {
    }

    private fun loadImage(imagePath: String?, view: ImageView) {
        imagePath?.let { path ->
            val file = File(this.filesDir, path)

            if (file.exists()) {
                previousImagePath = path // Aktualizuj uloženou cestu k obrázku
                Log.d("InfoStanovisteFragment", "Loading image from: $path")
                val uri = FileProvider.getUriForFile(
                    this,
                    "${this.packageName}.fileprovider",
                    file
                )
                Picasso.get()
                    .load(uri)
                    .placeholder(com.hruby.sharedresources.R.drawable.ic_launcher_background)
                    .into(view)
            } else {
                Log.e("InfoStanovisteFragment", "File does not exist at: $path")
            }
        }
    }
}