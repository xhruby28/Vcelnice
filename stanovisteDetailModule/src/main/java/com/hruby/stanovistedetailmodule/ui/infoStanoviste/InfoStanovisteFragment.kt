package com.hruby.stanovistedetailmodule.ui.infoStanoviste

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hruby.sharedresources.helpers.ImageHelper
import com.hruby.sharedresources.helpers.PermissionHelper
import com.hruby.stanovistedetailmodule.databinding.FragmentInfoStanovisteBinding
import com.hruby.stanovistedetailmodule.R
import java.io.File

class InfoStanovisteFragment : Fragment() {
    private var _binding: FragmentInfoStanovisteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InfoStanovisteViewModel
    private var stanovisteId: Int = -1

    private var isStanovisteInfoFabMenuOpen = false

    private var currentPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)

        if (stanovisteId != -1) {
            // Inicializace ViewModelu s ID stanoviště
            val factory = InfoStanovisteViewModelFactory(requireContext(), stanovisteId)
            viewModel = ViewModelProvider(this, factory)[InfoStanovisteViewModel::class.java]
        } else {
            throw IllegalArgumentException("StanovisteId není validní.")
        }

        Log.d("UlyStanovisteFragment", "stanoviste id: $stanovisteId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInfoStanovisteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab: FloatingActionButton = view.findViewById(R.id.stanoviste_detail_info_fragment_fab)

        infoStanovisteFragmentText()

        fab.setOnClickListener {
            toggleStanovisteInfoFabMenu(fab)
        }

        binding.stanovisteDetailInfoFragmentFabSync.setOnClickListener{
            viewModel.stanoviste.observe(viewLifecycleOwner){ stanoviste ->
                if(stanoviste.maMAC){
                    //checkBluetoothPermissions()
                } else {
                    Toast.makeText(context, "Stanoviště není spárované, párování lze vytvořit pomocí UPRAVIT", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.stanovisteDetailInfoFragmentFabEdit.setOnClickListener{
//            TODO("Rozsáhlá možnost editovat paratmetry stanoviště, " +
//                    "včetně přidání obrázku z galerie nebo možnost rovnou vyfotit stanoviště," +
//                    " spárování s modulem SmartHive, pokud nemá MAC, nebo spárování s jiný modulem (změna MAC).")
//            TODO("Opravdu udělat to, že v UPRAVIT bude možnost spárovat se SmartHive")
            showImageSelectionDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Funkce pro fungování fragmentu a jeho částí
    private fun infoStanovisteFragmentText(){
        viewModel.stanoviste.observe(viewLifecycleOwner) { stanoviste ->
            binding.stanovisteDetailInfoFragmentNazevStanoviste.text = stanoviste.name
            binding.stanovisteDetailInfoFragmentStanovisteLastCheck.text = stanoviste.lastCheck
            binding.stanovisteDetailInfoFragmentStanovisteLastState.text = stanoviste.lastState

            val stringURL: String = stanoviste.locationUrl.toString()
            val arrayGPS: List<String> = stringURL.split("=")

            //holder.textLocationUrl.text = stanoviste.locationUrl
            var gpsCoordinates = if (arrayGPS.size > 1 && arrayGPS[1].isNotEmpty()) {
                arrayGPS[1]
            } else {
                "NULL, NULL"
            }

            binding.stanovisteDetailInfoFragmentStanovisteGps.text = gpsCoordinates

            if (stanoviste.maMAC) {
                binding.stanovisteDetailInfoFragmentStanovisteMaMac.text = stanoviste.siteMAC
            } else {
                binding.stanovisteDetailInfoFragmentStanovisteMaMac.text = String.format("Není spárované")
            }

            viewModel.ulyCount.observe(viewLifecycleOwner) { count -> binding.stanovisteDetailInfoFragmentStanovistePocetUlu.text = String.format("$count") }

            binding.stanovisteDetailInfoFragmentStanovisteGps.setOnClickListener {
                val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(stanoviste.locationUrl))
                mapIntent.setPackage("com.google.android.apps.maps")
                it.context.startActivity(mapIntent)
            }

//            TODO("Zde přidat další informace stanoviště pro Detailní infon např. notifikace, problemy v úlech")
//            TODO("Při načítání odkazu z databáze to problikává, po znovu")
            stanoviste.imagePath?.let { path ->
                Glide.with(requireContext())
                    .load(File(path))
                    .centerCrop()
                    .into(binding.stanovisteDetailInfoFragmentImage)
            }
        }
    }

    private fun toggleStanovisteInfoFabMenu(fab: FloatingActionButton) {
        if (isStanovisteInfoFabMenuOpen) {
            fab.setImageResource(com.hruby.sharedresources.R.drawable.arrow_upward_24dp)
            binding.stanovisteDetailInfoFragmentFabEdit.visibility = View.GONE
            binding.stanovisteDetailInfoFragmentFabSync.visibility = View.VISIBLE
        } else {
            fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            binding.stanovisteDetailInfoFragmentFabEdit.visibility = View.VISIBLE
            binding.stanovisteDetailInfoFragmentFabSync.visibility = View.VISIBLE
        }
        isStanovisteInfoFabMenuOpen = !isStanovisteInfoFabMenuOpen
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            showImageSelectionDialog()
        } else {
            Toast.makeText(context, "Oprávnění byla zamítnuta", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val newPath = ImageHelper.saveImageToInternalStorage(requireContext(), it)
            updateStanovisteImage(newPath)
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                val newPath = ImageHelper.saveImageToInternalStorage(requireContext(), uri)
                updateStanovisteImage(newPath)
            }
        }
    }

    // Funkce pro zobrazení dialogu pro výběr obrázku
    private fun showImageSelectionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Vyberte akci")
            .setItems(arrayOf("Vybrat foto z uložiště", "Vyfotit stanoviště")) { _, which ->
                when (which) {
                    0 -> {
                        if (PermissionHelper.hasPermissions(requireContext(), PermissionHelper.storagePermissionsRead)) {
                            ImageHelper.pickImageFromGallery(galleryLauncher)
                        } else {
                            PermissionHelper.requestPermissions(permissionLauncher, PermissionHelper.storagePermissionsRead)
                        }
                    }
                    1 -> {
                        //TODO("Focení/ukládání foto nefunguje")
                        if (PermissionHelper.hasPermissions(requireContext(), PermissionHelper.cameraPermissions)) {
                            currentPhotoUri = ImageHelper.captureImage(requireContext())
                            cameraLauncher.launch(currentPhotoUri)
                        } else {
                            PermissionHelper.requestPermissions(permissionLauncher, PermissionHelper.cameraPermissions)
                        }
                    }
                }
            }
            .show()
    }

    // Funkce pro aktualizaci obrázku pro stanoviště v databázi
    private fun updateStanovisteImage(imagePath: String?) {
        imagePath?.let { newImagePath ->
            viewModel.stanoviste.observe(viewLifecycleOwner) { stanoviste ->
                // Smazání starého obrázku, pokud existuje
                stanoviste.imagePath?.let { oldImagePath ->
                    ImageHelper.deleteImageFromInternalStorage(oldImagePath)
                }
                // Aktualizace cesty v databázi
                stanoviste.imagePath = newImagePath
                viewModel.updateStanoviste(stanoviste)

                Glide.with(requireContext())
                    .load("") // Prázdné načtení nebo placeholder
                    .into(binding.stanovisteDetailInfoFragmentImage)

                // Poté načtěte nový obrázek
                Glide.with(requireContext())
                    .load(File(newImagePath))
                    .centerCrop()
                    .into(binding.stanovisteDetailInfoFragmentImage)
            }
        }
    }

    // Bluetooth logika
    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val requiredPermissions = PermissionHelper.bluetoothPermissions.toSet()
            val grantedPermissions = permissions.filterValues { it }.keys

            if (requiredPermissions.all { it in grantedPermissions }) {
                synchronizeWithBTDevice()
            } else {
                Toast.makeText(context, "Bluetooth oprávnění nebyla udělena", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkBluetoothPermissions() {
        val bluetoothPermissions = PermissionHelper.bluetoothPermissions

        // Pokud je pro Bluetooth nějaké oprávnění potřeba, zkontrolujte ho
        if (bluetoothPermissions.isNotEmpty()) {
            val missingPermissions = bluetoothPermissions.filter {
                ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                bluetoothPermissionLauncher.launch(missingPermissions.toTypedArray())
            } else {
                synchronizeWithBTDevice()
            }
        } else {
            synchronizeWithBTDevice()
        }
    }

    private fun synchronizeWithBTDevice(){
        var stanovisteMAC: String

        viewModel.stanoviste.observe(viewLifecycleOwner){ stanoviste->
            stanovisteMAC = stanoviste.siteMAC.toString()
        }


    }
}