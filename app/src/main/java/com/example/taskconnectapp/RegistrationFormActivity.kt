package com.example.taskconnectapp

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RegistrationFormActivity : AppCompatActivity() {
    private lateinit var contactNumberTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var applyText: EditText
    private lateinit var btnFileUpload: Button
    private lateinit var btnRegister: Button
    private lateinit var selectedImagePreview: ImageView


    private val userId = 1 // Replace with actual logged-in user ID
    private val profileUrl = "http://10.0.2.2/taskconnect/get_user_profile.php?users_id=$userId" // Localhost for emulator
    private val uploadUrl = "http://10.0.2.2/taskconnect/upload_verification.php"
    private var selectedFileUri: Uri? = null
    private var selectedFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_form)

        contactNumberTextView = findViewById(R.id.contactNumber)
        locationTextView = findViewById(R.id.locationTextView)
        applyText = findViewById(R.id.ApplyText)
        btnFileUpload = findViewById(R.id.btnFileUpload)
        btnRegister = findViewById(R.id.btnRegister)
        selectedImagePreview = findViewById(R.id.selectedImagePreview)

        fetchUserProfile()

        btnFileUpload.setOnClickListener {
            openFileChooser()
        }

        btnRegister.setOnClickListener {
            if (validateInputs()) {
                uploadFile()
            }
        }

        val backButton = findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            finish() // Go back to the previous activity
        }

    }

    private fun fetchUserProfile() {
        val client = OkHttpClient()
        val request = Request.Builder().url(profileUrl).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UserProfile", "Error fetching profile: ${e.message}")
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to fetch user profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("UserProfile", "Response: $responseBody") // LOG RESPONSE

                runOnUiThread {
                    try {
                        val jsonObject = JSONObject(responseBody ?: "{}")
                        if (jsonObject.getBoolean("success")) {
                            val user = jsonObject.getJSONObject("user")
                            val mobile = user.optString("mobile_no", "N/A")
                            val rawLocation = user.optString("location", "N/A")
                            val location = if (rawLocation == "null") "N/A" else rawLocation

                            Log.d("UserProfile", "Mobile: $mobile, Location: $location") // LOG VALUES

                            contactNumberTextView.text = mobile
                            locationTextView.text = location
                        } else {
                            Toast.makeText(applicationContext, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("UserProfile", "Parsing error: ${e.message}")
                    }
                }
            }
        })
    }


    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, FILE_PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                selectedImagePreview.setImageURI(uri)
                selectedImagePreview.visibility = ImageView.VISIBLE
                selectedFilePath = getFilePathFromUri(this, uri) // Convert URI to file path
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (applyText.text.toString().trim().isEmpty()) {
            applyText.error = "Description is required"
            return false
        }
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file to upload", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun uploadFile() {
        if (selectedFilePath == null) {
            Toast.makeText(this, "Invalid file path", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(selectedFilePath!!)
        Log.d("FileUpload", "Users ID: $userId")
        Log.d("FileUpload", "Description: ${applyText.text.toString().trim()}")
        Log.d("FileUpload", "File Name: ${file.name}")
        Log.d("FileUpload", "File Path: $selectedFilePath")

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("users_id", userId.toString())
            .addFormDataPart("description", applyText.text.toString().trim())
            .addFormDataPart(
                "document", file.name,
                RequestBody.create("image/jpeg".toMediaType(), file)
            )
            .build()

        val request = Request.Builder().url(uploadUrl).post(requestBody).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FileUpload", "Upload failed: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("FileUpload", "Server Response: $responseBody")
                runOnUiThread {
                    try {
                        val jsonObject = JSONObject(responseBody ?: "{}")
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(applicationContext, "File uploaded successfully", Toast.LENGTH_SHORT).show()

                            // Redirect user to home screen
                            val intent = Intent(applicationContext, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish() // Close current activity
                        } else {
                            Toast.makeText(applicationContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("FileUpload", "Parsing error: ${e.message}")
                    }
                }
            }
        })
    }


    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val fileName = getFileName(contentResolver, uri) ?: return null
        val file = File(context.cacheDir, fileName)

        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            Log.e("FileUtils", "Error copying file: ${e.message}")
            null
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val fileName = nameIndex?.let { cursor.getString(it) }
        cursor?.close()
        return fileName
    }

    companion object {
        private const val FILE_PICK_REQUEST = 1
    }
}