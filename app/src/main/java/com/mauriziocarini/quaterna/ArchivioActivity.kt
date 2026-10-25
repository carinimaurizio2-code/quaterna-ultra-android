package com.mauriziocarini.quaterna

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ArchivioActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0A12"))
            setPadding(20, 40, 20, 20)
        }

        val tvTitolo = TextView(this).apply {
            text = "📋 ARCHIVIO COMPLETO"
            textSize = 22f
            setTextColor(Color.parseColor("#D4AF5A"))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        layout.addView(tvTitolo)

        val listView = ListView(this).apply {
            dividerHeight = 2
            divider = GradientDrawable().apply { setColor(Color.parseColor("#333344")) }
        }
        layout.addView(listView)
        setContentView(layout)

        val file = File(filesDir, "storico.txt")
        if (!file.exists()) {
            tvTitolo.text = "Nessun dato in archivio."
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val righe = withContext(Dispatchers.Default) {
                val testo = file.readText()
                val archivio = LottoEngine.parseStorico(testo)
                
                // RIMOSSO IL LIMITE: Ora prende tutto l'archivio e lo inverte (dal più recente)
                archivio.reversed().map { est ->
                    "${est.data.replace("/", "-")}  |  ${est.ruota}  |  ${est.n1} - ${est.n2} - ${est.n3} - ${est.n4} - ${est.n5}"
                }
            }

            val adapter = object : ArrayAdapter<String>(this@ArchivioActivity, android.R.layout.simple_list_item_1, righe) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent) as TextView
                    view.setTextColor(Color.WHITE)
                    view.textSize = 14f
                    view.setPadding(10, 20, 10, 20)
                    // Evidenzia la ruota di Roma in oro
                    if (righe[position].contains("|  RM  |")) view.setTextColor(Color.parseColor("#D4AF5A"))
                    return view
                }
            }
            listView.adapter = adapter
        }
    }
}
