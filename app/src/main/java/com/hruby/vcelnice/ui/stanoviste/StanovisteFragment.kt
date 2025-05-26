package com.hruby.vcelnice.ui.stanoviste

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.StanovisteRepository
import com.hruby.databasemodule.databaseLogic.viewModel.StanovisteViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.StanovisteViewModelFactory
import com.hruby.vcelnice.R
import com.hruby.vcelnice.databinding.FragmentStanovisteBinding
import com.hruby.navmodule.Navigator
import com.hruby.sharedresources.helpers.BluetoothHelper
import com.hruby.sharedresources.helpers.PermissionHelper
import com.hruby.vcelnice.ui.stanoviste.dialogs.EditDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StanovisteFragment : Fragment(), EditDialogFragment.EditDialogListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StanovisteRecycleViewAdapter
    private lateinit var stanovisteViewModel: StanovisteViewModel
    private val stanovisteList: MutableList<Stanoviste> = mutableListOf()

    private lateinit var emptyView: TextView

    @Inject
    lateinit var navigator: Navigator

    private var _binding: FragmentStanovisteBinding? = null
    private val binding get() = _binding!!

    private var isFabMenuOpen = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Navigator) {
            navigator = context
        } else {
            throw IllegalStateException("Host activity must implement Navigator")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStanovisteBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("StanovisteFragment", "onViewCreated called")

        emptyView = view.findViewById(R.id.emptyView)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val fab: FloatingActionButton = view.findViewById(R.id.stanoviste_fab)
        val fabMenu: LinearLayout = view.findViewById(R.id.fab_menu)
        val buttonAddApiaryConnect: Button = view.findViewById(R.id.button_add_smart_hive)
        val buttonAddLocation: Button = view.findViewById(R.id.button_add_location)

        // Nastavení listeneru pro FAB
        fab.setOnClickListener {
            toggleFabMenu(fab, fabMenu)
        }

        // Listener pro tlačítko "Přidat zařízení pomocí BT/BLE"
        buttonAddApiaryConnect.setOnClickListener {
            val bluetoothEnabled = BluetoothHelper.isBluetoothEnabled()
            if (bluetoothEnabled) {
                checkBluetoothPermissions()
            } else {
                Toast.makeText(requireContext(), "Zapněte prosím Bluetooth.", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener pro tlačítko "Přidat lokalitu bez ApiaryConnect Core"
        buttonAddLocation.setOnClickListener {
            openAddDialog("")
        }

        // Inicializace ViewModel
        val application = requireActivity().application
        val repository = StanovisteRepository(StanovisteDatabase.getDatabase(application))
        val factory = StanovisteViewModelFactory(repository)
        stanovisteViewModel = ViewModelProvider(this, factory)[StanovisteViewModel::class.java]

        // Pozorování na změny v LiveData
        stanovisteViewModel.allStanoviste.observe(viewLifecycleOwner) { stanoviste ->
            // Aktualizace seznamu
            stanovisteList.clear()
            stanovisteList.addAll(stanoviste)
            adapter.notifyDataSetChanged()

            if (stanovisteList.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        }

        adapter = StanovisteRecycleViewAdapter(
            stanovisteList,
            { stanoviste, position ->
//                TODO("Při kliknutí na možnost EDIT bude uživatel přesměrován na StanovisteDetailModule - Úprava," +
//                        "kde bude kompletní seznam věcí, které uživatel může změnit")
                showEditDialog(stanoviste, position)
            },
            { stanoviste, position ->
                showDeleteDialog(stanoviste, position)
            },
            { stanovisteId ->
                onStanovisteSelected(stanovisteId)
            }
        )
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Dialogy
    private fun showEditDialog(stanoviste: Stanoviste, position: Int) {
        val dialog = EditDialogFragment()
        val bundle = Bundle()
        bundle.putInt("index", position)
        bundle.putString("name", stanoviste.name)
        bundle.putString("lastCheck", stanoviste.lastCheck)
        bundle.putString("locationUrl", stanoviste.locationUrl)
        dialog.arguments = bundle

        dialog.show(childFragmentManager, "EditDialogFragment")
    }

    private fun openAddDialog(macAddress: String, maMac: Boolean = false) {
        val dialog = EditDialogFragment()
        val bundle = Bundle()
        bundle.putInt("index", -1)  // Přidání argumentů
        bundle.putString("macAddress", macAddress)
        bundle.putBoolean("maMac", maMac)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "EditDialogFragment")
    }

    private fun showDeleteDialog(stanoviste: Stanoviste, position: Int){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Potvrzení smazání")
            .setMessage("Opravdu chcete smazat stanoviště: ${stanoviste.name}?")
            .setPositiveButton("Ano") { dialog, _ ->
                // Volání funkce pro smazání
                stanovisteViewModel.deleteStanoviste(stanoviste)
                adapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton("Ne") { dialog, _ ->
                dialog.cancel()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onDialogSave(maMac:Boolean, macAddress: String, index: Int, name: String, lastCheck: String, locationUrl: String) {
        val fabMenu: LinearLayout = requireView().findViewById(R.id.fab_menu)
        val fab: FloatingActionButton = requireView().findViewById(R.id.stanoviste_fab)
        if (index in stanovisteList.indices) {
            val existingStanoviste = stanovisteList[index]
            existingStanoviste.name = name
            existingStanoviste.lastCheck = lastCheck
            existingStanoviste.locationUrl = locationUrl

            // Aktualizace databáze
            stanovisteViewModel.updateStanoviste(existingStanoviste)
            adapter.notifyItemChanged(index) // Označ nový stav položky
        } else {
            val newStanoviste = Stanoviste(
                name = name,
                maMAC = maMac,
                lastCheck = lastCheck,
                locationUrl = locationUrl,
                lastState = "Nové stanoviště",
                siteMAC = macAddress,
                //imageResId = 0 // Můžeš zde upravit výchozí hodnotu obrázku
            )
            stanovisteViewModel.insertStanoviste(newStanoviste)
            adapter.notifyItemChanged(index) // Označ nový stav položky
        }
        fab.setImageResource(android.R.drawable.ic_input_add)
        fabMenu.visibility = View.GONE
        isFabMenuOpen = false
    }

    private fun toggleFabMenu(fab: FloatingActionButton, fabMenu: LinearLayout) {
        if (isFabMenuOpen) {
            fab.setImageResource(android.R.drawable.ic_input_add)
            fabMenu.visibility = View.GONE
        } else {
            fab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            fabMenu.visibility = View.VISIBLE
        }
        isFabMenuOpen = !isFabMenuOpen
    }

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val requiredPermissions = PermissionHelper.bluetoothPermissions.toSet()
            val grantedPermissions = permissions.filterValues { it }.keys

            if (requiredPermissions.all { it in grantedPermissions }) {
                requestBluetoothDevices()
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

            // Pokud nějaká oprávnění chybí, požádejte o ně
            if (missingPermissions.isNotEmpty()) {
                bluetoothPermissionLauncher.launch(missingPermissions.toTypedArray())
            } else {
                // Pokud jsou všechna oprávnění udělena, pokračujte s Bluetooth operacemi
                requestBluetoothDevices()
            }
        } else {
            // Pro starší Android verze pokračujte s Bluetooth operacemi
            requestBluetoothDevices()
        }
    }

    private fun requestBluetoothDevices() {
        BluetoothHelper.requestBluetoothDevices(
            context = requireContext(),
            fragmentManager = childFragmentManager,
            onDeviceSelected = { macAddress ->
                if (BluetoothHelper.isEsp32Device(macAddress)) {
                    openAddDialog(macAddress, true) // Vlastní chování fragmentu
                } else {
                    Toast.makeText(context, "Neplatné ESP32 zařízení", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Navigace do modulu s detaily
    private fun onStanovisteSelected(stanovisteId: Int) {
        navigator.openStanovisteDetail(stanovisteId)
    }
}