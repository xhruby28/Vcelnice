package com.hruby.sharedresources.helpers

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.hruby.sharedresources.dialogs.DeviceListDialog
import java.util.UUID

object BluetoothHelper {
    private var bleGatt: BluetoothGatt? = null
    private var notifyGattCharacteristic: BluetoothGattCharacteristic? = null
    private var writeGattCharacteristic: BluetoothGattCharacteristic? = null

    private var wakeLock: PowerManager.WakeLock? = null

    // UUID služby a charakteristiky
    private const val MY_SERVICE_UUID =                         "f6bb2d8d-6ce2-4847-9d0e-47344f9ce34f" // Zadej skutečné UUID služby
    private const val MY_NOTIFICATION_CHARACTERISTIC_UUID =     "d898d3cd-d161-4876-8858-c07024ab5136" // Zadej skutečné UUID charakteristiky
    private const val MY_WRITE_CHARACTERISTIC_UUID =            "107c0a9c-df0a-423d-9f7c-faf0e5107408"

    private const val DESCRIPTOR_UUID =         "00002902-0000-1000-8000-00805f9b34fb"

    private var stanovisteMac: String = ""

    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled == true
    }

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

    // Připojení k BLE zařízení podle MAC adresy
    @SuppressLint("MissingPermission")
    fun connectToDevice(context: Context, macAddress: String, onConnected: () -> Unit, onError: (String) -> Unit) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = bluetoothAdapter.getRemoteDevice(macAddress)
        stanovisteMac = macAddress.toString()

        // Spojení s BLE zařízením
        bleGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BluetoothHelper", "Connected to device")
                    // Po připojení získáme služby zařízení
                    acquireWakeLock(context)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Zařízení připojeno", Toast.LENGTH_SHORT).show()
                    }
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BluetoothHelper", "Disconnected from device")
                    releaseWakeLock(context, false)
                    onError("Disconnected from device")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        Log.d("BluetoothHelper", "Services discovered")
                        initializeGattCharacteristics(gatt)
                        Toast.makeText(context, "Zařízení připojeno, služby nalezeny.", Toast.LENGTH_SHORT).show()

                        onConnected() // Volání callbacku, když je připojeno
                    }, 500)
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Nepovedlo se připojit k zařízením.", Toast.LENGTH_SHORT).show()
                    }
                    onError("Failed to discover services, status: $status")
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BluetoothHelper", "Characteristic read: ${gatt.readCharacteristic(characteristic)}")
                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BluetoothHelper", "Characteristic write successful")
                } else {
                    Log.d("BluetoothHelper", "Characteristic write failed, status: $status")
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == UUID.fromString(MY_NOTIFICATION_CHARACTERISTIC_UUID)) {
                    var message: String
                    message = characteristic.getStringValue(0)
                    Log.d("BluetoothHelper /CharChange", "Message received: $message")
                    when (message) {
                        "WIFI_STARTED" -> {
                            WiFiHelper.disconnectFromWiFi(context)
                            WiFiHelper.connectToWiFi(context,
                                onConnected = {
                                    // Kód, který se spustí po úspěšném připojení
                                    Log.d("BluetoothHelper /CharChange", "Device Wifi connected, ready to receive data")

                                    val url = "http://192.168.4.1:80/download"
                                    WiFiHelper.downloadDataFromESP32(url,context)
                                },
                                onError = { error ->
                                    // Kód pro ošetření chyby při připojování
                                    Log.e("BluetoothHelper /CharChange", "Error: $error")
                                }
                            )
                        }
                        "WIFI_FAILED" -> {
                            Log.e("BluetoothHelper /CharChange", "ESP WiFi Init failed")
                            disconnect(context, false)
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "WiFi na sběrnici se nepodařilo inicializovat", Toast.LENGTH_SHORT).show()
                            }
                        }
                        "SYNC_COMPLETE_ACK" -> {
                            disconnect(context, true)
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "Synchronizace hotova", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })
    }

    // Inicializace charakteristiky z BluetoothGatt
    @SuppressLint("MissingPermission")
    private fun initializeGattCharacteristics(gatt: BluetoothGatt) {
        // Získání služby podle UUID
        val service: BluetoothGattService? = gatt.getService(UUID.fromString(MY_SERVICE_UUID))
        if (service != null) {
            // Získání charakteristiky pro zápis příkazů
            writeGattCharacteristic = service.getCharacteristic(UUID.fromString(MY_WRITE_CHARACTERISTIC_UUID))

            // Nastavení notifikací
            notifyGattCharacteristic = service.getCharacteristic(UUID.fromString(MY_NOTIFICATION_CHARACTERISTIC_UUID))
            Log.d("BluetoothHelper", "Gatt characteristic initialized")
            gatt.setCharacteristicNotification(notifyGattCharacteristic, true)

            val descriptor = notifyGattCharacteristic?.getDescriptor(UUID.fromString(DESCRIPTOR_UUID))
            descriptor?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val enableNotificationValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(it, enableNotificationValue)
                    Log.d("BluetoothHelper","Descriptor initialized")
                } else {
                    it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(it)
                    Log.d("BluetoothHelper","Descriptor initialized")
                }
            }
        }
    }

    // Odeslání příkazu pro synchronizaci
    fun sendCommand(syncCommand: String, context: Context) {
        Thread.sleep(1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("BluetoothHelper SendCommand", "Using NewAPI")
            sendCommandForNewAPI(syncCommand, context)
        } else {
            Log.d("BluetoothHelper SendCommand", "Using OldAPI")
            sendCommandForOldAPI(syncCommand, context)
        }
    }

    // Funkce pro odeslání synchronizačního příkazu pro starší API
    @SuppressLint("MissingPermission")
    private fun sendCommandForOldAPI(syncCommand: String, context: Context) {
        // Použití starší metody pro nižší API level
        writeGattCharacteristic?.let {
            it.value = syncCommand.toByteArray(Charsets.UTF_8)
            val success = bleGatt?.writeCharacteristic(it) ?: false
            if (!success) {
                Log.e("BluetoothHelper", "Failed to write sync command")
                disconnect(context, false)
            }
        }
    }

    // Funkce pro odeslání synchronizačního příkazu
    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.TIRAMISU) // Tento kód bude použit pouze pro API level 33 a vyšší
    private fun sendCommandForNewAPI(syncCommand: String, context: Context) {
        writeGattCharacteristic?.let {
            val commandBytes = syncCommand.toByteArray(Charsets.UTF_8)
            val success = bleGatt?.writeCharacteristic(it, commandBytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) ?: false
            if (success == false) {
                Log.e("BluetoothHelper", "Failed to write sync command")
                disconnect(context, false)
            }
        }
    }

    // Odpojení od zařízení BLE
    @SuppressLint("MissingPermission")
    fun disconnect(context: Context, lock: Boolean) {
        Log.e("BluetoothHelper", "Closing connection now")
        WiFiHelper.disconnectFromWiFi(context)
        releaseWakeLock(context, lock)
        bleGatt?.disconnect()
        bleGatt?.close()
        bleGatt = null
    }

    private fun acquireWakeLock(context: Context) {
        if (wakeLock == null) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "BluetoothHelper:WakeLock"
            )
        }
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)
        Log.d("BluetoothHelper", "WakeLock acquired")
    }

    private fun releaseWakeLock(context: Context, success: Boolean) {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
            Log.d("BluetoothHelper", "WakeLock released")
        }

        if(success){
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Zařízení odpojeno. Data byla úspěšně získána.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Zařízení předčasně odpojeno.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
