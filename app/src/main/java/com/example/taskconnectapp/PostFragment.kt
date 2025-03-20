package com.example.taskconnectapp

import com.google.android.material.bottomnavigation.BottomNavigationView
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class PostFragment : Fragment() {
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var requirementEditText: EditText
    private lateinit var budgetEditText: EditText
    private lateinit var postTaskButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_posting_page, container, false)

        // Initialize UI elements
        titleEditText = view.findViewById(R.id.question_txt)
        descriptionEditText = view.findViewById(R.id.descriptionText)
        contactEditText = view.findViewById(R.id.contact_number)
        locationEditText = view.findViewById(R.id.location)
        requirementEditText = view.findViewById(R.id.requirementText)
        budgetEditText = view.findViewById(R.id.budgetText)
        postTaskButton = view.findViewById(R.id.postTaskButton)

        // Submit task functionality
        postTaskButton.setOnClickListener {
            validateAndPostTask()
        }

        return view
    }

    private fun validateAndPostTask() {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val contact = contactEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        val requirement = requirementEditText.text.toString().trim()
        val budgetText = budgetEditText.text.toString().trim()
        val budget = if (budgetText.isEmpty()) "0.00" else budgetText

        // Check for empty fields
        if (title.isEmpty() || description.isEmpty() || contact.isEmpty() || location.isEmpty() || budgetText.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        postTaskToServer(title, description, contact, location, requirement, budget)
    }

    private fun postTaskToServer(title: String, description: String, contact: String, location: String, requirement: String, budget: String) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Posting task...")
        progressDialog.show()

        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("title", title)
            .add("descript", description)
            .add("contact_number", contact)
            .add("location", location)
            .add("requirements", requirement)
            .add("budget", budget)
            .build()

        val request = Request.Builder()
            .url("http://10.0.2.2/taskconnect/post_task.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Failed to post task: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    progressDialog.dismiss()
                    val responseData = response.body?.string()
                    val jsonObject = JSONObject(responseData ?: "{}")

                    if (jsonObject.optBoolean("success", false)) {
                        Toast.makeText(requireContext(), "Task posted successfully!", Toast.LENGTH_SHORT).show()

                        // Update Bottom Navigation Selection
                        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.btmNavView)
                        bottomNav.selectedItemId = R.id.home_btn  // Replace with the actual ID of the Home menu item

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, HomeFragment())
                            .commit()


                        // Navigate back to HomeFragment using popBackStack()
                        parentFragmentManager.popBackStack()
                    } else {
                        val errorMsg = jsonObject.optString("message", "Unknown error")
                        Toast.makeText(requireContext(), "Failed to post task: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
