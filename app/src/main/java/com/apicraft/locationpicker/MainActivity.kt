package com.apicraft.locationpicker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.clans.fab.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
	private var gpsLocationLayout: LinearLayout? = null
	private var gpsLocationLongValue: TextView? = null
	private var gpsLocationLatValue: TextView? = null
	private var networkLocationLayout: LinearLayout? = null
	private var networkLocationLongValue: TextView? = null
	private var networkLocationLatValue: TextView? = null
	private var bestLocationLayout: LinearLayout? = null
	private var bestLocationLongValue: TextView? = null
	private var bestLocationLatValue: TextView? = null
	private var locationPickerButton: FloatingActionButton? = null
	private var clearLocationButton: FloatingActionButton? = null
	private var locationManager: LocationManager? = null
	private var longitudeBest: Double = 0.toDouble()
	private var latitudeBest: Double = 0.toDouble()
	private var longitudeGPS: Double = 0.toDouble()
	private var latitudeGPS: Double = 0.toDouble()
	private var longitudeNetwork: Double = 0.toDouble()
	private var latitudeNetwork: Double = 0.toDouble()
	private var TAG = "Main Activity"
	private var RECORD_REQUEST_CODE = 123
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(toolbar)
		setUpUI()
		locationPickerButton?.setOnClickListener {
			checkPermission()
		}
		clearLocationButton?.setOnClickListener{
			resetCoordinates()
		}
		locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		getLocations()
	}
	
	private fun getLocations() {
		if (!isLocationEnabled()) {
			showAlert()
		}
	}
	
	private fun resetCoordinates(){
		locationManager?.removeUpdates(locationListenerGPS)
		locationManager?.removeUpdates(locationListenerBest)
		locationManager?.removeUpdates(locationListenerNetwork)
		
		gpsLocationLongValue?.text = resources.getString(R.string.zero)
		gpsLocationLatValue?.text = resources.getString(R.string.zero)
		
		networkLocationLongValue?.text = resources.getString(R.string.zero)
		networkLocationLatValue?.text = resources.getString(R.string.zero)
		
		bestLocationLongValue?.text = resources.getString(R.string.zero)
		bestLocationLatValue?.text = resources.getString(R.string.zero)
	}
	
	private fun checkPermission() {
		val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
		val locationCoarsePermission =
			ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
		val internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
		
		if (locationPermission != PackageManager.PERMISSION_GRANTED || locationCoarsePermission != PackageManager.PERMISSION_GRANTED || internetPermission != PackageManager.PERMISSION_GRANTED) {
			Log.i(TAG, "Permission to Access Location or Internet Denied")
			if (ActivityCompat.shouldShowRequestPermissionRationale(
					this,
					Manifest.permission.ACCESS_FINE_LOCATION
				) || ActivityCompat.shouldShowRequestPermissionRationale(
					this,
					Manifest.permission.ACCESS_COARSE_LOCATION
				) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)
			) {
				val builder = AlertDialog.Builder(this)
				builder.setMessage(
					"Permission to access location and Internet is required for this app to pick GPS coordinates"
				).setTitle("Permission required")
				builder.setPositiveButton("OK") { dialog, id ->
					Log.i(TAG, "Clicked")
					makeRequest()
				}
				val dialog = builder.create()
				dialog.show()
			} else {
				makeRequest()
			}
		} else{
			toggleBestUpdates()
			toggleGPSUpdates()
			toggleNetworkUpdates()
		}
	}
	
	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<String>, grantResults: IntArray
	) {
		when (requestCode) {
			RECORD_REQUEST_CODE -> {
				
				if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					Log.i(TAG, "Permission has been denied by user")
					showAlert()
				} else {
					Log.i(TAG, "Permission has been granted by user")
					toggleBestUpdates()
					toggleGPSUpdates()
					toggleNetworkUpdates()
				}
			}
		}
	}
	
	private fun makeRequest() {
		ActivityCompat.requestPermissions(
			this,
			arrayOf(
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.INTERNET
			), RECORD_REQUEST_CODE
		)
	}
	
	private fun showAlert() {
		val dialog = AlertDialog.Builder(this)
		dialog.setTitle("Enable Location")
			.setMessage("Your Locations Settings is off.\n Please Enable Location to " + "use this app")
			.setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
				val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
				startActivity(myIntent)
			}
			.setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
		dialog.show()
	}
	
	private fun setUpUI() {
		gpsLocationLayout = findViewById(R.id.gps_location_layout)
		networkLocationLayout = findViewById(R.id.network_location_layout)
		bestLocationLayout = findViewById(R.id.best_location_layout)
		locationPickerButton = findViewById(R.id.location_picker)
		clearLocationButton = findViewById(R.id.clear_location)
		
		gpsLocationLongValue = findViewById(R.id.gps_location_long_value)
		gpsLocationLatValue = findViewById(R.id.gps_location_lat_value)
		
		networkLocationLongValue = findViewById(R.id.network_location_long_value)
		networkLocationLatValue = findViewById(R.id.network_location_lat_value)
		
		bestLocationLongValue = findViewById(R.id.best_location_long_value)
		bestLocationLatValue = findViewById(R.id.best_location_lat_value)
	}
	
	
	private fun isLocationEnabled(): Boolean {
		var enabled = false
		if (locationManager != null) {
			enabled =
				locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
					LocationManager.NETWORK_PROVIDER
				)
		}
		return enabled
	}
	
	private val locationListenerBest = object : LocationListener {
		override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
		}
		
		override fun onProviderEnabled(provider: String?) {
		}
		
		override fun onProviderDisabled(provider: String?) {
		}
		
		override fun onLocationChanged(location: Location) {
			longitudeBest = location.longitude
			latitudeBest = location.latitude
			
			runOnUiThread {
				bestLocationLongValue?.text = longitudeBest.toString()
				bestLocationLatValue?.text = latitudeBest.toString()
			}
		}
	}
	
	private val locationListenerNetwork = object : LocationListener {
		override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
		}
		
		override fun onProviderEnabled(provider: String?) {
		}
		
		override fun onProviderDisabled(provider: String?) {
		}
		
		override fun onLocationChanged(location: Location) {
			longitudeNetwork = location.longitude
			latitudeNetwork = location.latitude
			
			runOnUiThread {
				networkLocationLongValue?.text = longitudeNetwork.toString()
				networkLocationLatValue?.text = latitudeNetwork.toString()
			}
		}
		
		
	}
	
	private val locationListenerGPS = object : LocationListener {
		override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
		}
		
		override fun onProviderEnabled(provider: String?) {
		}
		
		override fun onProviderDisabled(provider: String?) {
		}
		
		override fun onLocationChanged(location: Location) {
			longitudeGPS = location.longitude
			latitudeGPS = location.latitude
			
			runOnUiThread {
				gpsLocationLongValue?.text = longitudeGPS.toString()
				gpsLocationLatValue?.text = latitudeGPS.toString()
			}
		}
	}
	
	@SuppressLint("MissingPermission")
	private fun toggleGPSUpdates() {
		if (isLocationEnabled()) {
			locationManager?.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, (3000).toLong(), (2).toFloat(), locationListenerGPS
			)
		}
	}
	
	@SuppressLint("MissingPermission")
	private fun toggleBestUpdates() {
		if (isLocationEnabled()) {
			val criteria = Criteria()
			criteria.accuracy = Criteria.ACCURACY_FINE
			criteria.isAltitudeRequired = true
			criteria.isBearingRequired = true
			criteria.isCostAllowed = true
			criteria.powerRequirement = Criteria.POWER_LOW
			val provider = locationManager?.getBestProvider(criteria, true)
			if (provider != null) {
				locationManager?.requestLocationUpdates(
					provider,
					(3000).toLong(),
					(2).toFloat(),
					locationListenerBest
				)
			}
		}
	}
	
	@SuppressLint("MissingPermission")
	private fun toggleNetworkUpdates() {
		if (!isLocationEnabled()) {
			locationManager?.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, (3000).toLong(), (2).toFloat(), locationListenerNetwork
			)
		}
	}
}
