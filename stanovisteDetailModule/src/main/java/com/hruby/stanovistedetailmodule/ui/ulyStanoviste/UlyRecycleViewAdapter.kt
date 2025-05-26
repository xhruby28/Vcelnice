package com.hruby.stanovistedetailmodule.ui.ulyStanoviste

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.databasemodule.databaseLogic.viewModel.ZaznamKontrolyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.UlyViewModelFactory
import com.hruby.stanovistedetailmodule.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UlyRecycleViewAdapter (
    private var ulList: List<Uly>,
    private val onEditClick: (Uly, Int) -> Unit, // Lambda pro úpravu
    private val onDeleteClick: (Uly, Int) -> Unit, // Lambda pro mazání
    private val onItemClick: (Int) -> Unit
): RecyclerView.Adapter<UlyRecycleViewAdapter.UlyViewHolder>(){
    class UlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ulId: TextView = itemView.findViewById(R.id.ul_id_ids)
        val haveMacAddress: ImageView = itemView.findViewById(R.id.ul_sync_icon)

        val posledniKontrolaIcon: ImageView = itemView.findViewById(R.id.iv_uly_kontrola)
        val posledniKontrola: TextView = itemView.findViewById(R.id.ul_kontrola)
        val posledniKontrolaText: TextView = itemView.findViewById(R.id.ul_kontrola_text)

        val kralovnaIcon: ImageView = itemView.findViewById(R.id.iv_uly_queen)
        val kralovna: TextView = itemView.findViewById(R.id.ul_kralovna)
        val kralovnaText: TextView = itemView.findViewById(R.id.ul_kralovna_text)

        val latestProblemIcon: ImageView = itemView.findViewById(R.id.iv_uly_problem)
        val latestProblemText: TextView = itemView.findViewById(R.id.ul_problem_text)
        val latestProblem: TextView = itemView.findViewById(R.id.ul_problem)
        //val ulRating: RatingBar = itemView.findViewById(R.id.ul_hodnoceni_stars)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.uly_item, parent, false)

        return UlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: UlyViewHolder, position: Int) {
        val uly = ulList[position]

        holder.ulId.text = String.format(uly.cisloUlu.toString())

        //holder.ulRating.rating = uly.hodnoceni

        if (!uly.lastKontrola.isNullOrEmpty()){
            holder.posledniKontrolaIcon.visibility = View.VISIBLE
            holder.posledniKontrola.visibility = View.VISIBLE
            holder.posledniKontrolaText.visibility = View.VISIBLE

            val formatted = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(uly.lastKontrola!!.toLong()))
            holder.posledniKontrolaText.text = formatted.toString()
        } else {
            holder.posledniKontrolaIcon.visibility = View.GONE
            holder.posledniKontrola.visibility = View.GONE
            holder.posledniKontrolaText.visibility = View.GONE
        }

        if (!uly.matkaOznaceni.isNullOrEmpty()){
            holder.kralovnaIcon.visibility = View.VISIBLE
            holder.kralovna.visibility = View.VISIBLE
            holder.kralovnaText.visibility = View.VISIBLE

            holder.kralovnaText.text = uly.matkaOznaceni
        } else {
            holder.kralovnaIcon.visibility = View.GONE
            holder.kralovna.visibility = View.GONE
            holder.kralovnaText.visibility = View.GONE
        }

        if(uly.problemovyUl == false){
            holder.latestProblemIcon.visibility = View.GONE
            holder.latestProblem.visibility = View.GONE
            holder.latestProblemText.visibility = View.GONE
        } else {
            holder.latestProblem.text = uly.posledniProblem

            holder.latestProblemIcon.visibility = View.VISIBLE
            holder.latestProblem.visibility = View.VISIBLE
            holder.latestProblemText.visibility = View.VISIBLE
        }

        if(uly.maMAC){
            holder.haveMacAddress.visibility = View.VISIBLE
        } else {
            holder.haveMacAddress.visibility = View.GONE
        }

        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, uly, position)
            true
        }

        holder.itemView.setOnClickListener{
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(uly.id) // Předáme ID do lambda funkce
            }
        }
    }

    private fun showPopupMenu(view: View, uly: Uly, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_uly_stanoviste)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                /*R.id.uly_action_edit -> {
                    onEditClick(uly, position) // Volání pro úpravu
                    true
                }*/
                R.id.uly_action_delete -> {
                    onDeleteClick(uly, position)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount(): Int {
        return ulList.size
    }
}