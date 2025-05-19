package com.hruby.stanovistedetailmodule.ui.ulyStanoviste

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.hruby.databasemodule.data.Uly
import com.hruby.stanovistedetailmodule.R


class UlyRecycleViewAdapter (
    private var ulList: List<Uly>,
    private val onEditClick: (Uly, Int) -> Unit, // Lambda pro úpravu
    private val onDeleteClick: (Uly, Int) -> Unit, // Lambda pro mazání
    private val onItemClick: (Int) -> Unit
): RecyclerView.Adapter<UlyRecycleViewAdapter.UlyViewHolder>(){

    class UlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ulId: TextView = itemView.findViewById(R.id.ul_id_ids)
        val latestProblem: TextView = itemView.findViewById(R.id.ul_problem)
        val ulRating: RatingBar = itemView.findViewById(R.id.ul_hodnoceni_stars)
        val latestProblemText: TextView = itemView.findViewById(R.id.ul_problem_text)
        val haveMacAddress: ImageView = itemView.findViewById(R.id.ul_sync_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.uly_item, parent, false)

        return UlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: UlyViewHolder, position: Int) {
        val uly = ulList[position]

        holder.ulId.text = String.format(uly.cisloUlu.toString())
//        TODO("Upravit hodnocení tak, aby to tvořilo průměr ze všech hodnot")
        holder.ulRating.rating = uly.hodnoceni
//        TODO("Změnit zobrazování posledního problému na problemovyUl true/false, " +
//                "když false, tak se neobjeví v seznamu informace o problému")
        if(uly.problemovyUl == false){
            holder.latestProblem.visibility = View.GONE
            holder.latestProblemText.visibility = View.GONE
        } else {
            holder.latestProblem.text = uly.posledniProblem

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