package com.example.taskconnectapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskHistoryAdapter(private val taskList: List<Task>) :
    RecyclerView.Adapter<TaskHistoryAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTaskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        val tvTaskDate: TextView = itemView.findViewById(R.id.creationDate)
        val nameTextView: TextView = itemView.findViewById(R.id.taskPostedBy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_history_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTaskTitle.text = task.title
        holder.nameTextView.text = "Posted by: ${task.postedBy}"
        holder.tvTaskDate.text = "${task.completedAt}"
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}

