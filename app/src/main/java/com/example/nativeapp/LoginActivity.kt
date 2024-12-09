package com.example.nativeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        val url = "http://10.0.2.2:3000/login" // Use 10.0.2.2 for localhost in emulator

        val json = JSONObject()
        json.put("username", username)
        json.put("password", password)

        val requestBody =
            json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("login", "onFailure: " + e.message )
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Failed to connect to server" + e.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val token = JSONObject(responseBody).getString("token")
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        navigateToMainPage(token)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("NativeAppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("AUTH_TOKEN", token)
        editor.apply()
    }

    private fun navigateToMainPage(token: String) {
        saveToken(token);
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("TOKEN", token)
        startActivity(intent)
        finish()
    }
}