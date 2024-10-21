package com.hruby.vcelnice.ui.stanoviste

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.hruby.vcelnice.R


class StanovisteRecycleViewAdapter(
    private var stanovisteList: List<Stanoviste>,
    private val onEditClick: (Stanoviste, Int) -> Unit, // Lambda pro úpravu
    private val onDeleteClick: (Stanoviste, Int) -> Unit // Lambda pro mazání
) : RecyclerView.Adapter<StanovisteRecycleViewAdapter.StanovisteViewHolder>() {

    class StanovisteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.stanoviste_name)
        val textLastCheck: TextView = itemView.findViewById(R.id.stanoviste_check)
        val textLocationUrl: TextView = itemView.findViewById(R.id.stanoviste_location_url)
        val textLastState: TextView = itemView.findViewById(R.id.stanoviste_state)
        val imageView: ImageView = itemView.findViewById(R.id.stanoviste_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StanovisteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.stanoviste_item, parent, false)

        return StanovisteViewHolder(view)
    }

    override fun onBindViewHolder(holder: StanovisteViewHolder, position: Int) {
        val stanoviste = stanovisteList[position]

        val stringURL: String = stanoviste.locationUrl.toString()
        val arrayGPS: List<String> = stringURL.split("=")

        holder.textName.text = stanoviste.name
        holder.textLastCheck.text = "${stanoviste.lastCheck}"
        //holder.textLocationUrl.text = stanoviste.locationUrl
        var gpsCoordinates = if (arrayGPS.size > 1 && arrayGPS[1].isNotEmpty()) {
            arrayGPS[1] // Pokud je v seznamu více než jeden prvek a není prázdný
        } else {
            "NULL, NULL" // Jinak nastavíme na "NULL, NULL"
        }

        holder.textLocationUrl.text = gpsCoordinates
        holder.textLastState.text = stanoviste.lastState
        holder.imageView

        holder.textLocationUrl.setOnClickListener{
            //val gmmIntentUri = Uri.parse("geo:$lat,$long?q=$lat,$long(Google+Maps)")
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(stanoviste.locationUrl))
            mapIntent.setPackage("com.google.android.apps.maps")
            it.context.startActivity(mapIntent)
        }

        // Přidání popup menu pro úpravy a mazání
        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, stanoviste, position)
            true
        }
    }

    private fun showPopupMenu(view: View, stanoviste: Stanoviste, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_stanoviste)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    onEditClick(stanoviste, position) // Volání pro úpravu
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick(stanoviste, position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int {
        return stanovisteList.size
    }

    private fun deleteStanoviste(stanoviste: Stanoviste) {
        // Logika pro smazání položky
        // Můžete přidat potvrzovací dialog
    }
}