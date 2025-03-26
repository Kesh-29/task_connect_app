package com.example.taskconnectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ✅ Fix: Pass context when initializing TaskAdapter
        taskAdapter = TaskAdapter(requireContext(), taskList)
        recyclerView.adapter = taskAdapter

        fetchTasks()

        return view
    }

    private fun fetchTasks() {
        val url = "http://10.0.2.2/taskconnect/get_task.php"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!isAdded) return
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to fetch tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!isAdded) return

                response.body?.let { responseBody ->
                    val json = responseBody.string()
                    println("JSON Response: $json")

                    val jsonArray = JSONArray(json)

                    taskList.clear()
                    for (i in 0 until jsonArray.length()) {
                        val taskObj = jsonArray.getJSONObject(i)
                        val task = Task(
                            title = taskObj.getString("title"),
                            postedBy = taskObj.optString("postedBy", "Unknown"),
                            budget = taskObj.optString("budget", "PHP 0.00"),
                            taskId = taskObj.getString("task_id") // Ensure taskId is a String
                        )
                        taskList.add(task)
                    }

                    requireActivity().runOnUiThread {
                        if (!isAdded) return@runOnUiThread
                        taskAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}
