package com.mauriziocarini.quaterna

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    // Salviamo i dati calcolati qui per usarli nel report
    private var storicoCalcolato: List<LottoEngine.Previsione> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(clrSfondo)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 40, 30, 40)
        scroll.addView(layout)

        // 1. TITOLO IN ALTO
        val tvTitoloApp = TextView(this).apply {
            text = "🎯 QUATERNA ULTRA"
            textSize = 28f
            setTextColor(clrOro)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        layout.addView(tvTitoloApp)

        tvStatusTop = TextView(this).apply {
            text = "In attesa di aggiornamento..."
            textSize = 12f
            setTextColor(clrTesto)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        layout.addView(tvStatusTop)

        // PULSANTI RIGA 1
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        btnAggiorna = creaPulsante("⬇ Aggiorna", clrAzzurro)
        val btnManuale = creaPulsante("✏ Incolla", clrVerde)
        val btnPrev = creaPulsante("🎯 Previsione", clrVerde)
        row1.addView(btnAggiorna)
        row1.addView(btnManuale)
        row1.addView(btnPrev)
        layout.addView(row1)

        // PULSANTI RIGA 2
        val row2 = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 30)
        }
        val btnArchivio = creaPulsante("📋 Archivio", clrOro)
        val btnReport = creaPulsante("📄 Report", clrAzzurro)
        row2.addView(btnArchivio)
        row2.addView(btnReport)
        layout.addView(row2)

        val tvTitoloStorico = TextView(this).apply {
            text = "📊 Storico esiti"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(clrTesto)
            setPadding(0, 0, 0, 20)
        }
        layout.addView(tvTitoloStorico)

        llStorico = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layout.addView(llStorico)

        // --- AZIONI DEI PULSANTI ---
        
        caricaDatiLocali()

        btnAggiorna.setOnClickListener {
            btnAggiorna.text = "Download..."
            btnAggiorna.isEnabled = false
            tvStatusTop.text = "Scaricamento in corso da GitHub..."
            tvStatusTop.setTextColor(clrAzzurro)

            CoroutineScope(Dispatchers.Main).launch {
                val dati = NetworkClient.scaricaStorico()
                if (dati != null) {
                    salvaDatiLocali(dati)
                    caricaDatiLocali()
                } else {
                    tvStatusTop.text = "✖ Errore di download."
                    tvStatusTop.setTextColor(clrRosso)
                    btnAggiorna.text = "⬇ Aggiorna"
                    btnAggiorna.isEnabled = true
                }
            }
        }

        btnManuale.setOnClickListener { mostraDialogoIncolla() }
        
        btnArchivio.setOnClickListener { 
            startActivity(Intent(this, ArchivioActivity::class.java)) 
        }
        
        btnPrev.setOnClickListener { 
            if (llStorico.childCount > 0) mostraAvviso("Previsione Attiva", "La previsione più recente è la prima card in alto.")
            else mostraAvviso("Previsione", "Nessun dato caricato.")
        }
        
        btnReport.setOnClickListener { generaEReport() }

        setContentView(scroll)
    }

    private fun creaPulsante(testo: String, colore: Int): Button {
        val b = Button(this)
        b.text = testo
        b.setTextColor(Color.WHITE)
        b.textSize = 12f
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
            tvStatusTop.text = "Nessun dato. Clicca 'Aggiorna' o 'Incolla'."
            return
        }

        tvStatusTop.text = "⏳ Elaborazione dati in corso..."
        tvStatusTop.setTextColor(clrOro)
        llStorico.removeAllViews()

        CoroutineScope(Dispatchers.Main).launch {
            val (archivioSize, ultimaData, storico) = withContext(Dispatchers.Default) {
                val testo = file.readText()
                val archivio = LottoEngine.parseStorico(testo)
                val ultima = archivio.lastOrNull()?.data ?: "Sconosciuta"
                val st = LottoEngine.calcolaTuttoLoStorico(archivio)
                Triple(archivio.size, ultima, st)
            }

            storicoCalcolato = storico
            
            tvStatusTop.text = "Aggiornato! $archivioSize righe • Ultima: $ultimaData"
            tvStatusTop.setTextColor(Color.WHITE)
            btnAggiorna.text = "⬇ Aggiorna"
            btnAggiorna.isEnabled = true

            if (storico.isEmpty()) {
                val tvNoDati = TextView(this@MainActivity).apply {
                    text = "Dati insufficienti per calcolare le previsioni."
                    setTextColor(clrRosso)
                }
                llStorico.addView(tvNoDati)
                return@launch
            }

            for (prev in storico) {
                val card = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(30, 30, 30, 30)
                    background = GradientDrawable().apply {
                        setColor(clrPannello)
                        cornerRadius = 15f
                        setStroke(1, Color.parseColor("#333344"))
                    }
                    val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    p.setMargins(0, 0, 0, 20)
                    layoutParams = p
                }

                val r1 = LinearLayout(this@MainActivity).apply { orientation = LinearLayout.HORIZONTAL }
                val tvMese = TextView(this@MainActivity).apply { 
                    text = prev.meseAnno
                    textSize = 18f
                    setTextColor(clrOro)
                    setTypeface(null, Typeface.BOLD)
                }
                val tvData = TextView(this@MainActivity).apply { 
                    text = "5° Estr: ${prev.dataTarget}"
                    textSize = 14f
                    setTextColor(clrTesto)
                    gravity = Gravity.END
                }
                tvData.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                r1.addView(tvMese)
                r1.addView(tvData)

                val tvQuaterna = TextView(this@MainActivity).apply {
                    text = "Quaterna: " + prev.q.joinToString(" - ") { String.format("%02d", it) }
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 10, 0, 10)
                }

                val tvEsiti = TextView(this@MainActivity).apply {
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
    }

    private fun generaEReport() {
        if (storicoCalcolato.isEmpty()) {
            mostraAvviso("Errore", "Nessun dato disponibile. Aggiorna l'archivio prima di generare il report.")
            return
        }

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date())
        val sb = StringBuilder()
        
        sb.append("=========================================\n")
        sb.append("       REPORT - QUATERNA ULTRA\n")
        sb.append("=========================================\n")
        sb.append("Generato il: $dateStr\n\n")
        sb.append("--- STORICO PREVISIONI ED ESITI ---\n\n")
        
        for (prev in storicoCalcolato) {
            sb.append("MESE: ${prev.meseAnno}\n")
            sb.append("Data Calcolo: ${prev.dataTarget}\n")
            sb.append("Quaterna: ${prev.q.joinToString("-") { String.format("%02d", it) }}\n")
            
            if (prev.stato == "VINCENTE") {
                sb.append("ESITO: VINCENTE al ${prev.colpoMinimo}° colpo\n")
                for (esito in prev.esitiDettaglio) {
                    sb.append("  > $esito\n")
                }
            } else if (prev.stato == "IN GIOCO") {
                sb.append("ESITO: In gioco (colpo ${prev.colpiGiocati + 1} di 13)\n")
            } else {
                sb.append("ESITO: Negativo (13 colpi esauriti)\n")
            }
            sb.append("-----------------------------------------\n")
        }

        // Apre la nuova schermata del Report passando il testo
        val intent = Intent(this, ReportActivity::class.java)
        intent.putExtra("REPORT_TESTO", sb.toString())
        startActivity(intent)
    }

    private fun mostraDialogoIncolla() {
        val input = EditText(this).apply {
            hint = "Incolla qui (es:\n2026/10/25 RM 12 34 56 78 90)"
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
            setPadding(20, 20, 20, 20)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Aggiungi Estrazione Manuale")
            .setMessage("Incolla il testo. L'app aggiungerà i dati in fondo allo storico.")
            .setView(container)
            .setPositiveButton("Salva") { _, _ ->
                val testoIncollato = input.text.toString().trim()
                if (testoIncollato.isNotEmpty()) {
                    aggiungiAlloStorico(testoIncollato)
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun aggiungiAlloStorico(nuovoTesto: String) {
        val file = File(filesDir, "storico.txt")
        try {
            file.appendText("\n$nuovoTesto")
            mostraAvviso("Successo", "Estrazione aggiunta! Ricalcolo in corso...")
            caricaDatiLocali()
        } catch (e: Exception) {
            mostraAvviso("Errore", "Impossibile salvare i dati: ${e.message}")
        }
    }

    private fun salvaDatiLocali(testo: String) {
        try { File(filesDir, "storico.txt").writeText(testo) } catch (e: Exception) { e.printStackTrace() }
    }

    private fun mostraAvviso(titolo: String, messaggio: String) {
        AlertDialog.Builder(this).setTitle(titolo).setMessage(messaggio).setPositiveButton("OK", null).show()
    }
}
