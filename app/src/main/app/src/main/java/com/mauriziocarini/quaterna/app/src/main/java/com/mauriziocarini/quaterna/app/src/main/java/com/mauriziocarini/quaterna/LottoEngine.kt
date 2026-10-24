package com.mauriziocarini.quaterna

import java.security.MessageDigest

object LottoEngine {

    data class Estrazione(
        val dataEstr: String,
        val ruota: String,
        val n1: Int, val n2: Int, val n3: Int, val n4: Int, val n5: Int
    ) {
        fun getNumeri(): IntArray = intArrayOf(n1, n2, n3, n4, n5)
    }

    data class Previsione(
        var dataCalcolo: String = "",
        var annoRiferimento: Int = 0,
        var meseRiferimento: Int = 0,
        var ruotaGioco: String = "RM",
        var numeroBaseFigura: Int = 0,
        var figura: Int = 0,
        var q1: Int = 0, var q2: Int = 0, var q3: Int = 0, var q4: Int = 0,
        var stringaQuaterna: String = "",
        var dettaglioCalcolo: MutableList<String> = mutableListOf(),
        var esitiPlurimi: MutableList<String> = mutableListOf(),
        var colpoMinimo: Int = 0,
        var stato: String = "NEGATIVO",
        var colpiGiocati: Int = 0,
        var nAmbiTot: Int = 0,
        var nTerniTot: Int = 0,
        var nQuaterneSecche: Int = 0
    ) {
        fun getNumeri(): IntArray = intArrayOf(q1, q2, q3, q4)
    }

    fun fuori90(n: Int): Int {
        var v = n % 90
        if (v <= 0) v += 90
        return v
    }

    fun calcolaFigura(n: Int): Int {
        var sum = (n / 10) + (n % 10)
        while (sum > 9) sum -= 9
        if (sum == 0) sum = 9
        return sum
    }

    fun risolviDuplicatiQuaterna(q: IntArray) {
        for (i in 1 until q.size) {
            var tentativi = 0
            while (tentativi < 100 && duplicatoConPrecedenti(q, i)) {
                q[i] = fuori90(q[i] + 1)
                tentativi++
            }
        }
    }

    private fun duplicatoConPrecedenti(q: IntArray, indice: Int): Boolean {
        for (j in 0 until indice) {
            if (q[j] == q[indice]) return true
        }
        return false
    }

    fun calcolaPrevisione(dataTarget: String, anno: Int, mese: Int, archivio: List<Estrazione>): Previsione? {
        val res = Previsione(dataCalcolo = dataTarget, annoRiferimento = anno, meseRiferimento = mese)
        
        val eRM = archivio.firstOrNull { it.dataEstr == dataTarget && it.ruota == "RM" } ?: return null
        val eCA = archivio.firstOrNull { it.dataEstr == dataTarget && it.ruota == "CA" }
        
        val fig = calcolaFigura(eRM.n1)
        res.figura = fig
        res.numeroBaseFigura = eRM.n1

        var ru: Array<String>
        var po: IntArray
        var ad: IntArray

        when (fig) {
            8, 9 -> {
                ru = arrayOf("RM", "RM", "RM", "CA")
                po = intArrayOf(4, 5, 5, 4)
                ad = intArrayOf(90, 48, 20, 69)
            }
            2, 7 -> {
                ru = arrayOf("RM", "RM", "RM", "CA")
                po = intArrayOf(3, 4, 5, 4)
                ad = intArrayOf(4, 79, 32, 36)
            }
            1, 4, 5 -> {
                ru = arrayOf("RM", "RM", "RM", "CA")
                po = intArrayOf(2, 3, 3, 1)
                ad = intArrayOf(14, 5, 29, 44)
            }
            3, 6 -> {
                ru = arrayOf("RM", "RM", "RM", "RM")
                po = intArrayOf(1, 4, 1, 1)
                ad = intArrayOf(6, 13, 9, 45)
            }
            else -> return null
        }

        if (ru.contains("CA") && eCA == null) return null

        val grezzi = IntArray(4)
        val q = IntArray(4)
        val basi = IntArray(4)

        for (i in 0..3) {
            val est = if (ru[i] == "RM") eRM else eCA!!
            val numeri = est.getNumeri()
            basi[i] = numeri[po[i] - 1]
            grezzi[i] = fuori90(basi[i] + ad[i])
            q[i] = grezzi[i]
        }

        risolviDuplicatiQuaterna(q)

        for (i in 0..3) {
            var riga = "${i + 1}° numero:  ${po[i]}° estratto di ${ru[i]} (${String.format("%02d", basi[i])}) + ${ad[i]} = ${String.format("%02d", grezzi[i])}"
            if (q[i] != grezzi[i]) {
                riga += "  →  numero doppio, si gioca il +1: ${String.format("%02d", q[i])}"
            }
            res.dettaglioCalcolo.add(riga)
        }

        res.q1 = q[0]; res.q2 = q[1]; res.q3 = q[2]; res.q4 = q[3]
        res.stringaQuaterna = "${String.format("%02d", q[0])}-${String.format("%02d", q[1])}-${String.format("%02d", q[2])}-${String.format("%02d", q[3])}"
        
        return res
    }

    fun calcolaHardwareID(androidId: String): String {
        val input = "$androidId|QUATERNAULTRA2026"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val hex = bytes.joinToString("") { "%02x".format(it) }.uppercase()
        return "${hex.substring(0, 4)}-${hex.substring(4, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}"
    }
}
