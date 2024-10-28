package com.hruby.vcelnice.ui.stanoviste.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hruby.vcelnice.R

class DeviceListDialog(
    private val onDeviceSelected: (String) -> Unit
) : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceAdapter
    private val devices: MutableList<BluetoothDevice> = mutableListOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_device_list, null)
        builder.setView(view)
            .setTitle("Vyberte zařízení")
            .setNegativeButton("Zavřít") { dialog, _ -> dialog.dismiss() }

        recyclerView = view.findViewById(R.id.recyclerViewDevices)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = DeviceAdapter(devices) { device ->
            onDeviceSelected(device.address)
            dismiss()
        }
        recyclerView.adapter = adapter

        return builder.create()
    }

    fun updateDeviceList(newDevices: List<BluetoothDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        adapter.notifyDataSetChanged()
    }

    private fun pairWithDevice(device: BluetoothDevice) {
        // Logika pro spárování se zařízením
        Toast.makeText(context, "Párování s ${device.name}", Toast.LENGTH_SHORT).show()
        // Volání openEditDialog nebo jiné funkce
    }

    inner class DeviceAdapter(
        private val devices: List<BluetoothDevice>,
        private val onDeviceClick: (BluetoothDevice) -> Unit
    ) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val deviceName: TextView = view.findViewById(R.id.device_name)
            val deviceAddress: TextView = view.findViewById(R.id.device_address)

            init {
                view.setOnClickListener {
                    onDeviceClick(devices[adapterPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = devices[position]
            holder.deviceName.text = device.name
            holder.deviceAddress.text = device.address
        }

        override fun getItemCount(): Int {
            return devices.size
        }
    }
}