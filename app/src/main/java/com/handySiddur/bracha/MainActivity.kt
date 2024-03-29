package com.handySiddur.bracha

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import net.sf.hebcal.JewishHolidaysCalendar
import net.sourceforge.zmanim.ZmanimCalendar
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar
import net.sourceforge.zmanim.util.GeoLocation
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : WearableActivity() {
    private lateinit var date: ZmanimCalendar
    private lateinit var wearableRecyclerView: WearableRecyclerView
    private lateinit var jewishDateText: TextView
    private var lastLocation: ZmanLocation? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val brachas: ArrayList<BrachaItem> = ArrayList()
    enum class prayer {
        NONE, SHACHRIS, MINCHA, MARIV
    }
    private var cPrayer = prayer.NONE
    private var bAdapter: BrachaListAdapter? = null

    @Parcelize
    data class ZmanLocation(val lat : Double, val long : Double, val alt: Double) : Parcelable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                AlertDialog.Builder(this).setTitle("Location Permission")
                    .setMessage("We need your location to show you the proper times and Davening according to your current location")
                    .setNegativeButton("No thanks") { _, _ -> }
                    .setPositiveButton("Allow") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            1
                        )
                    }.show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    1)
            }
        }

        // Enables Always-on
        setAmbientEnabled()
        jewishDateText = findViewById(R.id.jewish_date)
        wearableRecyclerView = findViewById(R.id.list_view)
        wearableRecyclerView.apply {
            // To align the edge children (first and last) with the center of the screen
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@MainActivity)
            requestFocus()
        }
        wearableRecyclerView.layoutManager =
            WearableLinearLayoutManager(this, CustomScrollingLayoutCallback())

        val json = getSharedPreferences("userPrefs", Context.MODE_PRIVATE).getString("location", null)
        if (json != null) {
            lastLocation = Gson().fromJson(json, ZmanLocation::class.java)
            if (lastLocation != null) {
                setPrayer(lastLocation!!)
            }
        }
        getLocation()

        setBrachas()
    }

    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val zmanLocation = ZmanLocation(location.latitude, location.longitude, location.altitude)
                        if (lastLocation == null || getCurrentPrayer(zmanLocation) != getCurrentPrayer(lastLocation!!)) {
                            lastLocation = zmanLocation
                            Toast.makeText(this, "Location updated.", Toast.LENGTH_SHORT).show()
                            setPrayer(zmanLocation)
                            setBrachas()
                        }
                        val locationText = Gson().toJson(zmanLocation)
                        val prefs = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("location", locationText).apply()
                    } else {
                        Toast.makeText(this, "Location not determined.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setPrayer(location: ZmanLocation) {
        cPrayer = getCurrentPrayer(location)
    }

    private fun addTefila(date: String) {
        if (cPrayer != prayer.NONE) {
            brachas.add(
                3, BrachaItem(
                    R.drawable.siddur, "Siddur",
                    when (cPrayer) {
                        prayer.SHACHRIS -> getString(R.string.shachris)
                        prayer.MINCHA -> getString(R.string.mincha)
                        else -> getString(R.string.maariv)
                    }
                )
            )
            jewishDateText.visibility = View.VISIBLE
            jewishDateText.text = date
            bAdapter?.notifyDataSetChanged()
        }
    }

    private fun setBrachas() {
        brachas.clear()
        var cal = JewishHolidaysCalendar()
        val today = Calendar.getInstance()
        if (cPrayer == prayer.MARIV) {
            today.add(Calendar.DATE, 1)
        }
        cal = JewishHolidaysCalendar(today, cal.locale)
        val cal2 = JewishCalendar(today)
        val isChannuka = cal2.isChanukah
        val isRoshChodesh = cal2.isRoshChodesh
        var isPesach = false
        var isPurim = false
        var isSukkos = false
        val omer = cal2.dayOfOmer

        val isNissan = cal2.jewishMonth == 1

        for (event in cal.events) {
            when(event.key) {
                "CholHamoedPesach", "Pesach" -> isPesach = true
                "CholHamoedSukkot", "Sukkot" -> isSukkos = true
                "Purim" -> isPurim = true
            }
        }

        val alHanisim = if (isChannuka) getString(R.string.channuka_addition) else if (isPurim) getString(R.string.purim_addition) else ""
        val yaleVayovo = if (isRoshChodesh) getString(R.string.rosh_chodesh_addition_benthcing)
        else if (isPesach) getString(R.string.pesach_addition_benthcing) else if (isSukkos) getString(R.string.sukkos_addition_benthcing) else ""
        val roshChodeshAlHamechia = if (isRoshChodesh) getString(R.string.rosh_chodesh_al_hamechia) else ""


        if (omer > 0) {
            val omerString = getString(R.string.sefira, getString(resources.getIdentifier("_$omer", "string", packageName)))
            brachas.add(BrachaItem(R.drawable.sefiras_haomer, "Sefiras Haomer", omerString))
        }

        if (isNissan) {
            brachas.add(BrachaItem(R.drawable.tree, "B. Ilanos", getString(R.string.b_ilaons)))
        }

        brachas.add(BrachaItem(R.drawable.cookie, "Al Hamechia", getString(R.string.al_hamechia_mezonos, roshChodeshAlHamechia)))
        brachas.add(BrachaItem(R.drawable.bread, "B. Hamazon", getString(R.string.birchas_hamazon, alHanisim, yaleVayovo)))
        brachas.add(BrachaItem(R.drawable.hiking, "T. Haderech", getString(R.string.tefilas_haderech)))
        brachas.add(BrachaItem(R.drawable.bed, "K.S. al Hamita", getString(R.string.krias_shema_mita)))
        brachas.add(BrachaItem(R.drawable.moon, "K. Levana", getString(R.string.k_levana)))
        brachas.add(BrachaItem(R.drawable.lightning,"Lightning", getString(R.string.lightning)))
        brachas.add(BrachaItem(R.drawable.thunder,"Thunder", getString(R.string.thunder)))
        brachas.add(BrachaItem(R.drawable.rainbow,"Rainbow", getString(R.string.rainbow)))
        brachas.add(BrachaItem(R.drawable.toilet, "Asher Yatzar", getString(R.string.asher_yatzar)))
        //brachas.add(BrachaItem(R.drawable.ic_cc_settings_button_bottom, "Settings", getString(R.string.asher_yatzar)))
        addTefila(cal.hebrewDate.toString() + " " + cal.hebrewMonthAsString)

        val obj = object : ItemSelectedListener {
            override fun selected(pos: Int) {
//                if (pos == brachas.size - 1) {
//
//                }
//                else {
                    val intent = Intent(this@MainActivity, BrachaActivity::class.java)
                    intent.putExtra("bracha", brachas[pos])
                    startActivity(intent)
           //     }
            }

        }
        bAdapter = BrachaListAdapter(brachas, this, obj)
        wearableRecyclerView.adapter = bAdapter

        jewishDateText.setOnClickListener {
            val intent = Intent(this@MainActivity, ZmanimActivity::class.java)
            intent.putExtra("location", lastLocation)
            intent.putExtra("daf", cal2.dafYomiBavli.masechtaTransliterated + " " + cal2.dafYomiBavli.daf)
            startActivity(intent)
        }
    }


    private fun getCurrentPrayer(location: ZmanLocation): prayer {
        date = ZmanimCalendar(
            GeoLocation(
                "",
                location.lat,
                location.long,
                location.alt,
                TimeZone.getDefault()
            )
        )
        val cDate = Date()
        if (cDate.before(date.minchaGedola))
            return prayer.SHACHRIS
        else if (cDate.before(date.sunset))
            return  prayer.MINCHA
        else
            return  prayer.MARIV
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            getLocation()
        }
    }
}



interface ItemSelectedListener {
    fun selected(pos: Int)
}
