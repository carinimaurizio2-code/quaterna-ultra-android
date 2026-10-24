package com.mauriziocarini.quaterna

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Creiamo una UI di base via codice
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val tvRisultato = TextView(this).apply {
            text = "Clicca il bottone per scaricare i dati da GitHub"
            textSize = 18f
            setPadding(0, 0, 0, 30)
        }

        val btnScarica = Button(this).apply {
            text = "Scarica Storico"
            setOnClickListener {
                text = "Download in corso..."
                lifecycleScope.launch {
                    val dati = NetworkClient.scaricaStorico()
                    if (dati != null) {
                        tvRisultato.text = "Dati scaricati! Righe totali: ${dati.lines().size}"
                    } else {
                        tvRisultato.text = "Errore di download. Controlla la connessione."
                    }
                    text = "Riprova"
                }
            }
        }

        layout.addView(tvRisultato)
        layout.addView(btnScarica)
        setContentView(layout)
    }
}
