package com.mauriziocarini.quaterna

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : Activity() {

    private val clrSfondo = Color.parseColor("#0A0A12")
    private val clrPannello = Color.parseColor("#161622")
    private val clrOro = Color.parseColor("#D4AF5A")
    private val clrAzzurro = Color.parseColor("#38BDF8")
    private val clrVerde = Color.parseColor("#00E676")
    private val clrRosso = Color.parseColor("#E57373")
    private val clrTesto = Color.parseColor("#E4E4EE")
    private val clrGrigioScuro = Color.parseColor("#2A2A42")

    private lateinit var tvStatusTop: TextView
    private lateinit var llStorico: LinearLayout
    private lateinit var btnAggiorna: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(clrSfondo)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 40, 30, 40)
        scroll.addView(layout)

        // 1. BARRA DI STATO (In alto)
        tvStatusTop = TextView(this).apply {
            text = "In attesa di aggiornamento..."
            textSize = 12f
            setTextColor(clrOro)
            setPadding(0, 0, 0, 20)
        }
        layout.addView(tvStatusTop)

        // 2. GRIGLIA PULSANTI (Riga 1)
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        btnAggiorna = creaPulsante("⬇ Aggiorna", clrAzzurro)
        val btnManuale = creaPulsante("✏ Manuale", clrVerde)
        val btnPrev = creaPulsante("🎯 Previsione", clrVerde)
        row1.addView(btnAggiorna); row1.addView(btnManuale); row1.addView(btnPrev)
        layout.addView(row1)

        // 3. GRIGLIA PULSANTI (Riga 2)
        val row2 = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 30)
        }
        val btnArchivio = creaPulsante("📋 Archivio", clrOro)
        val btnReport = creaPulsante("📄 Report", clrAzzurro)
        row2.addView(btnArchivio); row2.addView(btnReport)
        layout.addView(row2)

        // 4. TITOLO SEZIONE
        val tvTitoloStorico = TextView(this).apply {
            text = "📊 Storico esiti (da Ottobre 2025)"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(clrTesto)
            setPadding(0, 0, 0, 20)
        }
        layout.addView(tvTitoloStorico)

        // 5. CONTENITORE PER LE CARD DEGLI ESITI
        llStorico = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(llStorico)

        // --- LOGICA ---
        caricaDatiLocali()

        btnAggiorna.setOnClickListener {
            btnAggiorna.text = "Download..."
            btnAggiorna.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                val dati = NetworkClient.scaricaStorico()
                if (dati != null) {
                    salvaDatiLocali(dati)
                    caricaDatiLocali()
                } else {
                    tvStatusTop.text = "✖ Errore di download. Controlla la connessione."
                    tvStatusTop.setTextColor(clrRosso)
                }
                btnAggiorna.text = "⬇ Aggiorna"
                btnAggiorna.isEnabled = true
            }
        }

        // Azioni per gli altri pulsanti (per ora mostrano un avviso)
        btnManuale.setOnClickListener { mostraAvviso("Archivio Manuale", "Funzione in fase di sviluppo.") }
        btnArchivio.setOnClickListener { mostraAvviso("Archivio", "Visualizzazione estrazioni in sviluppo.") }
        btnPrev.setOnClickListener { 
            if (llStorico.childCount > 0) {
                mostraAvviso("Previsione Attiva", "La previsione più recente è la prima card in alto.")
            } else {
                mostraAvviso("Previsione", "Nessun dato caricato.")
            }
        }
        btnReport.setOnClickListener { mostraAvviso("Report", "Generazione PDF/TXT in fase di sviluppo.") }

        setContentView(scroll)
    }

    private fun creaPulsante(testo: String, colore: Int): Button {
        val b = Button(this)
        b.text = testo
        b.setTextColor(Color.WHITE)
        b.textSize = 12f
        b.setBackgroundColor(clrGrigioScuro)
        
        // Bordo colorato
        val shape = GradientDrawable().apply {
            setColor(clrGrigioScuro)
            cornerRadius = 15f
            setStroke(2, colore)
        }
        b.background = shape
        
        val params = LinearLayout.LayoutParams(0, 110, 1f)
        params.setMargins(5, 0, 5, 0)
        b.layoutParams = params
        return b
    }

    private fun caricaDatiLocali() {
        val file = File(filesDir, "storico.txt")
        if (!file.exists()) {
            tvStatusTop.text = "Nessun dato. Clicca 'Aggiorna'."
            return
        }
        val testo = file.readText()
        val archivio = LottoEngine.parseStorico(testo)
        val ultimaData = archivio.lastOrNull()?.data ?: "Sconosciuta"
        
        tvStatusTop.text = "Aggiornato! ${archivio.size} righe • Ultima estrazione: $ultimaData"
        tvStatusTop.setTextColor(Color.WHITE)

        val storico = LottoEngine.calcolaTuttoLoStorico(archivio)
        llStorico.removeAllViews()

        for (prev in storico) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(30, 30, 30, 30)
                val shape = GradientDrawable().apply {
                    setColor(clrPannello)
                    cornerRadius = 15f
                    setStroke(1, Color.parseColor("#333344"))
                }
                background = shape
                val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                p.setMargins(0, 0, 0, 20)
                layoutParams = p
            }

            // Riga 1 della Card (Mese e Data)
            val r1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val tvMese = TextView(this).apply {
                text = prev.meseAnno
                textSize = 18f; setTextColor(clrOro); setTypeface(null, Typeface.BOLD)
            }
            val tvData = TextView(this).apply {
                text = "5° Estr: ${prev.dataTarget}"
                textSize = 14f; setTextColor(clrTesto)
                gravity = Gravity.END
            }
            val lpData = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            tvData.layoutParams = lpData
            r1.addView(tvMese); r1.addView(tvData)

            // Riga 2 (Quaterna calcolata)
            val tvQuaterna = TextView(this).apply {
                text = "Quaterna: " + prev.q.joinToString(" - ") { String.format("%02d", it) }
                textSize = 16f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD)
                setPadding(0, 10, 0, 10)
            }

            // Riga 3 (Esiti)
            val tvEsiti = TextView(this).apply {
                textSize = 14f
                if (prev.stato == "VINCENTE") {
                    text = "✔ " + prev.esitiDettaglio.joinToString("\n✔ ")
                    setTextColor(clrVerde)
                } else if (prev.stato == "IN GIOCO") {
                    text = "⏳ In gioco: colpo ${prev.colpiGiocati + 1}/13"
                    setTextColor(clrAzzurro)
                } else {
                    text = "✖ Negativo (13 colpi esauriti)"
                    setTextColor(clrRosso)
                }
            }

            card.addView(r1)
            card.addView(tvQuaterna)
            card.addView(tvEsiti)
            llStorico.addView(card)
        }
    }

    private fun salvaDatiLocali(testo: String) {
        try { File(filesDir, "storico.txt").writeText(testo) } catch (e: Exception) { e.printStackTrace() }
    }

    private fun mostraAvviso(titolo: String, messaggio: String) {
        AlertDialog.Builder(this).setTitle(titolo).setMessage(messaggio).setPositiveButton("OK", null).show()
    }
}
