package com.mauriziocarini.quaterna

object LottoEngine {

    data class Estrazione(val data: String, val ruota: String, val n1: Int, val n2: Int, val n3: Int, val n4: Int, val n5: Int) {
        fun numeri() = intArrayOf(n1, n2, n3, n4, n5)
    }

    data class Previsione(
        var dataTarget: String = "",
        var meseAnno: String = "",
        var figura: Int = 0,
        var numBase: Int = 0,
        var q: IntArray = intArrayOf(0, 0, 0, 0),
        var esitiDettaglio: MutableList<String> = mutableListOf(),
        var stato: String = "IN ATTESA",
        var colpiGiocati: Int = 0,
        var colpoMinimo: Int = 0
    )

    fun parseStorico(testo: String): List<Estrazione> {
        val lista = mutableListOf<Estrazione>()
        testo.lines().forEach { riga ->
            val campi = riga.trim().split(Regex("\\s+"))
            if (campi.size >= 7) {
                try {
                    val data = campi[0]
                    val ruota = campi[1].uppercase().replace("ROMA", "RM").replace("NAZIONALE", "RN").replace("CAGLIARI", "CA")
                    lista.add(Estrazione(data, ruota, campi[2].toInt(), campi[3].toInt(), campi[4].toInt(), campi[5].toInt(), campi[6].toInt()))
                } catch (e: Exception) { }
            }
        }
        return lista.sortedBy { it.data }
    }

    private fun fuori90(n: Int): Int {
        var v = n % 90
        if (v <= 0) v += 90
        return v
    }

    private fun calcolaFigura(n: Int): Int {
        var sum = (n / 10) + (n % 10)
        while (sum > 9) sum -= 9
        if (sum == 0) sum = 9
        return sum
    }

    private fun risolviDuplicati(q: IntArray) {
        for (i in 1 until q.size) {
            var tentativi = 0
            while (tentativi < 100 && (0 until i).any { q[it] == q[i] }) {
                q[i] = fuori90(q[i] + 1)
                tentativi++
            }
        }
    }

    // Calcola l'intero storico per la Dashboard
    fun calcolaTuttoLoStorico(archivio: List<Estrazione>): List<Previsione> {
        val risultati = mutableListOf<Previsione>()
        val dateRoma = archivio.filter { it.ruota == "RM" }.map { it.data }.distinct().sorted()
        val mesi = dateRoma.map { it.substring(0, 7) }.distinct() // es. "2025/10"

        for (mese in mesi) {
            val dateMese = dateRoma.filter { it.startsWith(mese) }.sorted()
            if (dateMese.size >= 5) {
                val dataTarget = dateMese[4] // 5° estrazione del mese
                val prev = calcolaPrevisione(dataTarget, archivio)
                if (prev != null) {
                    // Formatta il mese per la grafica (es. "OTT-25")
                    val anno = mese.substring(2, 4)
                    val meseNum = mese.substring(5, 7).toInt()
                    val nomiMesi = arrayOf("GEN", "FEB", "MAR", "APR", "MAG", "GIU", "LUG", "AGO", "SET", "OTT", "NOV", "DIC")
                    prev.meseAnno = "${nomiMesi[meseNum - 1]}-$anno"
                    
                    verificaEsiti(prev, archivio)
                    risultati.add(prev)
                }
            }
        }
        return risultati.sortedByDescending { it.dataTarget } // I più recenti in alto
    }

    private fun calcolaPrevisione(dataTarget: String, archivio: List<Estrazione>): Previsione? {
        val eRM = archivio.firstOrNull { it.data == dataTarget && it.ruota == "RM" } ?: return null
        val eCA = archivio.firstOrNull { it.data == dataTarget && it.ruota == "CA" }
        val fig = calcolaFigura(eRM.n1)
        val res = Previsione(dataTarget = dataTarget, figura = fig, numBase = eRM.n1)

        var ru: Array<String>; var po: IntArray; var ad: IntArray
        when (fig) {
            8, 9 -> { ru = arrayOf("RM", "RM", "RM", "CA"); po = intArrayOf(4, 5, 5, 4); ad = intArrayOf(90, 48, 20, 69) }
            2, 7 -> { ru = arrayOf("RM", "RM", "RM", "CA"); po = intArrayOf(3, 4, 5, 4); ad = intArrayOf(4, 79, 32, 36) }
            1, 4, 5 -> { ru = arrayOf("RM", "RM", "RM", "CA"); po = intArrayOf(2, 3, 3, 1); ad = intArrayOf(14, 5, 29, 44) }
            3, 6 -> { ru = arrayOf("RM", "RM", "RM", "RM"); po = intArrayOf(1, 4, 1, 1); ad = intArrayOf(6, 13, 9, 45) }
            else -> return null
        }

        if (ru.contains("CA") && eCA == null) return null

        val q = IntArray(4)
        for (i in 0..3) {
            val est = if (ru[i] == "RM") eRM else eCA!!
            val base = est.numeri()[po[i] - 1]
            q[i] = fuori90(base + ad[i])
        }
        risolviDuplicati(q)
        res.q = q
        return res
    }

    private fun verificaEsiti(res: Previsione, archivio: List<Estrazione>) {
        val dateSuccessive = archivio.filter { it.data > res.dataTarget }.map { it.data }.distinct().sorted().take(13)
        
        for ((index, data) in dateSuccessive.withIndex()) {
            val colpo = index + 1
            val estrazioniGiorno = archivio.filter { it.data == data }

            for (estr in estrazioniGiorno) {
                val hit = estr.numeri().count { num -> res.q.contains(num) }
                if (hit >= 2) {
                    val numeriColpiti = res.q.filter { estr.numeri().contains(it) }.joinToString("-") { String.format("%02d", it) }
                    val sorte = if (hit == 4) "Quaterna" else if (hit == 3) "Terno" else "Ambo"
                    
                    var ruotaTxt = ""
                    if (estr.ruota == "RM") ruotaTxt = "ROMA"
                    else if (estr.ruota == "RN") ruotaTxt = "NAZIONALE"
                    else if (hit >= 3) ruotaTxt = "TUTTE"
                    else continue 

                    // Aggiungiamo l'esito formattato
                    res.esitiDettaglio.add("$sorte: $numeriColpiti ($ruotaTxt)")
                    if (res.stato != "VINCENTE") {
                        res.stato = "VINCENTE"
                        res.colpoMinimo = colpo
                    }
                }
            }
            res.colpiGiocati = colpo
            if (res.stato == "VINCENTE") break 
        }
        
        if (res.stato != "VINCENTE") {
            if (res.colpiGiocati >= 13) res.stato = "NEGATIVO"
            else res.stato = "IN GIOCO"
        }
    }
}
