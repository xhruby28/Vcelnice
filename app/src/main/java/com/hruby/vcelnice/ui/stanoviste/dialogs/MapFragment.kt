package com.hruby.vcelnice.ui.stanoviste.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hruby.vcelnice.R

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private var listener: OnLocationSelectedListener? = null

    interface OnLocationSelectedListener {
        fun onLocationSelected(latLng: LatLng)
    }

    fun setOnLocationSelectedListener(listener: OnLocationSelectedListener) {
        this.listener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Tuto část můžeš vynechat, protože listener nyní nastavíš přímo
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapView = view.findViewById<MapView>(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener { latLng ->
            // Přidání markeru na vybrané souřadnice
            map.clear() // Vyčisti předchozí markery
            map.addMarker(MarkerOptions().position(latLng))
            listener?.onLocationSelected(latLng) // Volej metodu pro předání souřadnic
            parentFragmentManager.popBackStack() // Zavři mapu
        }
    }

    // Nezapomeň na metody lifecycle pro MapView
    override fun onResume() {
        super.onResume()
        view?.findViewById<MapView>(R.id.map)?.onResume()
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<MapView>(R.id.map)?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        view?.findViewById<MapView>(R.id.map)?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        view?.findViewById<MapView>(R.id.map)?.onLowMemory()
    }
}