package com.example.chatgptapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
<<<<<<< Updated upstream
import android.widget.Button
import android.widget.EditText
=======
import android.view.inputmethod.EditorInfo
>>>>>>> Stashed changes
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // Emulator:
    private val backendUrl = "http://10.0.2.2:3000/chat"

    // Echtes Handy:
    // private val backendUrl = "http://<deine-PC-LAN-IP>:3000/chat"


    // Ein einzelner OkHttpClient für die App
    private val client = OkHttpClient()
<<<<<<< Updated upstream
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val etQuestion=findViewById<EditText>(R.id.etQuestion)
        val btnSubmit=findViewById<Button>(R.id.btnSubmit)
        val txtResponse=findViewById<TextView>(R.id.txtResponse)

        btnSubmit.setOnClickListener {
            val question=etQuestion.text.toString().trim()
            Toast.makeText(this,question,Toast.LENGTH_SHORT).show()
            if(question.isNotEmpty()){
            getResponse(question) { response ->
                runOnUiThread {
                    txtResponse.text = response
=======

    // UI-Elemente
    private lateinit var txtResponse: TextView
    private lateinit var idTVQuestion: TextView
    private lateinit var etQuestion: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etQuestion = findViewById(R.id.etQuestion)
        idTVQuestion = findViewById(R.id.idTVQuestion)
        txtResponse = findViewById(R.id.txtResponse)

        // Senden über Tastatur-Action „Senden/Send“
        etQuestion.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val question = etQuestion.text?.toString()?.trim().orEmpty()
                if (question.isEmpty()) {
                    Toast.makeText(this, "Bitte Frage eingeben", Toast.LENGTH_SHORT).show()
                    return@OnEditorActionListener true
                }
                txtResponse.text = "Bitte warten …"
                getResponse(question) { response ->
                    runOnUiThread {
                        txtResponse.text = response
                    }
>>>>>>> Stashed changes
                }
            }
<<<<<<< Updated upstream
            }
        }
    }
    fun getResponse(question: String, callback: (String) -> Unit){
        val apiKey="Enter your api key from openai.com"
        val url="https://api.openai.com/v1/engines/text-davinci-003/completions"
=======
            false
        })
    }

    /**
     * Ruft das aktuelle Chat Completions API auf.
     * WICHTIG: Den API-Key NICHT in der App belassen. Für Demo ok,
     * in Produktion über Euer Backend proxien.
     */
    private fun getResponse(question: String, callback: (String) -> Unit) {
        idTVQuestion.text = question
        etQuestion.setText("")

        // !!! DEMO: Ersetze diesen Platzhalter für lokale Tests.
        // In Produktion: Eigener Backend-Endpoint ohne API-Key in der App!
        val apiKey = "...ENTER CODE"
        val url = "https://api.openai.com/v1/chat/completions"
>>>>>>> Stashed changes

        // JSON-Payload sauber mit JSONObject bauen (vermeidet Escape-Probleme)
        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", "You are a helpful assistant."))
            put(JSONObject().put("role", "user").put("content", question))
        }
        val payload = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
            put("temperature", 0.2)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API", "Request failed", e)
                callback("Fehler: ${e.message ?: "unbekannt"}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string().orEmpty()
                    if (!it.isSuccessful) {
                        Log.e("API", "HTTP ${it.code}: $body")
                        callback("API-Fehler (${it.code})")
                        return
                    }
                    try {
                        val root = JSONObject(body)
                        val content = root.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim()
                        callback(content)
                    } catch (ex: Exception) {
                        Log.e("API", "Parse error: $body", ex)
                        callback("Konnte die Antwort nicht lesen.")
                    }
                }
<<<<<<< Updated upstream
                else{
                    Log.v("data","empty")
                }
                val jsonObject=JSONObject(body)
                val jsonArray:JSONArray=jsonObject.getJSONArray("choices")
                val textResult=jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }
        })
    }

}
=======
            }
        })
    }
}
>>>>>>> Stashed changes
