package com.hruby.vcelnice.ui.stanoviste

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.hruby.vcelnice.R
import com.hruby.vcelnice.ui.stanoviste.dialogs.EditDialogFragment


class StanovisteRecycleViewAdapter(private val stanovisteList: List<Stanoviste>) : RecyclerView.Adapter<StanovisteRecycleViewAdapter.StanovisteViewHolder>() {

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
        holder.textLocationUrl.text = arrayGPS[1]
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
            showPopupMenu(holder.itemView, stanoviste, holder, position)
            true
        }
    }

    private fun showPopupMenu(view: View, stanoviste: Stanoviste, holder: StanovisteViewHolder, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_stanoviste)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    editStanoviste(holder, stanoviste, position)
                    true
                }
                R.id.action_delete -> {
                    // logika pro mazání
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun editStanoviste(holder: StanovisteViewHolder, stanoviste: Stanoviste, position: Int) {
        val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
        val dialog = EditDialogFragment()

        // Pass the current data to the dialog
        val bundle = Bundle()

        bundle.putInt("id", position)  // Předání ID
        bundle.putString("name", stanoviste.name)
        bundle.putString("lastCheck", stanoviste.lastCheck)
        bundle.putString("locationUrl", stanoviste.locationUrl)
        dialog.arguments = bundle

        dialog.show(fragmentManager, "EditDialogFragment")
    }

    private fun deleteStanoviste(stanoviste: Stanoviste) {
        // Logika pro smazání položky
        // Můžete přidat potvrzovací dialog
    }

    override fun getItemCount(): Int {
        return stanovisteList.size
    }
}