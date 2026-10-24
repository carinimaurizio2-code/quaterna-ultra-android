package com.mauriziocarini.quaterna

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : Activity() { // <--- Usiamo Activity base, non AppCompatActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(60, 60, 60, 60)

        val tvRisultato = TextView(this)
        tvRisultato.text = "Premi il bottone per scaricare i dati da GitHub"
        tvRisultato.textSize = 18f

        val btnScarica = Button(this)
        btnScarica.text = "Scarica Storico"
        
        btnScarica.setOnClickListener {
            btnScarica.text = "Download..."
            tvRisultato.text = "Connessione in corso..."
            
            // Usiamo un approccio semplice e sicuro per l'asincrono
            CoroutineScope(Dispatchers.Main).launch {
                val dati = NetworkClient.scaricaStorico()
                if (dati != null) {
                    tvRisultato.text = "Successo! Righe scaricate: ${dati.lines().size}"
                } else {
                    tvRisultato.text = "Errore: impossibile scaricare i dati."
                }
                btnScarica.text = "Riprova"
            }
        }

        layout.addView(tvRisultato)
        layout.addView(btnScarica)
        setContentView(layout)
    }
}
