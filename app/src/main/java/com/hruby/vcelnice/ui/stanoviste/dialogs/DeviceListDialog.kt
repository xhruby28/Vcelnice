package com.hruby.vcelnice.ui.stanoviste.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hruby.vcelnice.R

class DeviceListDialog(
    private val onDeviceSelected: (String) -> Unit
) : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceAdapter
    private val pairedDevices: MutableList<BluetoothDevice> = mutableListOf()
    private val nearbyDevices: MutableList<BluetoothDevice> = mutableListOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_device_list, null)
        builder.setView(view)
            // přidat Toast při přesměrování na nastavní BT ve znení "Vyberte zařízení s názvem (Třeba SmartHive)...
            .setTitle("Vyberte zařízení")
            .setNegativeButton("Zavřít") { dialog, _ -> dialog.dismiss() }

        recyclerView = view.findViewById(R.id.recyclerViewDevices)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = DeviceAdapter(pairedDevices, nearbyDevices) { device ->
            onDeviceSelected(device.address)
            dismiss()
        }
        recyclerView.adapter = adapter

        return builder.create()
    }

    fun updateDeviceList(pairedDevicesList: List<BluetoothDevice>, nearbyDevicesList: List<BluetoothDevice>) {
        pairedDevices.clear()
        pairedDevices.addAll(pairedDevicesList)

        nearbyDevices.clear()
        nearbyDevices.addAll(nearbyDevicesList)

        adapter.notifyDataSetChanged()
    }

    inner class DeviceAdapter(
        private val pairedDevices: List<BluetoothDevice>,
        private val nearbyDevices: List<BluetoothDevice>,
        private val onDeviceClick: (BluetoothDevice) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val VIEW_TYPE_HEADER = 0
        private val VIEW_TYPE_DEVICE = 1

        // Vrací typ položky na základě pozice
        override fun getItemViewType(position: Int): Int {
            return when (position) {
                0 -> VIEW_TYPE_HEADER // Nadpis "Spárovaná zařízení"
                pairedDevices.size + 1 -> VIEW_TYPE_HEADER // Nadpis "Dostupná zařízení"
                else -> VIEW_TYPE_DEVICE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_HEADER) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
                DeviceViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is HeaderViewHolder) {
                holder.bind(if (position == 0) "Spárovaná zařízení" else "Dostupná zařízení")
            } else if (holder is DeviceViewHolder) {
                val device = if (position <= pairedDevices.size) pairedDevices[position - 1] else nearbyDevices[position - pairedDevices.size - 2]
                holder.bind(device)
            }
        }

        override fun getItemCount(): Int {
            // +2 pro nadpisy
            return pairedDevices.size + nearbyDevices.size + 2
        }

        // ViewHolder pro nadpis
        inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val headerTitle: TextView = view.findViewById(R.id.header_title)

            fun bind(title: String) {
                headerTitle.text = title
            }
        }

        // ViewHolder pro zařízení
        inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val deviceName: TextView = view.findViewById(R.id.device_name)
            private val deviceAddress: TextView = view.findViewById(R.id.device_address)

            init {
                view.setOnClickListener {
                    val device = if (adapterPosition <= pairedDevices.size) pairedDevices[adapterPosition - 1] else nearbyDevices[adapterPosition - pairedDevices.size - 2]
                    onDeviceClick(device)
                }
            }

            @SuppressLint("MissingPermission")
            fun bind(device: BluetoothDevice) {
                deviceName.text = device.name ?: "Neznámé zařízení"
                deviceAddress.text = device.address
            }
        }
    }
}