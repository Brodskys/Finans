package com.example.finans.map

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.Manifest
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.Location
import android.preference.PreferenceManager
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.*


class BottomSheetMapFragment : BottomSheetDialogFragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    private lateinit var lastLocal: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var marker: Marker
    private lateinit var markerAddress: GeoPoint

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mapViewModel: MapViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.setCancelable(false)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_map, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_map, container, false)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)


        view.findViewById<TextView>(R.id.mapExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.mapSave).setOnClickListener {

            mapViewModel.selectMap(markerAddress)
            dismiss()

        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        searchView = view.findViewById(R.id.mapSearch)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                try{

                    val geocoder = Geocoder(requireContext())
                    val addressList = geocoder.getFromLocationName(query!!, 1)

                    if (addressList!!.isNotEmpty()) {
                        val address = addressList[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        mMap.clear()

                        placeMarkerOnMap(latLng)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    }
                }
                catch(e: Exception) {
                    Log.e("Maps", e.message!!)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

        setUpMap()

    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()){location ->

            if(location!=null){
                lastLocal = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,12f))
            }

        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {

        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(currentLatLong.latitude, currentLatLong.longitude, 1)
            val address = addresses?.get(0)?.getAddressLine(0)
            val markerOptions = MarkerOptions().position(currentLatLong)
            markerOptions.title(address)

            markerAddress = GeoPoint(currentLatLong.latitude, currentLatLong.longitude)

            marker = mMap.addMarker(markerOptions)!!
            marker.showInfoWindow()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }


    }


    override fun onMarkerClick(p0: Marker): Boolean = false

    override fun onMapClick(latLng: LatLng) {
        mMap.clear()

        placeMarkerOnMap(latLng)
    }
    companion object{
        const val LOCATION_REQUEST_CODE = 1
    }


}