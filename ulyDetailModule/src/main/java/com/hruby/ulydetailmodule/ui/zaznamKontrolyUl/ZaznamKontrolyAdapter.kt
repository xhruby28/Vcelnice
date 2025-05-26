package com.hruby.ulydetailmodule.ui.zaznamKontrolyUl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.ulydetailmodule.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ZaznamKontrolyAdapter(
    private val zaznamy: List<ZaznamKontroly>,
    private val onEditClick: (ZaznamKontroly, Int) -> Unit, // Lambda pro úpravu
    private val onDeleteClick: (ZaznamKontroly, Int) -> Unit, // Lambda pro mazání
    private val onItemClick: (ZaznamKontroly, Int) -> Unit
) : RecyclerView.Adapter<ZaznamKontrolyAdapter.ZaznamViewHolder>() {

    inner class ZaznamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDatum: TextView = itemView.findViewById(R.id.tvDatumKontroly)
        val tvMatkaVidena: TextView = itemView.findViewById(R.id.ivMatkaVidena)
        val tvPopisZasahu: TextView = itemView.findViewById(R.id.tvPopisZasahu)

        val ivProblem: ImageView = itemView.findViewById(R.id.ivProblemovyUl)
        val ivZasahMatkaOznacena: View = itemView.findViewById(R.id.ivZasahMatkaOznacena)
        val ivZasahKrmeni: ImageView = itemView.findViewById(R.id.ivZasahKrmeniNeboPriprava)
        val ivZasahMrizkaVlozena: ImageView = itemView.findViewById(R.id.ivZasahMrizkaVlozena)
        val ivZasahMrizkaOdebrana: View = itemView.findViewById(R.id.ivZasahMrizkaOdebrana)
        val ivZasahZruseno: View = itemView.findViewById(R.id.ivZasahZruseno)
        val tvProblemy: TextView = itemView.findViewById(R.id.tvProblemyVypisText)
        val loProblem: LinearLayout = itemView.findViewById<LinearLayout>(R.id.loProblemyTextLayout)
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
        holder.tvMatkaVidena.text = if (zaznam.matkaVidena) "Ano" else "Ne"

        val zasahy = mutableListOf<String>()
        val problemy = mutableListOf<String>()

        holder.ivProblem.visibility = if (zaznam.problemovyUl) View.VISIBLE else View.GONE
        holder.ivZasahMatkaOznacena.visibility = if (zaznam.zasahMatkaOznacena) View.VISIBLE else View.GONE
        if (zaznam.zasahMatkaOznacena) zasahy.add("Matka označena")

        holder.ivZasahKrmeni.visibility = if (zaznam.zasahKrmeni || zaznam.zasahPripravaNaKrmeni) View.VISIBLE else View.GONE
        if (zaznam.zasahKrmeni) zasahy.add("Krmení")
        if (zaznam.zasahPripravaNaKrmeni) zasahy.add("Příprava na krmení")

        holder.ivZasahMrizkaVlozena.visibility = if (zaznam.zasahVlozenaMrizka) View.VISIBLE else View.GONE
        if (zaznam.zasahVlozenaMrizka) zasahy.add("Vložena mřížka")

        holder.ivZasahMrizkaOdebrana.visibility = if (zaznam.zasahOdebranaMrizka) View.VISIBLE else View.GONE
        if (zaznam.zasahOdebranaMrizka) zasahy.add("Odebrána mřížka")

        holder.ivZasahZruseno.visibility = if (zaznam.zasahZrusenoSpojenim) View.VISIBLE else View.GONE
        if (zaznam.zasahZrusenoSpojenim) zasahy.add("Zrušeno spojením")

        holder.tvPopisZasahu.text = zasahy.joinToString(", ")

        if(zaznam.problemovyUl){
            holder.loProblem.visibility = View.VISIBLE

            if (zaznam.problemMatkaNeni == true) problemy.add("Matka není")
            if (zaznam.problemMatkaMednik == true) problemy.add("Matka v medníku")
            if (zaznam.problemMatkaNeklade == true) problemy.add("Matka neklade")
            if (zaznam.problemMatkaVybehliMatecnik == true) problemy.add("Vyběhlí matečník")
            if (zaznam.problemMatkaZvapenatelyPlod == true) problemy.add("Zvápenatění plodu")
            if (zaznam.problemMatkaNosema == true) problemy.add("Nosema")
            if (zaznam.problemMatkaLoupezOtevrena == true) problemy.add("Loupež otevřená")
            if (zaznam.problemMatkaLoupezSkryta == true) problemy.add("Loupež skrytá")
            if (zaznam.problemMatkaTrubcice == true) problemy.add("Trubčice")
            if (zaznam.problemMatkaJine == true) problemy.add(zaznam.problemText.toString())

            holder.tvProblemy.text = problemy.joinToString(", ")
        } else {
            holder.loProblem.visibility = View.GONE
        }

        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, zaznam, position)
            true
        }

        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                onItemClick(zaznam, position)
            }
        }
    }

    private fun showPopupMenu(view: View, zaznam: ZaznamKontroly, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.menu_zaznam_kontroly)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_zaznam_kontroly_upravit -> {
                    onEditClick(zaznam, position)
                    true
                }
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