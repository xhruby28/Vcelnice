package com.hruby.vcelnice.ui.stanoviste

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
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
import com.hruby.vcelnice.ui.stanoviste.dialogs.DeviceListDialog
import com.hruby.vcelnice.ui.stanoviste.dialogs.EditDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StanovisteFragment : Fragment(), EditDialogFragment.EditDialogListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StanovisteRecycleViewAdapter
    private lateinit var stanovisteViewModel: StanovisteViewModel
    private val stanovisteList: MutableList<Stanoviste> = mutableListOf()

    @Inject
    lateinit var navigator: Navigator

    private var _binding: FragmentStanovisteBinding? = null
    private val binding get() = _binding!!

    private var isFabMenuOpen = false

    // BLE
    private val bluetoothPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                requestBluetoothDevices()
            } else {
                Toast.makeText(context, "Přístup k Bluetooth byl zamítnut", Toast.LENGTH_SHORT)
                    .show()
            }
        }

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

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val fab: FloatingActionButton = view.findViewById(R.id.stanoviste_fab)
        val fabMenu: LinearLayout = view.findViewById(R.id.fab_menu)
        val buttonAddSmartHive: Button = view.findViewById(R.id.button_add_smart_hive)
        val buttonAddLocation: Button = view.findViewById(R.id.button_add_location)

        // Nastavení listeneru pro FAB
        fab.setOnClickListener {
            toggleFabMenu(fab, fabMenu)
        }

        // Listener pro tlačítko "Přidat zařízení pomocí BT/BLE"
        buttonAddSmartHive.setOnClickListener {
            checkBluetoothPermissions()
        }

        // Listener pro tlačítko "Přidat lokalitu bez SmartHive"
        buttonAddLocation.setOnClickListener {
            openAddDialog("Lokalita bez SmartHive")
        }

        // Inicializace ViewModel
        val application = requireActivity().application
        val repository = StanovisteRepository(StanovisteDatabase.getDatabase(application).stanovisteDao())
        val factory = StanovisteViewModelFactory(repository)
        stanovisteViewModel = ViewModelProvider(this, factory).get(StanovisteViewModel::class.java)

        // Pozorování na změny v LiveData
        stanovisteViewModel.allStanoviste.observe(viewLifecycleOwner) { stanoviste ->
            // Aktualizace seznamu
            stanovisteList.clear()
            stanovisteList.addAll(stanoviste)
            adapter.notifyDataSetChanged()
        }

        adapter = StanovisteRecycleViewAdapter(
            stanovisteList,
            { stanoviste, position ->
                showEditDialog(stanoviste, position)
            },
            { stanoviste, position ->
                showDeleteDialog(stanoviste, position)
            },
            { stanovisteId ->
//                val action = StanovisteFragmentDirections.actionNavStanovisteToStanovisteInfoModule(stanovisteId)
//                findNavController().navigate(action)
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

    private fun openAddDialog(macAddress: String) {
        val dialog = EditDialogFragment()
        val bundle = Bundle()
        bundle.putInt("index", -1)  // Přidání argumentů
        bundle.putString("macAddress", macAddress)
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

    override fun onDialogSave(macAddress: String, index: Int, name: String, lastCheck: String, locationUrl: String) {
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
                lastCheck = lastCheck,
                locationUrl = locationUrl,
                lastState = "Nové stanoviště",
                siteMAC = macAddress,
                imageResId = 0 // Můžeš zde upravit výchozí hodnotu obrázku
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

    // Bluetooth
    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )

            val missingPermissions = permissions.filter {
                ActivityCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                bluetoothPermissionsLauncher.launch(missingPermissions.toTypedArray())
            } else {
                requestBluetoothDevices()
            }
        } else {
            requestBluetoothDevices()
        }
    }

    private fun requestBluetoothDevices() {
        // Získání spárovaných zařízení
        val pairedDevices = getPairedBluetoothDevices()

        val dialog = DeviceListDialog { macAddress ->
            if (isEsp32Device(macAddress)) {
                openAddDialog(macAddress)
            } else {
                Toast.makeText(context, "Neplatné ESP32 zařízení", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show(childFragmentManager, "DeviceListDialog")

        // Zahájení skenování dostupných zařízení
        scanForNearbyDevices { nearbyDevices ->
            // Sloučení spárovaných a dostupných zařízení a jejich zobrazení
            dialog.updateDeviceList(pairedDevices, nearbyDevices)
            if (pairedDevices.isEmpty()){
                Log.d("StanovisteFragment", "Seznam pairedDevices je prázdný")
            } else {
                Log.d("StanovisteFragment", "Seznam pairedDevices není prázdný")
            }

            if (nearbyDevices.isEmpty()){
                Log.d("StanovisteFragment", "Seznam nearbyDevices je prázdný")
            } else {
                Log.d("StanovisteFragment", "Seznam nearbyDevices není prázdný")
            }
        }
    }

    // Vrací seznam spárovaných zařízení
    @SuppressLint("MissingPermission")
    private fun getPairedBluetoothDevices(): List<BluetoothDevice> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    // Skenuje okolní zařízení a volá callback s nalezenými zařízeními
    @SuppressLint("MissingPermission")
    private fun scanForNearbyDevices(callback: (List<BluetoothDevice>) -> Unit) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val allDevices: MutableList<BluetoothDevice> = mutableListOf()  // MutableList pro dynamickou úpravu seznamu zařízení

        // BLE Scan Callback
        val bleScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (!allDevices.contains(result.device)) {
                    allDevices.add(result.device)
                }
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                results.forEach { result ->
                    if (!allDevices.contains(result.device)) {
                        allDevices.add(result.device)
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Toast.makeText(context, "BLE skenování se nezdařilo", Toast.LENGTH_SHORT).show()
            }
        }

        // Classic Bluetooth Scan Callback
        val bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (BluetoothDevice.ACTION_FOUND == intent?.action) {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (!allDevices.contains(it)) {
                            allDevices.add(it)
                        }
                    }
                }
            }
        }

        // Registrovat přijímač pro běžné Bluetooth zařízení
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context?.registerReceiver(bluetoothReceiver, filter)

        // Zahájit skenování BLE
        scanner.startScan(bleScanCallback)
        // Zahájit skenování klasických BT zařízení
        bluetoothAdapter.startDiscovery()

        // Ukončit skenování po 10 sekundách a vrátit seznam zařízení
        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(bleScanCallback)
            bluetoothAdapter.cancelDiscovery()
            context?.unregisterReceiver(bluetoothReceiver)
            callback(allDevices)
        }, 10000)
    }

    private fun isEsp32Device(macAddress: String): Boolean {
        // Filtr na MAC adresu ESP zařízení
        return macAddress.startsWith("EC:DA:3B")
    }

    // Navigace do modulu s detaily
    private fun onStanovisteSelected(stanovisteId: Int) {
        navigator.openStanovisteDetail(stanovisteId)
    }
}