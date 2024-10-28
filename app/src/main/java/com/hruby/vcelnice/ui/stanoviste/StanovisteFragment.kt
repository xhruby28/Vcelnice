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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hruby.vcelnice.R
import com.hruby.vcelnice.databinding.FragmentStanovisteBinding
import com.hruby.vcelnice.ui.stanoviste.database.StanovisteDatabase
import com.hruby.vcelnice.ui.stanoviste.database.StanovisteRepository
import com.hruby.vcelnice.ui.stanoviste.database.StanovisteViewModelFactory
import com.hruby.vcelnice.ui.stanoviste.dialogs.DeviceListDialog
import com.hruby.vcelnice.ui.stanoviste.dialogs.EditDialogFragment

class StanovisteFragment : Fragment(), EditDialogFragment.EditDialogListener{

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StanovisteRecycleViewAdapter
    private lateinit var stanovisteViewModel: StanovisteViewModel
    private val stanovisteList: MutableList<Stanoviste> = mutableListOf()

    private var _binding: FragmentStanovisteBinding? = null
    private val binding get() = _binding!!

    // BLE
    private val bluetoothPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                requestBluetoothDevices()
            } else {
                Toast.makeText(context, "Přístup k Bluetooth byl zamítnut", Toast.LENGTH_SHORT).show()
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

        val fab: FloatingActionButton = view.findViewById(R.id.stanoviste_add)
        fab.setOnClickListener {
            checkBluetoothPermissions()
//            val dialog = EditDialogFragment()
//            val bundle = Bundle()
//            bundle.putInt("index", -1)  // Přidání argumentů
//            dialog.arguments = bundle
//            dialog.show(childFragmentManager, "EditDialogFragment")
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
            }
        )
        recyclerView.adapter = adapter
    }

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

    override fun onDialogSave(index: Int, name: String, lastCheck: String, locationUrl: String) {
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
                imageResId = 0 // Můžeš zde upravit výchozí hodnotu obrázku
            )
            stanovisteViewModel.insertStanoviste(newStanoviste)
            adapter.notifyItemChanged(index) // Označ nový stav položky
        }
    }

    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
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
                openEditDialog(macAddress)
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
    private fun getPairedBluetoothDevices(): List<BluetoothDevice> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    // Skenuje okolní zařízení a volá callback s nalezenými zařízeními
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

    private fun openEditDialog(macAddress: String) {
        val dialog = EditDialogFragment()
        val bundle = Bundle()
        bundle.putInt("index", -1)  // Přidání argumentů
        bundle.putString("macAddress", macAddress)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "EditDialogFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}