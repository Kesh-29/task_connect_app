package com.example.taskconnectapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.content.Intent

class ProfileFragment : Fragment() {
    private lateinit var ivUserProfile: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvJoinedDate: TextView
    private lateinit var tvUserLocation: TextView
    private lateinit var tvUserPhoneNumber: TextView
    private lateinit var logoutButton: Button
    private lateinit var registrationButton: Button
    private lateinit var tabLayoutProfile: TabLayout
    private lateinit var userInfoLayout: LinearLayout
    private lateinit var rvTaskHistory: RecyclerView
    private lateinit var taskHistoryAdapter: TaskHistoryAdapter
    private lateinit var profileBtnGroup: LinearLayout
    private lateinit var questionText: TextView
    private lateinit var isTasker: TextView
    private lateinit var tvNoCompletedTasks: TextView
    private val taskHistoryList = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_citizen_profile_page, container, false)

        // Find UI elements
        tvNoCompletedTasks = view.findViewById(R.id.tvNoCompletedTasks) // Reference TextView
        ivUserProfile = view.findViewById(R.id.ivUserProfile)
        tvUserName = view.findViewById(R.id.TvUserNameValue)
        tvJoinedDate = view.findViewById(R.id.joinedDate)
        tvUserLocation = view.findViewById(R.id.UserLocation)
        tvUserPhoneNumber = view.findViewById(R.id.UserPhoneNumber)
        registrationButton = view.findViewById(R.id.Register_Button)
        logoutButton = view.findViewById(R.id.logoutbtn)
        tabLayoutProfile = view.findViewById(R.id.tabLayout) // Reference TabLayout
        userInfoLayout = view.findViewById(R.id.UserInformation) // Reference User Info Layout
        rvTaskHistory = view.findViewById(R.id.rvTaskHistory) // Reference RecyclerView
        profileBtnGroup = view.findViewById(R.id.profileBtnGroup)
        isTasker = view.findViewById(R.id.TvIsTasker)
        questionText = view.findViewById(R.id.questionText)

        // Set up RecyclerView
        rvTaskHistory.layoutManager = LinearLayoutManager(requireContext())
        taskHistoryAdapter = TaskHistoryAdapter(taskHistoryList)
        rvTaskHistory.adapter = taskHistoryAdapter

        // Get user ID from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("users_id", 0)

        if (userId > 0) {
            fetchUserProfile(userId)
        } else {
            Log.e("ProfileFragment", "Invalid user ID")
        }

        tabLayoutProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        userInfoLayout.visibility = View.VISIBLE
                        rvTaskHistory.visibility = View.GONE
                        profileBtnGroup.visibility = View.VISIBLE
                        tvNoCompletedTasks.visibility = View.GONE
                    }
                    1 -> {
                        userInfoLayout.visibility = View.GONE
                        rvTaskHistory.visibility = View.VISIBLE
                        profileBtnGroup.visibility = View.GONE

                        val sharedPref = requireActivity().getSharedPreferences(
                            "UserPrefs",
                            Context.MODE_PRIVATE
                        )
                        val userId = sharedPref.getInt("users_id", 0)
                        if (userId > 0) {
                            fetchCompletedTasks(userId)
                        } else {
                            Log.e("ProfileFragment", "Invalid user ID")
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Logout button functionality
        logoutButton.setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(requireActivity(), LoginPage::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        registrationButton.setOnClickListener {
            val intent = Intent(requireActivity(), RegistrationFormActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun fetchUserProfile(userId: Int) {
        val url = "http://10.0.2.2/taskconnect/get_user_profile.php?users_id=$userId"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ProfileFragment", "Failed to fetch profile: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                Log.d("ProfileFragment", "Response: $jsonString")

                try {
                    val jsonObject = JSONObject(jsonString)

                    requireActivity().runOnUiThread {
                        if (jsonObject.getBoolean("success")) {
                            val user = jsonObject.getJSONObject("user")

                            val firstName = user.optString("first_name", "Unknown")
                            val lastName = user.optString("last_name", "")
                            val profileImage = user.optString("profile_image", "")
                            val joinedDate = user.optString("created_at", "Unknown")
                            val rawLocation = user.optString("location", "N/A")
                            val location = if (rawLocation == "null") "N/A" else rawLocation
                            val phoneNumber = user.optString("mobile_no", "Not Available")
                            val isTasker = user.optInt("is_tasker", 0) // Get is_tasker value

                            tvUserName.text = "$firstName $lastName"
                            tvJoinedDate.text = "Joined $joinedDate"
                            tvUserLocation.text = location
                            tvUserPhoneNumber.text = phoneNumber

                            if (profileImage.isNotEmpty()) {
                                Glide.with(requireContext()).load(profileImage).into(ivUserProfile)
                            } else {
                                ivUserProfile.setImageResource(R.drawable.user_profile)
                            }

                            // Hide tabTaskHistory if the user is not a tasker
                            if (isTasker == 0) {
                                tabLayoutProfile.getTabAt(1)?.view?.visibility = View.GONE
                                view?.findViewById<TextView>(R.id.TvIsTasker)?.visibility = View.GONE
                            } else {
                                view?.findViewById<TextView>(R.id.TvIsTasker)?.visibility = View.VISIBLE
                                questionText.visibility = View.GONE
                                registrationButton.visibility = View.GONE
                            }

                        } else {
                            Log.e("ProfileFragment", "Error: ${jsonObject.optString("message")}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "JSON Parsing error: ${e.message}")
                }
            }
        })
    }

    private fun fetchCompletedTasks(userId: Int) {
        val url = "http://10.0.2.2/taskconnect/get_completed_tasks.php?users_id=$userId"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ProfileFragment", "Failed to fetch completed tasks: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                Log.d("ProfileFragment", "Completed Tasks Response: $jsonString")

                try {
                    val jsonObject = JSONObject(jsonString)

                    requireActivity().runOnUiThread {
                        if (jsonObject.getBoolean("success")) {
                            taskHistoryList.clear()
                            val tasksArray = jsonObject.getJSONArray("tasks")

                            for (i in 0 until tasksArray.length()) {
                                val taskObj = tasksArray.getJSONObject(i)
                                val task = Task(
                                    taskId = taskObj.getString("task_id"),
                                    title = taskObj.getString("title"),
                                    postedBy = taskObj.optString("postedBy", "Unknown"),
                                    budget = taskObj.getString("budget"),
                                    status = taskObj.getString("status"),
                                    acceptedBy = taskObj.optInt("accepted_by", 0),
                                    completedAt = taskObj.optString("completed_at", "Unknown") // Add date
                                )

                                if (task.acceptedBy == userId) {
                                    taskHistoryList.add(task)
                                }
                            }

                            if (taskHistoryList.isEmpty()) {
                                tvNoCompletedTasks.visibility = View.VISIBLE
                                rvTaskHistory.visibility = View.GONE
                            } else {
                                tvNoCompletedTasks.visibility = View.GONE
                                rvTaskHistory.visibility = View.VISIBLE
                            }

                            taskHistoryAdapter.notifyDataSetChanged()
                        } else {
                            Log.e("ProfileFragment", "No completed tasks found")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "JSON Parsing error: ${e.message}")
                }
            }
        })
    }


}
