package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StatsExporter {

    fun generateSyntheticTextReport(
        items: List<RoutineItem>,
        periodLabel: String = "Settimana Corrente",
        includeReminders: Boolean = true
    ): String {
        val targetItems = if (includeReminders) items else items.filter { it.type == RoutineType.EVENT }
        val events = items.filter { it.type == RoutineType.EVENT }
        val reminders = items.filter { it.type == RoutineType.REMINDER }

        val totalPlannedMins = targetItems.sumOf { it.plannedMinutes }
        val totalDoneMins = targetItems.sumOf { it.doneMinutes }
        val totalNotDoneMins = targetItems.sumOf { it.notDoneMinutes }
        val totalPendingMins = (totalPlannedMins - totalDoneMins - totalNotDoneMins).coerceAtLeast(0)

        val totalItemsCount = targetItems.size
        val completedCount = targetItems.count { it.executionStatus == ExecutionStatus.COMPLETED }
        val missedCount = targetItems.count { it.executionStatus == ExecutionStatus.MISSED }
        val pendingCount = targetItems.count { it.executionStatus == ExecutionStatus.PENDING }

        val completionPercent = if (totalItemsCount > 0) (completedCount * 100) / totalItemsCount else 0
        val timePercent = if (totalPlannedMins > 0) (totalDoneMins * 100) / totalPlannedMins else 0

        val completedEvents = events.count { it.executionStatus == ExecutionStatus.COMPLETED }
        val missedEvents = events.count { it.executionStatus == ExecutionStatus.MISSED }
        val pendingEvents = events.count { it.executionStatus == ExecutionStatus.PENDING }

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN)
        val nowStr = sdf.format(Date())

        val sb = StringBuilder()
        sb.appendLine("📊 REPORT SINTETICO DI PROGRESSO - WEEKROUTINE")
        sb.appendLine("📅 Periodo: $periodLabel")
        sb.appendLine("🕒 Data esportazione: $nowStr")
        sb.appendLine("────────────────────────────────────────")
        sb.appendLine("🏆 INDICE DI COMPLETAMENTO: $completionPercent%")
        sb.appendLine("⏱️ Tempo svolto: ${RoutineItem.formatMinutes(totalDoneMins)} / ${RoutineItem.formatMinutes(totalPlannedMins)} ($timePercent%)")
        sb.appendLine("❌ Tempo perso / non fatto: ${RoutineItem.formatMinutes(totalNotDoneMins)}")
        if (totalPendingMins > 0) {
            sb.appendLine("⏳ Tempo non ancora assegnato: ${RoutineItem.formatMinutes(totalPendingMins)}")
        }
        sb.appendLine("────────────────────────────────────────")

        sb.appendLine("📌 STATO EVENTI A ORARIO:")
        sb.appendLine("  • Totale eventi: ${events.size}")
        sb.appendLine("  • Fatti ✅: $completedEvents (${if (events.isNotEmpty()) (completedEvents * 100) / events.size else 0}%)")
        sb.appendLine("  • Non Fatti ❌: $missedEvents")
        sb.appendLine("  • Non Assegnati ⏳: $pendingEvents")

        if (includeReminders && reminders.isNotEmpty()) {
            val completedRem = reminders.count { it.executionStatus == ExecutionStatus.COMPLETED }
            sb.appendLine("\n📝 STATO PROMEMORIA / TO-DO:")
            sb.appendLine("  • Totale promemoria: ${reminders.size}")
            sb.appendLine("  • Completati: $completedRem / ${reminders.size}")
        }

        sb.appendLine("\n📈 PROGRESSO GIORNALIERO (Lun - Dom):")
        DayOfWeek.entries.forEach { day ->
            val dayItems = targetItems.filter { it.dayOfWeek == day.dayIndex }
            if (dayItems.isNotEmpty()) {
                val doneDay = dayItems.count { it.executionStatus == ExecutionStatus.COMPLETED }
                val missedDay = dayItems.count { it.executionStatus == ExecutionStatus.MISSED }
                val doneMins = dayItems.sumOf { it.doneMinutes }
                val plannedMins = dayItems.sumOf { it.plannedMinutes }
                val percent = (doneDay * 100) / dayItems.size
                sb.appendLine("  • ${day.shortName}: $doneDay/${dayItems.size} fatti ($percent%) | ${RoutineItem.formatMinutes(doneMins)}/${RoutineItem.formatMinutes(plannedMins)}${if (missedDay > 0) " (❌ $missedDay saltati)" else ""}")
            }
        }

        sb.appendLine("\n🏷️ TEMPO SVOLTO PER CATEGORIA:")
        RoutineCategory.entries.forEach { cat ->
            val catItems = targetItems.filter { it.category == cat }
            if (catItems.isNotEmpty()) {
                val catDone = catItems.sumOf { it.doneMinutes }
                val catPlanned = catItems.sumOf { it.plannedMinutes }
                val catNotDone = catItems.sumOf { it.notDoneMinutes }
                val doneCount = catItems.count { it.executionStatus == ExecutionStatus.COMPLETED }
                sb.appendLine("  • ${cat.label}: ${RoutineItem.formatMinutes(catDone)} fatti (su ${RoutineItem.formatMinutes(catPlanned)}) - $doneCount/${catItems.size} attività${if (catNotDone > 0) " | ${RoutineItem.formatMinutes(catNotDone)} persi" else ""}")
            }
        }

        sb.appendLine("\n💡 SINTESI DI PROGRESSO:")
        val motivation = when {
            completionPercent >= 80 -> "🌟 Eccellente costanza! Hai completato la stragrande maggioranza delle tue attività pianificate."
            completionPercent >= 60 -> "👍 Buon progresso! Hai mantenuto un ritmo solido, con margine per ottimizzare i blocchi rimasti indietro."
            completionPercent >= 40 -> "⚖️ Ritmo intermedio. Analizza le attività non svolte per riequilibrare la pianificazione settimanale."
            else -> "🎯 Settimana impegnativa. Rivedi la routine con blocchi più leggeri e realistici per ripartire con slancio!"
        }
        sb.appendLine(motivation)
        sb.appendLine("────────────────────────────────────────")
        sb.appendLine("Generato da WeekRoutine")

        return sb.toString()
    }

    fun generateCsvData(items: List<RoutineItem>): String {
        val sb = StringBuilder()
        // CSV Header
        sb.appendLine("ID,Giorno,Tipo,Titolo,Categoria,Priorita,Orario_Inizio,Orario_Fine,Minuti_Previsti,Stato_Esecuzione,Minuti_Svolti,Minuti_Persi,Note")

        items.sortedWith(compareBy<RoutineItem> { it.dayOfWeek }.thenBy { it.sortMinutes }).forEach { item ->
            val dayName = item.dayEnum.fullName
            val typeStr = if (item.type == RoutineType.EVENT) "Evento" else "Promemoria"
            val titleEscaped = "\"" + item.title.replace("\"", "\"\"") + "\""
            val notesEscaped = "\"" + item.notes.replace("\"", "\"\"") + "\""
            val startTime = if (item.startHour != null && item.startMinute != null) {
                String.format(Locale.getDefault(), "%02d:%02d", item.startHour, item.startMinute)
            } else ""
            val endTime = if (item.endHour != null && item.endMinute != null) {
                String.format(Locale.getDefault(), "%02d:%02d", item.endHour, item.endMinute)
            } else ""
            val statusStr = when (item.executionStatus) {
                ExecutionStatus.COMPLETED -> "FATTO"
                ExecutionStatus.MISSED -> "NON_FATTO"
                ExecutionStatus.PENDING -> "NON_ASSEGNATO"
            }

            sb.appendLine("${item.id},$dayName,$typeStr,$titleEscaped,${item.category.label},${item.priority.label},$startTime,$endTime,${item.plannedMinutes},$statusStr,${item.doneMinutes},${item.notDoneMinutes},$notesEscaped")
        }

        // Summary row
        val totalPlanned = items.sumOf { it.plannedMinutes }
        val totalDone = items.sumOf { it.doneMinutes }
        val totalNotDone = items.sumOf { it.notDoneMinutes }
        sb.appendLine()
        sb.appendLine("RIASSUNTO,,,TOTALE_ATTIVITA,${items.size},,TOTALE_MINUTI,,$totalPlanned,,$totalDone,$totalNotDone,")

        return sb.toString()
    }

    fun shareText(context: Context, subject: String, text: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(sendIntent, "Condividi Report Sintetico")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossibile condividere: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareCsvFile(context: Context, fileName: String, csvContent: String): Boolean {
        return try {
            val exportsDir = File(context.cacheDir, "exports")
            if (!exportsDir.exists()) {
                exportsDir.mkdirs()
            }
            val file = File(exportsDir, fileName)
            FileWriter(file).use { it.write(csvContent) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Statistiche Routine - $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Esporta File CSV Statistiche")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: share as plain text
            shareText(context, "Esportazione Statistiche CSV", csvContent)
            false
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Report sintetico copiato negli appunti!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Errore nella copia: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
