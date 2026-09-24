package com.example.gemini

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiChatService {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiChatService"
        // Using supported model per gemini-api skill for general conversation tasks
        private const val MODEL = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

        const val SYSTEM_INSTRUCTION = """
Sei 'FocusCoach', un assistente e coach personale gentile, accogliente ed empatico dedicato ad aiutare l'utente a riprendere il controllo della propria attenzione, concentrazione e routine di vita.

Il tuo ruolo e principi guida:
1. Atteggiamento: Sii sempre caloroso, incoraggiante, paziente e mai giudicante. Comprendi che perdere l'attenzione o procrastinare è un'esperienza umana comune causata dai ritmi moderni e dalla sovrastimolazione.
2. Fonti affidabili e scientifiche: Fornisci consigli basati su evidenze di neuroscienze e psicologia comportamentale (ad es. Deep Work di Cal Newport, Attentional Control Theory, dopamina e reset dei circuiti di ricompensa, Tecnica del Pomodoro di Cirillo, abitudini progressive di James Clear, respirazione fisiologica e mindfulness).
3. Chiarezza e spiegazioni: Spiega i meccanismi dell'attenzione in modo semplice ma rigoroso, chiarendo ogni dubbio o domanda con esempi pratici applicabili nella routine quotidiana.
4. Azioni concrete: Guida l'utente con piccoli passi sostenibili, suggerendo come incastrare le sessioni di concentrazione e le pause di recupero nei 7 giorni della settimana.
5. Lingua: Rispondi sempre in lingua italiana, usando una formattazione chiara e piacevole con elenchi puntati o paragrafi brevi per agevolare la lettura.
"""
    }

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException(
                    "Chiave API Gemini mancante o non configurata. Inserisci la tua GEMINI_API_KEY nel pannello Secrets di AI Studio per abilitare la chat con il Focus Coach."
                )
            )
        }

        // Build multi-turn contents
        val contents = mutableListOf<GeminiContent>()

        // Add previous history turns (limiting to last 10 messages for optimal context)
        val recentHistory = history.takeLast(10)
        for (msg in recentHistory) {
            if (!msg.isError && msg.text.isNotBlank()) {
                val role = if (msg.isUser) "user" else "model"
                contents.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }
        }

        // Add current user prompt
        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userMessage))
            )
        )

        val geminiRequest = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiSystemInstruction(
                parts = listOf(GeminiPart(text = SYSTEM_INSTRUCTION.trimIndent()))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7f,
                topP = 0.95f,
                maxOutputTokens = 2048
            )
        )

        val jsonBody = requestAdapter.toJson(geminiRequest)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val url = "$BASE_URL?key=$apiKey"
        val httpRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        try {
            val response = client.newCall(httpRequest).execute()
            val bodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "API call failed (${response.code}): $bodyString")
                val errorMsg = try {
                    val errorObj = responseAdapter.fromJson(bodyString)
                    errorObj?.error?.message ?: "Errore del server Gemini (${response.code})"
                } catch (e: Exception) {
                    "Errore di connessione a Gemini (${response.code})"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val geminiResponse = responseAdapter.fromJson(bodyString)
            val replyText = geminiResponse?.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            if (replyText.isNullOrBlank()) {
                Result.failure(Exception("Nessuna risposta generata dal modello."))
            } else {
                Result.success(replyText.trim())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception calling Gemini", e)
            Result.failure(Exception("Impossibile contattare Gemini: ${e.localizedMessage ?: "Verifica la connessione ad internet"}"))
        }
    }
}
