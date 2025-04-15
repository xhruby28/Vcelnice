package com.hruby.stanovistedetailmodule.ui.infoStanoviste.editDialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hruby.sharedresources.dialogs.MapTouchWrapper
import com.hruby.stanovistedetailmodule.R

class StanovisteEditMapFragment : Fragment(), OnMapReadyCallback {
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
        return inflater.inflate(R.layout.fragment_stanoviste_edit_map, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapView = view.findViewById<MapView>(R.id.stanoviste_edit_map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val mapTouchWrapper = view.findViewById<MapTouchWrapper>(R.id.map_touch_wrapper)

        mapTouchWrapper.mapTouchListener = {
            view.parent?.requestDisallowInterceptTouchEvent(true)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Souřadnice pro Českou republiku
        val czechRepublicLatLng = LatLng(49.8175, 15.4730) // Střed České republiky
        val zoomLevel = 6.0f // Přizpůsobte si úroveň zoomu podle potřeby

        // Nastavení kamery na Českou republiku
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(czechRepublicLatLng, zoomLevel))

        map.setOnMapLongClickListener { latLng ->
            // Přidání markeru na vybrané souřadnice
            map.clear() // Vyčisti předchozí markery
            map.addMarker(MarkerOptions().position(latLng))
            listener?.onLocationSelected(latLng) // Volej metodu pro předání souřadnic
            //parentFragmentManager.popBackStack() // Zavři mapu
        }
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<MapView>(R.id.stanoviste_edit_map)?.onResume()
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<MapView>(R.id.stanoviste_edit_map)?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        view?.findViewById<MapView>(R.id.stanoviste_edit_map)?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        view?.findViewById<MapView>(R.id.stanoviste_edit_map)?.onLowMemory()
    }
}