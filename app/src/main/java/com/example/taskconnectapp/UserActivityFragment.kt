package com.example.taskconnectapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class UserActivityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private lateinit var tabLayout: TabLayout
    private lateinit var btnRegisterTasker: Button
    private lateinit var txtNoTasks: TextView

    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_activity, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewUserTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        tabLayout = view.findViewById(R.id.tabLayout)
        btnRegisterTasker = view.findViewById(R.id.btnRegisterTasker)
        txtNoTasks = view.findViewById(R.id.txtNoTasks)

        userId = getUserIdFromSession()

        taskAdapter = TaskAdapter(requireContext(), taskList)
        recyclerView.adapter = taskAdapter

        // ✅ Make task items clickable
        taskAdapter.setOnItemClickListener { task ->
            val intent = Intent(requireContext(), TaskDetailsActivity::class.java)
            intent.putExtra("task_id", task.taskId)
            startActivity(intent)
        }

        // ✅ Refresh tasks when switching tabs
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                fetchTasks()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        fetchTasks()
        return view
    }

    private fun fetchTasks() {
        if (userId == null) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            Log.e("UserActivityFragment", "User ID is null")
            return
        }

        val selectedTab = tabLayout.selectedTabPosition
        val type = if (selectedTab == 0) "posted" else "accepted"
        val url = "http://10.0.2.2/taskconnect/get_user_tasks.php?users_id=$userId&type=$type"

        Log.d("UserActivityFragment", "Fetching tasks from: $url")

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UserActivityFragment", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()

                if (jsonString.isNullOrEmpty()) {
                    Log.e("UserActivityFragment", "Empty response from server")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Empty response from server", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                try {
                    Log.d("UserActivityFragment", "Response: $jsonString")

                    val jsonObject = JSONObject(jsonString)

                    if (!jsonObject.optBoolean("success", false)) {
                        val message = jsonObject.optString("message", "Unknown error")
                        Log.e("UserActivityFragment", "Server error: $message")
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                        }
                        return
                    }

                    val isTasker = jsonObject.optInt("is_tasker", 0)
                    requireActivity().runOnUiThread {
                        btnRegisterTasker.visibility = if (selectedTab == 1 && isTasker == 0) View.VISIBLE else View.GONE
                    }

                    val tasksArray: JSONArray? = jsonObject.optJSONArray("tasks")
                    taskList.clear()

                    if (tasksArray != null) {
                        for (i in 0 until tasksArray.length()) {
                            val taskObj = tasksArray.getJSONObject(i)

                            val task = Task(
                                taskId = taskObj.getString("task_id"),
                                title = taskObj.getString("title"),
                                postedBy = if (selectedTab == 0) "You" else taskObj.optString("postedBy", "Unknown"),
                                budget = taskObj.optString("budget", "PHP 0.00")
                            )
                            taskList.add(task)
                        }
                    }

                    requireActivity().runOnUiThread {
                        if (taskList.isEmpty()) {
                            txtNoTasks.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            txtNoTasks.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE

                            taskAdapter.notifyDataSetChanged()
                        }
                    }

                } catch (e: JSONException) {
                    Log.e("UserActivityFragment", "JSON Error: ${e.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "JSON Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun getUserIdFromSession(): String? {
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val storedUserId = sharedPreferences.getInt("users_id", -1)

        return if (storedUserId != -1) storedUserId.toString() else null
    }
}
