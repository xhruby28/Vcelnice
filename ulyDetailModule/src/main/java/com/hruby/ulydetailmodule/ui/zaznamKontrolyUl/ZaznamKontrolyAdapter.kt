package com.hruby.ulydetailmodule.ui.zaznamKontrolyUl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.ulydetailmodule.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ZaznamKontrolyAdapter(
    private val zaznamy: List<ZaznamKontroly>,
    private val onDeleteClick: (ZaznamKontroly, Int) -> Unit, // Lambda pro mazání
    private val onItemClick: (ZaznamKontroly) -> Unit
) : RecyclerView.Adapter<ZaznamKontrolyAdapter.ZaznamViewHolder>() {

    inner class ZaznamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDatum: TextView = itemView.findViewById<TextView>(R.id.tvDatumKontroly)
        val tvTyp: TextView = itemView.findViewById<TextView>(R.id.tvTypKontroly)
        val ivMedobrani: ImageView = itemView.findViewById<ImageView>(R.id.ivMedobraniUl)
        val ivProblem: ImageView = itemView.findViewById<ImageView>(R.id.ivProblemovyUl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZaznamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ul_zaznam_kontroly_item, parent, false)
        return ZaznamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZaznamViewHolder, position: Int) {
        val zaznam = zaznamy[position]
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        holder.tvDatum.text = "Datum: ${sdf.format(Date(zaznam.datum))}"
        holder.ivProblem.visibility = if (zaznam.problemovyUl) View.VISIBLE else View.GONE

        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, zaznam, position)
            true
        }

        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(zaznam)
                }
        }
    }

    private fun showPopupMenu(view: View, zaznam: ZaznamKontroly, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_zaznam_kontroly)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_zaznam_kontroly_delete -> {
                    onDeleteClick(zaznam, position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int = zaznamy.size
}