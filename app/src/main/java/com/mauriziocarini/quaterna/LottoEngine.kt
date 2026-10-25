      fun calcolaTuttoLoStorico(archivio: List<Estrazione>): List<Previsione> {
        val risultati = mutableListOf<Previsione>()
        val dateRoma = archivio.filter { it.ruota == "RM" }.map { it.data }.distinct().sorted()
        
        // FILTRO: Partiamo da Ottobre 2025 ("2025/10")
        val mesi = dateRoma.map { it.substring(0, 7) }.distinct().filter { it >= "2025/10" }
        
        val archivioPerData = archivio.groupBy { it.data }
        val tutteLeDateOrdinate = archivio.map { it.data }.distinct().sorted()

        for (mese in mesi) {
            val dateMese = dateRoma.filter { it.startsWith(mese) }.sorted()
            if (dateMese.size >= 5) {
                val dataTarget = dateMese[4]
                val prev = calcolaPrevisione(dataTarget, archivio)
                if (prev != null) {
                    val anno = mese.substring(2, 4)
                    val meseNum = mese.substring(5, 7).toInt()
                    val nomiMesi = arrayOf("GEN", "FEB", "MAR", "APR", "MAG", "GIU", "LUG", "AGO", "SET", "OTT", "NOV", "DIC")
                    prev.meseAnno = "${nomiMesi[meseNum - 1]}-$anno"
                    
                    verificaEsitiVeloce(prev, archivioPerData, tutteLeDateOrdinate)
                    risultati.add(prev)
                }
            }
        }
        return risultati.sortedByDescending { it.dataTarget }
    }
