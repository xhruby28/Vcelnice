package com.hruby.vcelnice.ui.parovani

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.hruby.vcelnice.databinding.FragmentParovaniBinding

class ParovaniFragment : Fragment() {

    private var _binding: FragmentParovaniBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parovaniViewModel =
            ViewModelProvider(this).get(ParovaniViewModel::class.java)

        _binding = FragmentParovaniBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textParovani
        parovaniViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}