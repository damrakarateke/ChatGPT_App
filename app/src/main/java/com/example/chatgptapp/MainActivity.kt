package com.example.chatgptapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // --- Backend-URL ---
    // Emulator:
    private val backendUrl = "http://10.0.2.2:3000/chat"
    // Echtes Handy (Beispiel): private val backendUrl = "http://192.168.178.20:3000/chat"

    // Optional: App-Auth-Header, falls im Server (.env) gesetzt
    private val appAuthToken: String? = "change-me" // oder null, wenn nicht genutzt

    // HTTP-Client
    private val client = OkHttpClient()

    // UI
    private lateinit var etQuestion: TextInputEditText
    private lateinit var btnSend: Button
    private lateinit var idTVQuestion: TextView
    private lateinit var txtResponse: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etQuestion = findViewById(R.id.etQuestion)
        btnSend = findViewById(R.id.btnSend)
        idTVQuestion = findViewById(R.id.idTVQuestion)
        txtResponse = findViewById(R.id.txtResponse)

        // Button klick
        btnSend.setOnClickListener { sendQuestion() }

        // Enter/Done auf Tastatur ebenfalls senden
        etQuestion.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                sendQuestion()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun sendQuestion() {
        val question = etQuestion.text?.toString()?.trim().orEmpty()
        if (question.isEmpty()) {
            Toast.makeText(this, "Bitte Frage eingeben", Toast.LENGTH_SHORT).show()
            return
        }

        txtResponse.text = "Bitte warten …"
        getResponse(question) { response ->
            runOnUiThread { txtResponse.text = response }
        }
    }

    /**
     * Ruft deinen Proxy (POST /chat) auf und erwartet { "reply": "..." }.
     * Kein OpenAI-Key in der App!
     */
    private fun getResponse(question: String, callback: (String) -> Unit) {
        idTVQuestion.text = question
        etQuestion.setText("")

        val payload = JSONObject().apply { put("message", question) }

        val reqBuilder = Request.Builder()
            .url(backendUrl)
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))

        // optionaler App-Auth-Header
        appAuthToken?.let { reqBuilder.addHeader("X-App-Auth", it) }

        val request = reqBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API", "Proxy request failed", e)
                runOnUiThread { callback("Netzwerkfehler: ${e.localizedMessage ?: "unbekannt"}") }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string().orEmpty()
                    if (!it.isSuccessful) {
                        Log.e("API", "HTTP ${it.code}: $body")
                        runOnUiThread { callback("Proxy-Fehler (${it.code}): ${body.take(200)}") }
                        return
                    }
                    try {
                        val json = JSONObject(body)
                        val content = json.optString("reply", "").trim()
                        runOnUiThread { callback(if (content.isNotEmpty()) content else "Leere Antwort vom Server.") }
                    } catch (ex: Exception) {
                        Log.e("API", "Parse error: $body", ex)
                        runOnUiThread { callback("Antwort konnte nicht gelesen werden: ${ex.localizedMessage}") }
                    }
                }
            }
        })
    }
}
