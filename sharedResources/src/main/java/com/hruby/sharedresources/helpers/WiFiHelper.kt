package com.hruby.sharedresources.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.viewModel.MereneHodnotyViewModel
import com.hruby.databasemodule.databaseLogic.viewModel.StanovisteViewModel
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


object WiFiHelper {
    private const val WIFI_SSID = "Hive_Gateway"
    private const val WIFI_PASSWORD = "6m=JaFt0P=J0f!z}Sq69"

    private lateinit var ulyViewModel: UlyViewModel
    private lateinit var mereneHodnotyViewModel: MereneHodnotyViewModel
    private lateinit var stanovisteViewModel: StanovisteViewModel

    private var stanovisteMac: String = ""
    private var isInProcess: Boolean = false

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun initializeViewModels(
        ulyViewModel: UlyViewModel,
        mereneHodnotyViewModel: MereneHodnotyViewModel,
        stanovisteViewModel: StanovisteViewModel
    ) {
        this.ulyViewModel = ulyViewModel
        this.mereneHodnotyViewModel = mereneHodnotyViewModel
        this.stanovisteViewModel = stanovisteViewModel
    }

    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    fun writeMACForWifi(macAddress: String){
        stanovisteMac = macAddress.toString()
    }

    fun connectToWiFi(context: Context, onConnected: () -> Unit, onError: (String) -> Unit) {
        if(!isInProcess){
            isInProcess = true
            disconnectFromWiFi(context)

            Log.d("WiFiHelper", "Connection lock Enabled")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiSpecifier = WifiNetworkSpecifier.Builder()
                    .setSsid(WIFI_SSID)
                    .setWpa2Passphrase(WIFI_PASSWORD)
                    .build()

                val networkRequest = NetworkRequest.Builder()
                    .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiSpecifier)
                    .build()

                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                networkCallback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        connectivityManager.bindProcessToNetwork(network)
                        Log.d("WiFiHelperN", "Connection lock Disabled")
                        onConnected()
                        isInProcess = false
                    }

