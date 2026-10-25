    fun calcolaTuttoLoStorico(archivio: List<Estrazione>): List<Previsione> {
        val risultati = mutableListOf<Previsione>()
        val dateRoma = archivio.filter { it.ruota == "RM" }.map { it.data }.distinct().sorted()
        
        // QUI C'E' LA MODIFICA: Aggiungiamo il filtro per far partire da Ottobre 2025
        val mesi = dateRoma.map { it.substring(0, 7) }.distinct().filter { it >= "2025/10" }
        
        val archivioPerData = archivio.groupBy { it.data }
        val tutteLeDateOrdinate = archivio.map { it.data }.distinct().sorted()

        for (mese in mesi) {
            // ... il resto del codice rimane identico
