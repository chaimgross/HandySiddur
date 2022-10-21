package com.handySiddur.bracha;

import android.location.Location
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import net.sourceforge.zmanim.ZmanimCalendar
import net.sourceforge.zmanim.util.GeoLocation
import java.util.*
import kotlin.collections.ArrayList

class ZmanimActivity : WearableActivity() {

    private lateinit var wearableRecyclerView: WearableRecyclerView
    private var zAdapter: ZmanimAdapter? = null
    private val zmanim: ArrayList<Pair<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zman_activity)

        wearableRecyclerView = findViewById<WearableRecyclerView>(R.id.list_view)
        wearableRecyclerView.apply {
            // To align the edge children (first and last) with the center of the screen
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@ZmanimActivity)
            requestFocus()
        }
        wearableRecyclerView.layoutManager =
            WearableLinearLayoutManager(this, CustomScrollingLayoutCallback())

        intent.getParcelableExtra<MainActivity.ZmanLocation>("location")?.let { lastLocation ->
            val daf = intent.getStringExtra("daf") ?: ""
            val date = ZmanimCalendar(
                GeoLocation(
                    "",
                    lastLocation.lat,
                    lastLocation.long,
                    lastLocation.alt,
                    TimeZone.getDefault()
                )
            )
            zmanim.add(Pair("Daf:", daf))
            if (date.calendar.get(Calendar.DAY_OF_WEEK) == 6) {
                zmanim.add(Pair("Lighting:", getDate(date.candleLighting)))
            }
            zmanim.add(Pair("Sunrise:", getDate(date.sunrise)))
            zmanim.add(Pair("Sunset:", getDate(date.sunset)))
            zmanim.add(Pair("Alos Hashachar:", getDate(date.alos72)))
            zmanim.add(Pair("Sof Shma (MA)", getDate(date.sofZmanShmaMGA)))
            zmanim.add(Pair("Sof Shma (GRA)", getDate(date.sofZmanShmaGRA)))
            zmanim.add(Pair("Sof Tefila (MA)", getDate(date.sofZmanTfilaMGA)))
            zmanim.add(Pair("Sof Tefila (GRA)", getDate(date.sofZmanTfilaGRA)))
            zmanim.add(Pair("Chatzos:",  getDate(date.chatzos)))
            zmanim.add(Pair("Early Mincha:", getDate(date.minchaGedola)))
            zmanim.add(Pair("Plag HaMincha:", getDate(date.plagHamincha)))
            zmanim.add(Pair("Tzais:", getDate(date.tzais)))
            zmanim.add(Pair("Tzais (RT):", getDate(date.tzais72)))
        }


        zAdapter = ZmanimAdapter(zmanim, this)
        wearableRecyclerView.adapter = zAdapter
    }

    private fun getDate(date: Date): String {
        val mins: String = if (date.minutes < 10) {
            "0" + date.minutes
        } else {
            ""+ date.minutes
        }

        return "" + date.hours + ":" + mins
    }
}