                    override fun onUnavailable() {
                        isInProcess = false
                        Log.d("WiFiHelperN", "Connection lock Disabled")
                        onError("WiFi connection failed")
                        BluetoothHelper.disconnect(context,false)
                    }
                }

                connectivityManager.requestNetwork(networkRequest, networkCallback!!)
            } else {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiConfig = WifiConfiguration().apply {
                    SSID = "\"$WIFI_SSID\""
                    preSharedKey = "\"$WIFI_PASSWORD\""
                }

                val networkId = wifiManager.addNetwork(wifiConfig)
                if (networkId != -1) {
                    wifiManager.enableNetwork(networkId, true)
                    Log.d("WiFiHelperO", "Connection lock Disabled")
                    onConnected()
                    isInProcess = false
                } else {
                    isInProcess = false
                    Log.d("WiFiHelperO", "Connection lock Disabled")
                    onError("WiFi configuration failed")
                    BluetoothHelper.disconnect(context,false)
                }
            }
        }
    }

    fun disconnectFromWiFi(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(it)
                Log.d("WiFiHelper", "Disconnected from WiFi")
            }
        } catch (e: Exception) {
            Log.w("WiFiHelper", "Tried to unregister a null or already unregistered callback")
        } finally {
            networkCallback = null
            connectivityManager.bindProcessToNetwork(null)
        }
        connectivityManager.bindProcessToNetwork(null)
    }

    fun downloadDataFromESP32(url: String, context: Context) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ESP32", "Failed to download data", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()

                    if (inputStream != null) {
                        val contentLength = response.body?.contentLength() ?: -1
                        Log.d("ESP32", "Content-Length: $contentLength")

                        val data = StringBuilder()
                        val buffer = ByteArray(1024)  // Velikost bufferu pro bloky
                        var bytesRead: Int

                        try {
                            while (true) {
                                // Čteme data po blocích
                                bytesRead = inputStream.read(buffer)
                                Log.d("ESP32", "Bytes received: $bytesRead")
                                if (bytesRead == -1) {
                                    break  // Konec streamu
                                }

                                // Přidáme přečtená data do StringBuilder
                                data.append(String(buffer, 0, bytesRead))
                            }

                            Log.d("ESP32", "Data received: $data")

                            // Zpracování dat
                            processCompleteData(data.toString(), context)
                        } catch (e: IOException) {
                            Log.e("ESP32", "Error reading stream", e)
                        } finally {
                            inputStream.close()  // Uzavření streamu
                        }
                    } else {
                        Log.e("ESP32", "Input stream is null")
                    }
                } else {
                    Log.e("ESP32", "Failed with code: ${response.code}")
                }
            }
        })
    }


    private fun processCompleteData(csvData: String, context: Context) {
        CoroutineScope(Dispatchers.IO).launch(){
            val rows = csvData.split("\n")
            rows.forEach { row ->
                val values = row.split(",")

                Log.d("WiFiHelper DataProcessingTask", "Processing data: $row")
                if (values.size == 13) {
                    val macAddress = values[0].toString()

                    Log.d("ESP32", "Site G MAC: $stanovisteMac")
                    Log.d("ESP32", "Ul MAC: $macAddress")

                    val ul = ulyViewModel.getUlWithOthersByMACAndStanovisteMAC(macAddress, stanovisteMac)
                    val stanoviste = stanovisteViewModel.getStanovisteByMAC(stanovisteMac.toString())

                    Log.d(
                        "WiFiHelper DataProcessingTask",
                        "Ul: ${ul?.id ?: "Ul nenalezen"}, UlMAC: ${ul?.macAddress ?: "Ul nenalezen"}, Stanoviste: ${stanoviste?.id ?: "Stanoviste nenalezeno"}"
                    )

                    if (stanoviste != null){
                        if (ul != null) {
                            // Aktualizace úlu
                            Log.d("WiFiHelper DataProcessingTask", "Data belong to Ul: ${ul.id}")
                            val mereneHodnoty = MereneHodnoty(
                                ulId = ul.id,
                                datum = values[1].toLong(),
                                hmotnost = values[2].toFloat(),
                                teplotaUl = values[3].toFloat(),
                                vlhkostModul = values[4].toFloat(),
                                teplotaModul = values[5].toFloat(),
                                frekvence = values[6].toFloatOrNull() ?: -127f,
                                gyroX = values[7].toFloat(),
                                gyroY = values[8].toFloat(),
                                gyroZ = values[9].toFloat(),
                                accelX = values[10].toFloat(),
                                accelY = values[11].toFloat(),
                                accelZ = values[12].toFloat()
                            )
                            mereneHodnotyViewModel.checkAndInsertMereneHodnoty(
                                ul.id,
                                macAddress.toString(),
                                values[1].toLong(),
                                mereneHodnoty
                            )
                        } else {
                            // Vytvoření nového úlu
                            val createUl = Uly(
                                stanovisteId = stanoviste.id,
                                macAddress = macAddress,
                                cisloUlu = null,
                                popis = "Vygenerovaný úl s MAC: $macAddress",
                                maMAC = true
                            )

                            ulyViewModel.insertUl(createUl)
                            var newUl = ulyViewModel.getUlWithOthersByMACAndStanovisteMAC(macAddress, stanovisteMac)

                            val startTime = System.currentTimeMillis()
                            val timeout = 5000L  // např. timeout 5 sekund
                            while (newUl == null && (System.currentTimeMillis() - startTime) < timeout) {
                                newUl = ulyViewModel.getUlWithOthersByMACAndStanovisteMAC(macAddress, stanovisteMac)
                                delay(100)  // Krátká pauza před dalším pokusem, aby nedošlo k blokování hlavního vlákna
                            }
                            Log.d("WiFiHelper DataProcessingTask","Inserting into new ul: ${newUl?.id}")

                            if (newUl != null) {
                                val mereneHodnoty = MereneHodnoty(
                                    ulId = newUl.id,
                                    datum = values[1].toLong(),
                                    hmotnost = values[2].toFloat(),
                                    teplotaUl = values[3].toFloat(),
                                    vlhkostModul = values[4].toFloat(),
                                    teplotaModul = values[5].toFloat(),
                                    frekvence = values[6].toFloatOrNull() ?: -127f,
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
                    } else {
                        Log.d("WiFiHelper DataProcessing", "Stanoviste not found")
                    }
                }
            }
            BluetoothHelper.sendCommand("SYNC_COMPLETE", context)
            //disconnect(context, true)
        }
    }
}