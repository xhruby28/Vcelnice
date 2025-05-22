package com.hruby.ulydetailmodule.ui.namereneHodnotyUl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.ulydetailmodule.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NamereneHodnotyUlRecycleViewAdapter(
    private var hodnotyList: List<MereneHodnoty>
) : RecyclerView.Adapter<NamereneHodnotyUlRecycleViewAdapter.NamereneHodnotyViewHolder>() {

    class NamereneHodnotyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val datetime: TextView = itemView.findViewById(R.id.datum_cas_mereni)
        val tempUl: TextView = itemView.findViewById(R.id.temp_number)
        val tempModul: TextView = itemView.findViewById(R.id.temp_modul_number)
        val humModul: TextView = itemView.findViewById(R.id.hum_modul_number)
        val freq: TextView = itemView.findViewById(R.id.freq_number)
        val weight: TextView = itemView.findViewById(R.id.weight_number)
        val gyroX: TextView = itemView.findViewById(R.id.gyroX_number)
        val gyroY: TextView = itemView.findViewById(R.id.gyroY_number)
        val gyroZ: TextView = itemView.findViewById(R.id.gyroZ_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NamereneHodnotyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.namerene_hodnoty_item, parent, false)

        return NamereneHodnotyViewHolder(view)
    }

    override fun onBindViewHolder(holder: NamereneHodnotyViewHolder, position: Int) {
        val hodnoty = hodnotyList[position]
        holder.datetime.text = formatTimestamp(hodnoty.datum.toLong())
        holder.tempUl.text = String.format(hodnoty.teplotaUl.toString())
        holder.tempModul.text = String.format(hodnoty.teplotaModul.toString())
        holder.humModul.text = String.format(hodnoty.vlhkostModul.toString())
        holder.freq.text = String.format(hodnoty.frekvence.toString())
        holder.weight.text = String.format(hodnoty.hmotnost.toString())
        holder.gyroX.text = String.format(hodnoty.gyroX.toString())
        holder.gyroY.text = String.format(hodnoty.gyroY.toString())
        holder.gyroZ.text = String.format(hodnoty.gyroZ.toString())


    }

    override fun getItemCount(): Int {
        return hodnotyList.size
    }

    private fun formatTimestamp(timestamp: Long): String {
        // Vytvoříme instanci SimpleDateFormat s požadovaným formátem
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        // Převedeme Long timestamp na Date (sekundy -> milisekundy)
        val date = Date(timestamp * 1000)  // Převedení na milisekundy

        // Nastavení časové zóny (volitelné, pokud chcete použít místní časovou zónu)
        dateFormat.timeZone = TimeZone.getDefault()  // Místní časová zóna (pokud je potřeba)

        // Naformátujeme datum a vrátíme jako řetězec
        return dateFormat.format(date)
    }
}