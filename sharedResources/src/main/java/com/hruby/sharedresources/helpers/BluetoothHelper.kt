package com.hruby.sharedresources.helpers

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentManager
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.viewModel.MereneHodnotyViewModel
import com.hruby.databasemodule.databaseLogic.viewModel.StanovisteViewModel
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.sharedresources.dialogs.DeviceListDialog
import java.util.UUID

object BluetoothHelper {
    private var bleGatt: BluetoothGatt? = null
    private var bleGattCharacteristic: BluetoothGattCharacteristic? = null

    private lateinit var ulyViewModel: UlyViewModel
    private lateinit var mereneHodnotyViewModel: MereneHodnotyViewModel
    private lateinit var stanovisteViewModel: StanovisteViewModel

    private var wakeLock: PowerManager.WakeLock? = null

    // UUID služby a charakteristiky
    private const val MY_SERVICE_UUID =         "f6bb2d8d-6ce2-4847-9d0e-47344f9ce34f" // Zadej skutečné UUID služby
    private const val MY_CHARACTERISTIC_UUID =  "d898d3cd-d161-4876-8858-c07024ab5136" // Zadej skutečné UUID charakteristiky

    private val receivedChunks = mutableMapOf<Int, String>()
    private var totalChunks = -1

    private var stanovisteMac: String = ""

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
                    Log.d("BluetoothHelper", "Services discovered")
                    initializeGattCharacteristics(gatt)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Zařízení připojeno, služby nalezeny.", Toast.LENGTH_SHORT).show()
                    }
                    onConnected() // Volání callbacku, když je připojeno
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
                val chunk = gatt.readCharacteristic(characteristic).toString()
                val (metadata, data) = chunk.split(":", limit = 2)
                val (index, total) = metadata.split("/").map { it.toInt() }

                receivedChunks[index] = data
                totalChunks = total

                // Potvrzení přijetí chunku
                sendAcknowledgment(index)

                // Kontrola, zda byly přijaty všechny chunky
                if (receivedChunks.size == totalChunks) {
                    val completeData = (0 until totalChunks).joinToString("") { receivedChunks[it] ?: "" }
                    processCompleteData(completeData, context)
                } else {
                    // Pokud chybí nějaký chunk, požádat o jeho opětovné odeslání
                    requestMissingChunk(index)
                }
            }
        })
    }

    private fun processCompleteData(csvData: String, context: Context) {
        val rows = csvData.split("\n")
        rows.forEach { row ->
            val values = row.split(",")
            val macAddress = values[0]

            val ul = ulyViewModel.getUlWithOthersByMACAndStanovisteMAC(macAddress, stanovisteMac).value
            val stanoviste = stanovisteViewModel.getStanovisteByMAC(stanovisteMac).value

            if (ul != null) {
                // Aktualizace úlu
                val mereneHodnoty = MereneHodnoty(
                    ulId = ul.ul.id,
                    datum = values[1].toLong(),
                    hmotnost = values[2].toFloat(),
                    teplotaUl = values[3].toFloat(),
                    vlhkostModul = values[4].toFloat(),
                    teplotaModul = values[5].toFloat(),
                    frekvence = values[6].toFloat(),
                    gyroX = values[7].toFloat(),
                    gyroY = values[8].toFloat(),
                    gyroZ = values[9].toFloat(),
                    accelX = values[10].toFloat(),
                    accelY = values[11].toFloat(),
                    accelZ = values[12].toFloat()
                )
                mereneHodnotyViewModel.insertMereneHodnoty(mereneHodnoty)
            } else {
                // Vytvoření nového úlu
                val createUl = Uly(
                    stanovisteId = stanoviste!!.id,
                    macAddress = macAddress,
                    cisloUlu = null,
                    popis = "Vygenerovaný úl s MAC: $macAddress",
                    maMAC = true
                )
                ulyViewModel.insertUl(createUl)
                val newUl = ulyViewModel.getUlWithOthersByMACAndStanovisteMAC(macAddress, stanovisteMac).value

                val mereneHodnoty = MereneHodnoty(
                    ulId = newUl!!.ul.id,
                    datum = values[1].toLong(),
                    hmotnost = values[2].toFloat(),
                    teplotaUl = values[3].toFloat(),
                    vlhkostModul = values[4].toFloat(),
                    teplotaModul = values[5].toFloat(),
                    frekvence = values[6].toFloat(),
                    gyroX = values[7].toFloat(),
                    gyroY = values[8].toFloat(),
                    gyroZ = values[9].toFloat(),
                    accelX = values[10].toFloat(),
                    accelY = values[11].toFloat(),
                    accelZ = values[12].toFloat()
                )
                mereneHodnotyViewModel.insertMereneHodnoty(mereneHodnoty)
            }
        }
        releaseWakeLock(context, true)
        disconnect()
    }

    // Inicializace charakteristiky z BluetoothGatt
    private fun initializeGattCharacteristics(gatt: BluetoothGatt) {
        // Získání služby podle UUID
        val service: BluetoothGattService? = gatt.getService(UUID.fromString(MY_SERVICE_UUID))
        if (service != null) {
            // Získání charakteristiky podle UUID
            bleGattCharacteristic = service.getCharacteristic(UUID.fromString(MY_CHARACTERISTIC_UUID))
            Log.d("BluetoothHelper", "Gatt characteristic initialized")
        }
    }

    // Odeslání příkazu pro synchronizaci
    fun sendSyncCommand(syncCommand: String, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sendSyncCommandForNewAPI(syncCommand, context)
        } else {
            sendSyncCommandForOldAPI(syncCommand, context)
        }
    }

    // Odeílání požadavku o znovu poslání chunku podle indexu
    private fun requestMissingChunk(index: Int) {
        val requestCommand = "RESEND:$index"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sendCommandForNewAPI(requestCommand)
        } else {
            sendCommandForOldAPI(requestCommand)
        }
    }

    // Odeslání potvrzení přijetí (ACK)
    @SuppressLint("MissingPermission")
    private fun sendAcknowledgment(chunkIndex: Int) {
        val ackCommand = "ACK:$chunkIndex"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sendCommandForNewAPI(ackCommand)
        } else {
            sendCommandForOldAPI(ackCommand)
        }
    }

    // Funkce pro odeslání synchronizačního příkazu pro starší API
    @SuppressLint("MissingPermission")
    private fun sendSyncCommandForOldAPI(syncCommand: String, context: Context) {
        // Použití starší metody pro nižší API level
        bleGattCharacteristic?.let {
            it.value = syncCommand.toByteArray(Charsets.UTF_8)
            val success = bleGatt?.writeCharacteristic(it) ?: false
            if (!success) {
                Log.e("BluetoothHelper", "Failed to write sync command")
                disconnect()
                releaseWakeLock(context, false)
            }
        }
    }

    // Funkce pro odeslání synchronizačního příkazu
    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.TIRAMISU) // Tento kód bude použit pouze pro API level 33 a vyšší
    private fun sendSyncCommandForNewAPI(syncCommand: String, context: Context) {
        bleGattCharacteristic?.let {
            val commandBytes = syncCommand.toByteArray(Charsets.UTF_8)
            val success = bleGatt?.writeCharacteristic(it, commandBytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) ?: false
            if (success == false) {
                Log.e("BluetoothHelper", "Failed to write sync command")
                disconnect()
                releaseWakeLock(context, false)
            }
        }
    }

    // Funkce pro odeslání synchronizačního pro starší API
    @SuppressLint("MissingPermission")
    private fun sendCommandForOldAPI(command: String) {
        // Použití starší metody pro nižší API level
        bleGattCharacteristic?.let {
            it.value = command.toByteArray(Charsets.UTF_8)
            val success = bleGatt?.writeCharacteristic(it) ?: false
            if (!success) {
                Log.e("BluetoothHelper", "Failed to write $command command")
            }
        }
    }

    // Funkce pro odeslání příkazu
    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.TIRAMISU) // Tento kód bude použit pouze pro API level 33 a vyšší
    private fun sendCommandForNewAPI(command: String) {
        bleGattCharacteristic?.let {
            val commandBytes = command.toByteArray(Charsets.UTF_8)
            val success = bleGatt?.writeCharacteristic(it, commandBytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) ?: false
            if (success == false) {
                Log.e("BluetoothHelper", "Failed to write $command command")
            }
        }
    }

    // Odpojení od zařízení BLE
    @SuppressLint("MissingPermission")
    fun disconnect() {
        Log.e("BluetoothHelper", "Closing connection now")
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
