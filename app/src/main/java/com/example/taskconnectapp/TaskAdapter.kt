package com.example.taskconnectapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(private val context: Context, private val taskList: List<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.taskTitle)
        val nameTextView: TextView = view.findViewById(R.id.taskPostedBy)
        val budgetTextView: TextView = view.findViewById(R.id.taskBudget)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.titleTextView.text = task.title
        holder.nameTextView.text = "Posted by: ${task.postedBy}"
        holder.budgetTextView.text = task.budget

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, TaskDetailsActivity::class.java)
            intent.putExtra("task_id", task.taskId) // UUID as String
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = taskList.size
}
