package com.example.taskconnectapp

import android.content.Intent
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.titleTextView.text = task.title
        holder.nameTextView.text = "Posted by: ${task.postedBy}"
        holder.budgetTextView.text = task.budget

        // Handle click event
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TaskDetailsActivity::class.java).apply {
                putExtra("title", task.title)
                putExtra("postedBy", task.postedBy)
                putExtra("budget", task.budget)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = taskList.size
}
