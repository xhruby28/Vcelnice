package com.hruby.sharedresources.permissionHelpers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.hruby.sharedresources.dialogs.DeviceListDialog

object BluetoothHelper {

    // Vrací seznam spárovaných zařízení
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    // Skenuje okolní BLE zařízení
    @SuppressLint("MissingPermission")
    fun scanForDevices(
        context: Context,
        onResult: (List<BluetoothDevice>) -> Unit,
        onError: (String) -> Unit = { error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show() }
    ) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val devices = mutableListOf<BluetoothDevice>()

        val bleScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (!devices.contains(result.device)) devices.add(result.device)
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                results.forEach { result ->
                    if (!devices.contains(result.device)) devices.add(result.device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                onError("Skenování BLE selhalo: $errorCode")
            }
        }

        val bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val device =
                    intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                device?.let { if (!devices.contains(it)) devices.add(it) }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(bluetoothReceiver, filter)

        // Start BLE scan
        scanner.startScan(bleScanCallback)
        bluetoothAdapter.startDiscovery()

        // Stop scan after 10 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            scanner.stopScan(bleScanCallback)
            bluetoothAdapter.cancelDiscovery()
            context.unregisterReceiver(bluetoothReceiver)
            onResult(devices)
        }, 10000)
    }

    fun requestBluetoothDevices(
        context: Context,
        fragmentManager: FragmentManager,
        onDeviceSelected: (String) -> Unit, // Callback pro práci s vybraným zařízením
        onError: (String) -> Unit = { error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show() }
    ) {
        // Získání spárovaných zařízení
        val pairedDevices = getPairedDevices()

        // Vytvoření dialogu pro výběr zařízení
        val dialog = DeviceListDialog { macAddress ->
            onDeviceSelected(macAddress) // Callback s vybraným zařízením
        }

        dialog.show(fragmentManager, "DeviceListDialog")

        // Zahájení skenování zařízení
        scanForDevices(
            context,
            onResult = { nearbyDevices ->
                dialog.updateDeviceList(pairedDevices, nearbyDevices)
            },
            onError = onError
        )
    }

    // Filtr ESP32 zařízení podle MAC adresy
    fun isEsp32Device(macAddress: String): Boolean {
        return macAddress.startsWith("EC:DA:3B")
    }
}