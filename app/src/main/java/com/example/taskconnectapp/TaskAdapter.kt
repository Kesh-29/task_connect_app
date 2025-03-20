package com.example.taskconnectapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(private val taskList: List<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.taskTitle)
        val nameTextView: TextView = view.findViewById(R.id.taskPostedBy)
        val budgetTextView: TextView = view.findViewById(R.id.taskBudget)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.titleTextView.text = task.title
        holder.nameTextView.text = "Posted by: ${task.postedBy}" // ✅ FIXED: Use postedBy
        holder.budgetTextView.text = task.budget // ✅ No need to add "PHP", it's already formatted in get_tasks.php
    }

    override fun getItemCount() = taskList.size
}
