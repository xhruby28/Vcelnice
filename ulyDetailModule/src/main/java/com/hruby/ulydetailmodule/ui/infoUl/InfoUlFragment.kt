package com.hruby.ulydetailmodule.ui.infoUl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.repository.ZaznamKontrolyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel
import com.hruby.databasemodule.databaseLogic.viewModel.ZaznamKontrolyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.UlyViewModelFactory
import com.hruby.databasemodule.databaseLogic.viewModelFactory.ZaznamKontrolyViewModelFactory
import com.hruby.ulydetailmodule.databinding.FragmentInfoUlBinding
import com.hruby.ulydetailmodule.ui.infoUl.infoUlEdit.InfoUlEditDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InfoUlFragment : Fragment(), InfoUlEditDialog.EditUlEditDialogListener {
    private var stanovisteId: Int = -1
    private var ulId: Int = -1

    private var _binding: FragmentInfoUlBinding? = null
    private val binding get() = _binding!!

    private lateinit var ulyViewModel: UlyViewModel
    private lateinit var zaznamViewModel: ZaznamKontrolyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ulId = requireActivity().intent.getIntExtra("ulId", -1)
        stanovisteId = requireActivity().intent.getIntExtra("stanovisteId", -1)
        Log.d("InfoUlFragment", "stanoviste id: $stanovisteId, Ul ID: $ulId")

        val application = requireActivity().application
        val repository = UlyRepository(StanovisteDatabase.getDatabase(application))
        val factory = UlyViewModelFactory(repository)
        ulyViewModel = ViewModelProvider(this, factory)[UlyViewModel::class.java]

        val zaznamRepository = ZaznamKontrolyRepository(StanovisteDatabase.getDatabase(application))
        val zaznamFactory = ZaznamKontrolyViewModelFactory(zaznamRepository)
        zaznamViewModel = ViewModelProvider(this, zaznamFactory)[ZaznamKontrolyViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoUlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ulyViewModel.getUlWithOthersByStanovisteId(ulId, stanovisteId).observe(viewLifecycleOwner) { ulWithOthers ->
            val ul = ulWithOthers.ul

            // Název úlu
            binding.ulDetailInfoFragmentNazev.text = "Úl č. ${ul.cisloUlu ?: "?"}"

            // Popis
            binding.ulDetailInfoFragmentPopis.text = ul.popis ?: "Bez popisu"

            // MAC adresa
            if (ul.maMAC) {
                binding.ulDetailInfoFragmentMac.text = ul.macAddress ?: "Neuvedeno"
                binding.ulDetailInfoIvInfoMac.visibility = View.VISIBLE
                binding.ulDetailInfoFragmentMac.visibility = View.VISIBLE
                binding.ulDetailInfoFragmentMacText.visibility = View.VISIBLE
            } else {
                binding.ulDetailInfoIvInfoMac.visibility = View.GONE
                binding.ulDetailInfoFragmentMac.visibility = View.GONE
                binding.ulDetailInfoFragmentMacText.visibility = View.GONE
            }

            // Matka, označení, videna naposled
            binding.ulDetailInfoFragmentMatka.text = ul.matkaOznaceni ?: ""
            zaznamViewModel.getLastMatkaVidena(ulId).observe(viewLifecycleOwner) { zaznamMatka ->
                if (zaznamMatka != null) {
                    val formatted = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(Date(zaznamMatka.datum))
                    binding.ulDetailInfoFragmentMatkaPosledniVideni.text = formatted

                    // Zkontrolujeme, zda je potřeba aktualizovat
                    val needsUpdate = (ul.matkaVidena != zaznamMatka.matkaVidena ||
                            ul.matkaVidenaDatum != zaznamMatka.datum)

                    if (needsUpdate) {
                        val updatedUl = ul.copy(
                            matkaVidena = zaznamMatka.matkaVidena,
                            matkaVidenaDatum = zaznamMatka.datum
                        )
                        ulyViewModel.updateUl(updatedUl)
                    }
                }else {
                    binding.ulDetailInfoFragmentMatkaPosledniVideni.text = "-"
                }
            }

            // Problem v úlu z posledního záznamu
            zaznamViewModel.getLastZaznamForUl(ulId).observe(viewLifecycleOwner) { zaznam ->
                if (zaznam != null) {
                    val textLayout = binding.ulDetailInfoFragmentProblemLl
                    val textProblemy = binding.ulDetailInfoFragmentProblem

                    if (zaznam.problemovyUl == true) {
                        val problemy = mutableListOf<String>()

                        if (zaznam.problemMatkaNeni == true) problemy.add("Matka není")
                        if (zaznam.problemMatkaMednik == true) problemy.add("Matka v medníku")
                        if (zaznam.problemMatkaNeklade == true) problemy.add("Matka neklade")
                        if (zaznam.problemMatkaVybehliMatecnik == true) problemy.add("Vyběhlý matečník")
                        if (zaznam.problemMatkaZvapenatelyPlod == true) problemy.add("Zvápenatělý plod")
                        if (zaznam.problemMatkaNosema == true) problemy.add("Nosema")
                        if (zaznam.problemMatkaLoupezOtevrena == true) problemy.add("Loupež otevřená")
                        if (zaznam.problemMatkaLoupezSkryta == true) problemy.add("Loupež skrytá")
                        if (zaznam.problemMatkaTrubcice == true) problemy.add("Trubčice")
                        if (zaznam.problemMatkaJine == true && !zaznam.problemText.isNullOrBlank())
                            problemy.add(zaznam.problemText.toString())

                        textProblemy.text = problemy.joinToString(", ")
                        textLayout.visibility = View.VISIBLE
                        binding.ulDetailInfoIvProblem.visibility = View.VISIBLE
                        binding.ulDetailInfoFragmentProblemText.visibility = View.VISIBLE
                    } else {
                        // Skryj, pokud žádný problém
                        textLayout.visibility = View.GONE
                        binding.ulDetailInfoIvProblem.visibility = View.GONE
                        binding.ulDetailInfoFragmentProblemText.visibility = View.GONE
                    }

                    if (ul.problemovyUl != zaznam.problemovyUl || ul.posledniProblem != textProblemy.text.toString()) {
                        val updatedUl = ul.copy(
                            problemovyUl = zaznam.problemovyUl == true,
                            posledniProblem = textProblemy.text.toString()
                        )
                        ulyViewModel.updateUl(updatedUl)
                    }

                    val newKontrolaDate = zaznam.datum
                    val oldKontrolaDate = ul.lastKontrolaDate

                    if (oldKontrolaDate == null || oldKontrolaDate < newKontrolaDate) {
                        val updateUl = ul.copy(
                            lastKontrola = newKontrolaDate.toString(),
                            lastKontrolaDate = newKontrolaDate
                        )
                        ulyViewModel.updateUl(updateUl)
                    }
                }
            }

            // Hodnocení
            /*val ratingLayout = binding.root.findViewById<LinearLayout>(R.id.rating_layout)
            val showRatings = ul.hodnoceni > 0f || ul.agresivita > 0 || ul.mezolitostPlodu > 0 || ul.silaVcelstva > 0 || ul.stavebniPud > 0 || !ul.stavZasob.isNullOrEmpty()

            if (showRatings) {
                ratingLayout.visibility = View.VISIBLE
                binding.ratingHodnoceni.rating = ul.hodnoceni
                binding.ratingAgresivita.rating = ul.agresivita.toFloat()
                binding.ratingMezolitostPlodu.rating = ul.mezolitostPlodu.toFloat()
                binding.ratingSilaVcelstva.rating = ul.silaVcelstva.toFloat()
                binding.ratingStavebniPud.rating = ul.stavebniPud.toFloat()
                binding.textStavZasob.text = "Stav zásob: ${ul.stavZasob ?: "neuvedeno"}"
            } else {
                ratingLayout.visibility = View.GONE
            }*/

            binding.ulDetailInfoFragmentFab.setOnClickListener {
                    val dialog = InfoUlEditDialog()
                    val bundle = Bundle()
                    bundle.putSerializable("ul", ul)
                    dialog.arguments = bundle
                    dialog.show(childFragmentManager, "InfoUlEditDialog")
            }
        }
    }

    override fun onUlUpdate(updatedStanoviste: Uly) {
        ulyViewModel.updateUl(updatedStanoviste)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}