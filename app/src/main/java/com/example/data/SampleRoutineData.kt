package com.example.data

object SampleRoutineData {
    fun getStarterWeeklyRoutine(): List<RoutineItem> {
        val list = mutableListOf<RoutineItem>()

        // Lunedì
        list.add(
            RoutineItem(
                title = "Meditazione & Risveglio",
                notes = "10 min respirazione e stretching leggero",
                type = RoutineType.EVENT,
                dayOfWeek = 1,
                startHour = 7,
                startMinute = 0,
                endHour = 7,
                endMinute = 30,
                category = RoutineCategory.WELLNESS,
                colorHex = "#06B6D4",
                executionStatus = ExecutionStatus.COMPLETED,
                actualMinutesSpent = 30,
                isCompleted = true
            )
        )
        list.add(
            RoutineItem(
                title = "Focus Studio / Lavoro Mattina",
                notes = "Sessione di lavoro profondo senza notifiche",
                type = RoutineType.EVENT,
                dayOfWeek = 1,
                startHour = 9,
                startMinute = 0,
                endHour = 12,
                endMinute = 30,
                category = RoutineCategory.WORK,
                colorHex = "#3B82F6",
                executionStatus = ExecutionStatus.COMPLETED,
                actualMinutesSpent = 180, // 3 ore fatte su 3h 30m
                isCompleted = true
            )
        )
        list.add(
            RoutineItem(
                title = "Pianificare gli obiettivi settimanali",
                notes = "Definire le 3 priorità principali della settimana",
                type = RoutineType.REMINDER,
                dayOfWeek = 1,
                startHour = 8,
                startMinute = 30,
                priority = RoutinePriority.HIGH,
                category = RoutineCategory.PERSONAL,
                colorHex = "#EC4899",
                executionStatus = ExecutionStatus.COMPLETED,
                isCompleted = true
            )
        )
        list.add(
            RoutineItem(
                title = "Allenamento Upper Body & Corsa",
                notes = "Sessione pesi 45m + 20m cardio",
                type = RoutineType.EVENT,
                dayOfWeek = 1,
                startHour = 18,
                startMinute = 0,
                endHour = 19,
                endMinute = 15,
                category = RoutineCategory.FITNESS,
                colorHex = "#10B981",
                executionStatus = ExecutionStatus.MISSED,
                actualMinutesSpent = 0,
                isCompleted = false
            )
        )
        list.add(
            RoutineItem(
                title = "Bere almeno 2L d'acqua",
                notes = "Monitorare l'idratazione giornaliera",
                type = RoutineType.REMINDER,
                dayOfWeek = 1,
                priority = RoutinePriority.MEDIUM,
                category = RoutineCategory.WELLNESS,
                colorHex = "#06B6D4",
                isCompleted = false
            )
        )

        // Martedì
        list.add(
            RoutineItem(
                title = "Lavoro & Meeting di Team",
                notes = "Revisione progetti settimanali",
                type = RoutineType.EVENT,
                dayOfWeek = 2,
                startHour = 9,
                startMinute = 30,
                endHour = 13,
                endMinute = 0,
                category = RoutineCategory.WORK,
                colorHex = "#3B82F6"
            )
        )
        list.add(
            RoutineItem(
                title = "Lettura 30 pagine libro",
                notes = "Saggio di crescita personale o narrativa",
                type = RoutineType.REMINDER,
                dayOfWeek = 2,
                startHour = 21,
                startMinute = 30,
                priority = RoutinePriority.LOW,
                category = RoutineCategory.HOBBY,
                colorHex = "#8B5CF6",
                isCompleted = false
            )
        )
        list.add(
            RoutineItem(
                title = "Passeggiata serale rigenerante",
                notes = "40 minuti all'aria aperta",
                type = RoutineType.EVENT,
                dayOfWeek = 2,
                startHour = 19,
                startMinute = 0,
                endHour = 19,
                endMinute = 45,
                category = RoutineCategory.WELLNESS,
                colorHex = "#06B6D4"
            )
        )

        // Mercoledì
        list.add(
            RoutineItem(
                title = "Sessione Palestra / Gambe & Core",
                notes = "Riscaldamento accurato",
                type = RoutineType.EVENT,
                dayOfWeek = 3,
                startHour = 7,
                startMinute = 30,
                endHour = 8,
                endMinute = 45,
                category = RoutineCategory.FITNESS,
                colorHex = "#10B981"
            )
        )
        list.add(
            RoutineItem(
                title = "Sviluppo Progetto Personale",
                notes = "Coding / Creazione contenuti",
                type = RoutineType.EVENT,
                dayOfWeek = 3,
                startHour = 15,
                startMinute = 0,
                endHour = 17,
                endMinute = 30,
                category = RoutineCategory.STUDY,
                colorHex = "#6366F1"
            )
        )
        list.add(
            RoutineItem(
                title = "Fare la spesa sana per la seconda metà settimana",
                notes = "Frutta, verdura fresca e snack leggeri",
                type = RoutineType.REMINDER,
                dayOfWeek = 3,
                startHour = 18,
                startMinute = 30,
                priority = RoutinePriority.HIGH,
                category = RoutineCategory.HOME,
                colorHex = "#F59E0B",
                isCompleted = false
            )
        )

        // Giovedì
        list.add(
            RoutineItem(
                title = "Focus Lavoro & Analisi",
                notes = "Report e riepilogo attività",
                type = RoutineType.EVENT,
                dayOfWeek = 4,
                startHour = 10,
                startMinute = 0,
                endHour = 13,
                endMinute = 0,
                category = RoutineCategory.WORK,
                colorHex = "#3B82F6"
            )
        )
        list.add(
            RoutineItem(
                title = "Pratica Strumento / Hobby Creativo",
                notes = "Dedica tempo alle tue passioni",
                type = RoutineType.EVENT,
                dayOfWeek = 4,
                startHour = 18,
                startMinute = 30,
                endHour = 19,
                endMinute = 30,
                category = RoutineCategory.HOBBY,
                colorHex = "#8B5CF6"
            )
        )
        list.add(
            RoutineItem(
                title = "Backup file importanti e pulizia desktop",
                notes = "Organizzare la cartella download",
                type = RoutineType.REMINDER,
                dayOfWeek = 4,
                priority = RoutinePriority.MEDIUM,
                category = RoutineCategory.PERSONAL,
                colorHex = "#EC4899",
                isCompleted = false
            )
        )

        // Venerdì
        list.add(
            RoutineItem(
                title = "Chiusura Task Settimanali",
                notes = "Nessun task in sospeso prima del weekend",
                type = RoutineType.EVENT,
                dayOfWeek = 5,
                startHour = 9,
                startMinute = 0,
                endHour = 12,
                endMinute = 0,
                category = RoutineCategory.WORK,
                colorHex = "#3B82F6"
            )
        )
        list.add(
            RoutineItem(
                title = "Workout Full Body",
                notes = "Ultima sessione di allenamento intenso",
                type = RoutineType.EVENT,
                dayOfWeek = 5,
                startHour = 17,
                startMinute = 30,
                endHour = 18,
                endMinute = 45,
                category = RoutineCategory.FITNESS,
                colorHex = "#10B981"
            )
        )
        list.add(
            RoutineItem(
                title = "Pizzata o Uscita con Amici",
                notes = "Staccare completamente la mente",
                type = RoutineType.EVENT,
                dayOfWeek = 5,
                startHour = 20,
                startMinute = 30,
                endHour = 23,
                endMinute = 0,
                category = RoutineCategory.HOBBY,
                colorHex = "#8B5CF6"
            )
        )
        list.add(
            RoutineItem(
                title = "Inviare riepilogo venerdì al team",
                notes = "Email di aggiornamento",
                type = RoutineType.REMINDER,
                dayOfWeek = 5,
                startHour = 16,
                startMinute = 30,
                priority = RoutinePriority.HIGH,
                category = RoutineCategory.WORK,
                colorHex = "#3B82F6",
                isCompleted = false
            )
        )

        // Sabato
        list.add(
            RoutineItem(
                title = "Corsa all'aperto / Trekking",
                notes = "Percorso nel verde",
                type = RoutineType.EVENT,
                dayOfWeek = 6,
                startHour = 9,
                startMinute = 0,
                endHour = 10,
                endMinute = 30,
                category = RoutineCategory.FITNESS,
                colorHex = "#10B981"
            )
        )
        list.add(
            RoutineItem(
                title = "Pulizia profonda casa & bucato",
                notes = "Cambio lenzuola e riordino generale",
                type = RoutineType.REMINDER,
                dayOfWeek = 6,
                startHour = 11,
                startMinute = 0,
                priority = RoutinePriority.MEDIUM,
                category = RoutineCategory.HOME,
                colorHex = "#F59E0B",
                isCompleted = false
            )
        )
        list.add(
            RoutineItem(
                title = "Serata Cinema & Relax",
                notes = "Film o serie tv preferita",
                type = RoutineType.EVENT,
                dayOfWeek = 6,
                startHour = 21,
                startMinute = 0,
                endHour = 23,
                endMinute = 15,
                category = RoutineCategory.HOBBY,
                colorHex = "#8B5CF6"
            )
        )

        // Domenica
        list.add(
            RoutineItem(
                title = "Brunch o Colazione con Calma",
                notes = "Pancake e caffè speciale senza fretta",
                type = RoutineType.EVENT,
                dayOfWeek = 7,
                startHour = 10,
                startMinute = 0,
                endHour = 11,
                endMinute = 30,
                category = RoutineCategory.WELLNESS,
                colorHex = "#06B6D4"
            )
        )
        list.add(
            RoutineItem(
                title = "Meal Prep per i pasti della settimana",
                notes = "Cucinare in anticipo per risparmiare tempo",
                type = RoutineType.REMINDER,
                dayOfWeek = 7,
                startHour = 17,
                startMinute = 0,
                priority = RoutinePriority.HIGH,
                category = RoutineCategory.HOME,
                colorHex = "#F59E0B",
                isCompleted = false
            )
        )
        list.add(
            RoutineItem(
                title = "Riflessione settimanale e reset mentale",
                notes = "Cosa è andato bene e cosa migliorare la prossima settimana",
                type = RoutineType.REMINDER,
                dayOfWeek = 7,
                startHour = 20,
                startMinute = 30,
                priority = RoutinePriority.MEDIUM,
                category = RoutineCategory.PERSONAL,
                colorHex = "#EC4899",
                isCompleted = false
            )
        )

        return list
    }
}
