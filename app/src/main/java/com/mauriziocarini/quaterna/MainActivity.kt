package com.mauriziocarini.quaterna

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : Activity() {

    private val clrSfondo = Color.parseColor("#0A0A12")
    private val clrPannello = Color.parseColor("#161622")
    private val clrOro = Color.parseColor("#D4AF5A")
    private val clrAzzurro = Color.parseColor("#5AA0DC")
    private val clrVerde = Color.parseColor("#50D282")
    private val clrRosso = Color.parseColor("#D25050")
    private val clrTesto = Color.parseColor("#E4E4EE")

    private lateinit var tvStato: TextView
    private lateinit var tvQuaterna: TextView
    private lateinit var tvInfo: TextView
    private lateinit var tvEsiti: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(clrSfondo)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 60, 40, 40)
        scroll.addView(layout)

        // TITOLO
        val tvTitolo = TextView(this)
        tvTitolo.text = "🎯 QUATERNA ULTRA"
        tvTitolo.textSize = 28f
        tvTitolo.setTextColor(clrOro)
        tvTitolo.gravity = Gravity.CENTER
        layout.addView(tvTitolo)

        // PULSANTE AGGIORNA
        val btnAggiorna = Button(this)
        btnAggiorna.text = "⬇ Aggiorna Internet"
        btnAggiorna.textSize = 16f
        btnAggiorna.setTextColor(Color.WHITE)
        btnAggiorna.setBackgroundColor(Color.parseColor("#2A2A42"))
        val paramsBtn = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
        paramsBtn.setMargins(0, 30, 0, 30)
        btnAggiorna.layoutParams = paramsBtn
        layout.addView(btnAggiorna)

        // PANNELLO PREVISIONE
        val pannelloQuaterna = LinearLayout(this)
        pannelloQuaterna.orientation = LinearLayout.VERTICAL
        pannelloQuaterna.setPadding(30, 30, 30, 30)
        val shape = GradientDrawable().apply {
            setColor(clrPannello); cornerRadius = 20f; setStroke(2, Color.parseColor("#333344"))
        }
        pannelloQuaterna.background = shape
        layout.addView(pannelloQuaterna)

        tvStato = TextView(this).apply {
            text = "Verifica archivio in corso..."
            textSize = 14f; setTextColor(clrAzzurro)
        }
        pannelloQuaterna.addView(tvStato)

        tvQuaterna = TextView(this).apply {
            text = "-- - -- - -- - --"
            textSize = 32f; setTextColor(clrOro); gravity = Gravity.CENTER
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 30, 0, 30); layoutParams = p
        }
        pannelloQuaterna.addView(tvQuaterna)
        
        tvInfo = TextView(this).apply {
            text = ""; textSize = 14f; setTextColor(clrTesto); gravity = Gravity.CENTER
        }
        pannelloQuaterna.addView(tvInfo)

        // ESITI IN DETTAGLIO
        tvEsiti = TextView(this).apply {
            text = ""; textSize = 15f; setTextColor(Color.WHITE)
            val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 30, 0, 30); layoutParams = p
        }
        layout.addView(tvEsiti)

        // PULSANTE REPORT
        val btnReport = Button(this)
        btnReport.text = "📄 Genera Report Dettagliato"
        btnReport.setTextColor(Color.WHITE)
        btnReport.setBackgroundColor(clrAzzurro)
        layout.addView(btnReport)

        // LOGICA INIZIALE
        caricaDatiLocali()

        btnAggiorna.setOnClickListener {
            btnAggiorna.text = "Download in corso..."
            btnAggiorna.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val dati = NetworkClient.scaricaStorico()
                if (dati != null) {
                    salvaDatiLocali(dati)
                    caricaDatiLocali()
                } else {
                    tvStato.text = "✖ Errore di download. Controlla la connessione."
                    tvStato.setTextColor(clrRosso)
                }
                btnAggiorna.text = "⬇ Aggiorna Internet"
                btnAggiorna.isEnabled = true
            }
        }

        btnReport.setOnClickListener {
            val reportTxt = "REPORT QUATERNA ULTRA\n\n" +
                    "Data 5° Estrazione: ${tvInfo.text}\n" +
                    "Quaterna: ${tvQuaterna.text}\n\n" +
                    "Esiti:\n${if (tvEsiti.text.isEmpty()) "Nessun esito registrato." else tvEsiti.text}"
            AlertDialog.Builder(this).setTitle("Report").setMessage(reportTxt).setPositiveButton("OK", null).show()
        }

        setContentView(scroll)
    }

    private fun caricaDatiLocali() {
        val file = File(filesDir, "storico.txt")
        if (!file.exists()) {
            tvStato.text = "Archivio non trovato. Clicca 'Aggiorna'."
            return
        }
        val testo = file.readText()
        val archivio = LottoEngine.parseStorico(testo)
        
        val ultimaQuinta = LottoEngine.trovaUltimaQuintaEstrazione(archivio)
        if (ultimaQuinta != null) {
            val dataTarget = ultimaQuinta.first
            val prev = LottoEngine.calcolaPrevisione(dataTarget, archivio)
            
            if (prev != null) {
                LottoEngine.verificaEsiti(prev, archivio)
                
                tvInfo.text = "5° Estr. del mese: $dataTarget (Fig. ${prev.figura})"
                tvQuaterna.text = String.format("%02d - %02d - %02d - %02d", prev.q[0], prev.q[1], prev.q[2], prev.q[3])
                
                if (prev.stato == "VINCENTE") {
                    tvStato.text = "✔ VINCENTE al ${prev.colpoMinimo}° colpo!"
                    tvStato.setTextColor(clrVerde)
                } else if (prev.stato == "IN GIOCO") {
                    tvStato.text = "⏳ In gioco... Colpo ${prev.colpiGiocati + 1} di 13"
                    tvStato.setTextColor(clrAzzurro)
                } else {
                    tvStato.text = "✖ SCADUTA (13 colpi esauriti)"
                    tvStato.setTextColor(clrRosso)
                }

                if (prev.esitiDettaglio.isNotEmpty()) {
                    tvEsiti.text = "ESITI IN DETTAGLIO:\n" + prev.esitiDettaglio.joinToString("\n")
                    tvEsiti.setTextColor(clrVerde)
                } else {
                    tvEsiti.text = "Nessun esito registrato finora."
                    tvEsiti.setTextColor(clrTesto)
                }
            }
        } else {
            tvStato.text = "In attesa dei dati per la 5° estrazione..."
        }
    }

    private fun salvaDatiLocali(testo: String) {
        try { File(filesDir, "storico.txt").writeText(testo) } catch (e: Exception) { e.printStackTrace() }
    }
}
