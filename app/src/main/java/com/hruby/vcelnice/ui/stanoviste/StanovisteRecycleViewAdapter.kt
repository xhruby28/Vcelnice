package com.hruby.vcelnice.ui.stanoviste

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.vcelnice.R
import com.squareup.picasso.Picasso
import java.io.File


class StanovisteRecycleViewAdapter(
    private var stanovisteList: List<Stanoviste>,
    private val onEditClick: (Stanoviste, Int) -> Unit, // Lambda pro úpravu
    private val onDeleteClick: (Stanoviste, Int) -> Unit, // Lambda pro mazání
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<StanovisteRecycleViewAdapter.StanovisteViewHolder>() {

    class StanovisteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.stanoviste_name)
        val textLastCheck: TextView = itemView.findViewById(R.id.stanoviste_check)
        val textLocationUrl: TextView = itemView.findViewById(R.id.stanoviste_location_url)
        val textLastState: TextView = itemView.findViewById(R.id.stanoviste_state)
        val imageView: ImageView = itemView.findViewById(R.id.stanoviste_image)
        val stanovisteHaveMac: ImageView = itemView.findViewById(R.id.stanoviste_sync_icon)
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
        loadImage(holder.itemView.context, stanoviste.imagePath, holder.imageView)

        if (stanoviste.maMAC){
            holder.stanovisteHaveMac.visibility = View.VISIBLE
        } else {
            holder.stanovisteHaveMac.visibility = View.GONE
        }

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
        holder.itemView.setOnClickListener{
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(stanoviste.id) // Předáme ID do lambda funkce
            }
        }


    }

    private fun loadImage(context: Context, imagePath: String?, view: ImageView) {
        imagePath?.let { path ->
            val file = File(context.filesDir, path)

            if (file.exists()) {
                Log.d("InfoStanovisteFragment", "Loading image from: $path")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                Picasso.get()
                    .load(uri)
                    .placeholder(com.hruby.sharedresources.R.drawable.ic_launcher_background)
                    .into(view)
            } else {
                Log.e("InfoStanovisteFragment", "File does not exist at: $path")
            }
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

    fun setStanoviste(stanoviste: List<Stanoviste>) {
        this.stanovisteList = stanoviste
        notifyDataSetChanged()
    }
}