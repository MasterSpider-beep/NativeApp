package com.example.nativeapp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nativeapp.domain.Book

class BookAdapter(private val context: Context, private val books: List<Book>, private val onItemClick: (Book) -> Unit) :
    RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

        private var isShaking = false;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.title.text = book.title
        holder.author.text = book.author

        holder.itemView.setOnClickListener {
            onItemClick(book)
        }

        if (isShaking) {
            val shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_animation)
            holder.itemView.startAnimation(shakeAnimation)
        }
    }

    fun triggerShakeAnimation() {
        isShaking = true
        notifyDataSetChanged() // Refresh the adapter to apply animation
        Handler(Looper.getMainLooper()).postDelayed({
            isShaking = false
        }, 500)
    }

    override fun getItemCount(): Int = books.size

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.bookTitle)
        val author: TextView = itemView.findViewById(R.id.bookAuthor)
    }
}