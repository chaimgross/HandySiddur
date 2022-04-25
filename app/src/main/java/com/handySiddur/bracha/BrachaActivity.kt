package com.handySiddur.bracha

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.support.wearable.view.CardFrame
import android.widget.ImageView
import androidx.core.text.HtmlCompat


class BrachaActivity: WearableActivity() {
    private lateinit var cardFrame: CardFrame
    private lateinit var textView: ZoomTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bracha_activity)

        textView = findViewById(R.id.text_view)
        textView.setListener { switchColor() }
//        val scrollView = findViewById<CardScrollView>(R.id.scroll_view)
//        scrollView.isExpansionEnabled = true;
//        scrollView.expansionDirection = 1;
//        scrollView.expansionFactor = 10.0F;
        val bracha = intent.getSerializableExtra("bracha") as? BrachaItem

        val imageIcon = findViewById<ImageView>(R.id.image_icon)

        if (bracha?.image != null) {
            imageIcon.setImageResource(bracha.image)
        }

        cardFrame = findViewById(R.id.card_frame)
        cardFrame.isExpansionEnabled = true
        cardFrame.expansionDirection = 1
        cardFrame.expansionFactor = 10.0f
        setColor()
        if (bracha?.text != null) {
            textView.text = HtmlCompat.fromHtml(bracha.text,  HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }

    private fun switchColor(): Boolean {
        val prefs: SharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val color = prefs.getString("color_mode", "light")
        if (color == "light") {
            prefs.edit().putString("color_mode", "dark").apply()
            cardFrame.setBackgroundColor(Color.BLACK)
            textView.setTextColor(Color.WHITE)
        }
        else {
            prefs.edit().putString("color_mode", "light").apply()
            cardFrame.setBackgroundColor(Color.WHITE)
            textView.setTextColor(Color.BLACK)
        }
        return true
    }

    private fun setColor() {
        val prefs: SharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val color = prefs.getString("color_mode", "light")
        if (color == "light") {
            cardFrame.setBackgroundColor(Color.WHITE)
            textView.setTextColor(Color.BLACK)
        }
        else {
            cardFrame.setBackgroundColor(Color.BLACK)
            textView.setTextColor(Color.WHITE)
        }
    }
}