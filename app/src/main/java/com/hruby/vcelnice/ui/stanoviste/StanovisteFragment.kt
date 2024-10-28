package com.hruby.vcelnice.ui.stanoviste

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Bundle
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
        // Získání dostupných Bluetooth zařízení
        val availableDevices = getAvailableBluetoothDevices()

        // Předání seznamu zařízení dialogu po jeho vytvoření
        val dialog = DeviceListDialog { macAddress ->
            Log.d("StanovisteFragment", "MAC address: $macAddress")

            if (isEsp32Device(macAddress)) {
                openEditDialog(macAddress)
            } else {
                Toast.makeText(context, "Neplatné ESP32 zařízení", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show(childFragmentManager, "DeviceListDialog")

        // Aktualizace seznamu zařízení po zobrazení dialogu
        childFragmentManager.executePendingTransactions() // Zajistí, že dialog je zobrazen
        dialog.updateDeviceList(availableDevices)
    }

    private fun getAvailableBluetoothDevices(): List<BluetoothDevice> {
        val bluetoothAdapter: BluetoothAdapter? = (requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter?.bondedDevices ?: emptySet()
        return pairedDevices.toList() // Nahraďte tímto, pokud chcete mít i ne-párovaná zařízení
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