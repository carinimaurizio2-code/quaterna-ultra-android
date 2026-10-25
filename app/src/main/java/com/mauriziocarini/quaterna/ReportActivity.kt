
package com.mauriziocarini.quaterna

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class ReportActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val reportTesto = intent.getStringExtra("REPORT_TESTO") ?: "Errore nel caricamento del report."

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0A12"))
            setPadding(30, 40, 30, 40)
        }

        val tvTitolo = TextView(this).apply {
            text = "📄 REPORT QUATERNA ULTRA"
            textSize = 22f
            setTextColor(Color.parseColor("#D4AF5A"))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        layout.addView(tvTitolo)

        val scroll = ScrollView(this)
        val tvReport = TextView(this).apply {
            text = reportTesto
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(20, 20, 20, 20)
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#161622"))
                cornerRadius = 15f
                setStroke(1, Color.parseColor("#333344"))
            }
            background = shape
        }
        scroll.addView(tvReport)
        
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        params.setMargins(0, 0, 0, 20)
        scroll.layoutParams = params
        layout.addView(scroll)

        val btnCondividi = Button(this).apply {
            text = "Condividi / Copia Report"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#38BDF8"))
            textSize = 16f
            setOnClickListener {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, reportTesto)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Condividi Report"))
            }
        }
        layout.addView(btnCondividi)

        setContentView(layout)
    }
}
