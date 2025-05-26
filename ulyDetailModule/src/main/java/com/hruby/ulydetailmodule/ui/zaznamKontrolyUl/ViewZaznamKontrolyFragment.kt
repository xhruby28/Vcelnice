package com.hruby.ulydetailmodule.ui.zaznamKontrolyUl

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.ZaznamKontrolyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.ZaznamKontrolyViewModel
import com.hruby.databasemodule.databaseLogic.viewModelFactory.ZaznamKontrolyViewModelFactory
import com.hruby.ulydetailmodule.R
import com.hruby.ulydetailmodule.databinding.FragmentViewZaznamKontrolyBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewZaznamKontrolyFragment : Fragment() {

    private var _binding: FragmentViewZaznamKontrolyBinding? = null
    private val binding get() = _binding!!

    private lateinit var zaznam: ZaznamKontroly

    private lateinit var viewModel: ZaznamKontrolyViewModel

    private var zaznamId: Int = -1
    private var ulId: Int = -1


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewZaznamKontrolyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_zaznam_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                val action = ViewZaznamKontrolyFragmentDirections
                    .actionNavZaznamUlPreviewToNavZaznamUlEdit(ulId = zaznam.ulId, zaznamId = zaznam.id)
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        zaznamId = arguments?.getInt("zaznamId") ?: -1
        ulId = arguments?.getInt("ulId") ?: -1

        val db = StanovisteDatabase.getDatabase(requireContext().applicationContext)
        val repository = ZaznamKontrolyRepository(db)
        val factory = ZaznamKontrolyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ZaznamKontrolyViewModel::class.java]

        viewModel.getZaznamByIdAndUlId(zaznamId, ulId).observe(viewLifecycleOwner) { aktualizovanyZaznam ->
            if (aktualizovanyZaznam != null) {
                zaznam = aktualizovanyZaznam
                zobrazZaznam()
            }
        }
    }

    private fun zobrazZaznam() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.viewViewAddUlKontrolaDatePicker.text = dateFormat.format(Date(zaznam.datum))
        binding.viewEditTextKontrolaPopisKontroly.text = zaznam.zaznamText ?: ""
        binding.viewTextswitchKontrolaProblemovyUl.text = if (zaznam.problemovyUl) "Ano" else "Ne"
        binding.viewTextswitchZaznamMatkaVidena.text = if (zaznam.matkaVidena) "Ano" else "Ne"

        // Zásahy
        binding.viewCheckMatkaOznacena.isChecked = zaznam.zasahMatkaOznacena
        binding.viewCheckKrmeni.isChecked = zaznam.zasahKrmeni
        binding.viewCheckPripravaKrmeni.isChecked = zaznam.zasahPripravaNaKrmeni
        binding.viewCheckMrizkaVlozena.isChecked = zaznam.zasahVlozenaMrizka
        binding.viewCheckMrizkaOdebrana.isChecked = zaznam.zasahOdebranaMrizka
        binding.viewCheckMedobrani.isChecked = zaznam.zasahMedobrani
        binding.viewCheckZrusenoSpojenim.isChecked = zaznam.zasahZrusenoSpojenim
        binding.viewCheckCisteniDno.isChecked = zaznam.zasahVycisteneDno
        binding.viewCheckOskrAbaneSteny.isChecked = zaznam.zasahOskrabaneSteny

        // Nástavek
        binding.viewAddUlKontrolaEditTextMedobraniRamky.text = zaznam.nastavekAkce ?: ""

        // Mezistěny a souše
        binding.viewMezistenyPlodisteSpinner.text = zaznam.mezistenyPlodiste.toString()
        binding.viewSousePlodisteSpinner.text = zaznam.souskyPlodiste.toString()
        binding.viewMezistenyMednikSpinner.text = zaznam.mezistenyMednik.toString()
        binding.viewSouseMednikSpinner.text = zaznam.souskyMednik.toString()

        // Plod
        binding.viewPlodZavrenySwitchText.text = if (zaznam.plodZavreny) "Ano" else "Ne"
        binding.viewPlodOtevrenySwitchText.text = if (zaznam.plodOtevreny) "Ano" else "Ne"
        binding.viewPlodMednikSwitchText.text = if (zaznam.plodVMedniku) "Ano" else "Ne"
        binding.viewPlodTrubcinaSwitchText.text = if (zaznam.plodTrubcina) "Ano" else "Ne"
        binding.viewPlodMatecnikSwitchText.text = if (zaznam.plodMatecnik) "Ano" else "Ne"

        // 🐝 Problémový úl
        binding.viewEditTextKontrolaPopisProblemu.visibility =
            if (zaznam.problemovyUl) View.VISIBLE else View.GONE

        if (zaznam.problemovyUl) {
            binding.viewEditTextKontrolaMatkaNeni.setSafeChecked(zaznam.problemMatkaNeni)
            binding.viewEditTextKontrolaMatkaMednik.setSafeChecked(zaznam.problemMatkaMednik)
            binding.viewEditTextKontrolaMatkaNeklade.setSafeChecked(zaznam.problemMatkaNeklade)
            binding.viewEditTextKontrolaVybehlyMatecnik.setSafeChecked(zaznam.problemMatkaVybehliMatecnik)
            binding.viewEditTextKontrolaZvapenatelyPlod.setSafeChecked(zaznam.problemMatkaZvapenatelyPlod)
            binding.viewEditTextKontrolaNosema.setSafeChecked(zaznam.problemMatkaNosema)
            binding.viewEditTextKontrolaLoupezOtevrena.setSafeChecked(zaznam.problemMatkaLoupezOtevrena)
            binding.viewEditTextKontrolaLoupezSkryta.setSafeChecked(zaznam.problemMatkaLoupezSkryta)
            binding.viewEditTextKontrolaTrubcice.setSafeChecked(zaznam.problemMatkaTrubcice)
            binding.viewEditTextKontrolaJine.setSafeChecked(zaznam.problemMatkaJine)
            binding.viewEditTextKontrolaJineText.text =
                zaznam.problemText?.let { SpannableStringBuilder(it) } ?: SpannableStringBuilder("")
        } else {
            // Vynulování zobrazených checkboxů i textu pro jistotu
            listOf(
                binding.viewEditTextKontrolaMatkaNeni,
                binding.viewEditTextKontrolaMatkaMednik,
                binding.viewEditTextKontrolaMatkaNeklade,
                binding.viewEditTextKontrolaVybehlyMatecnik,
                binding.viewEditTextKontrolaZvapenatelyPlod,
                binding.viewEditTextKontrolaNosema,
                binding.viewEditTextKontrolaLoupezOtevrena,
                binding.viewEditTextKontrolaLoupezSkryta,
                binding.viewEditTextKontrolaTrubcice,
                binding.viewEditTextKontrolaJine
            ).forEach { it.isChecked = false }

            binding.viewEditTextKontrolaJineText.text = SpannableStringBuilder("")
        }
    }

    private fun CheckBox.setSafeChecked(value: Boolean?) {
        this.isChecked = value == true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(zaznam: ZaznamKontroly): ViewZaznamKontrolyFragment {
            val fragment = ViewZaznamKontrolyFragment()
            val args = Bundle()
            args.putSerializable("zaznam", zaznam)
            fragment.arguments = args
            return fragment
        }
    }
}