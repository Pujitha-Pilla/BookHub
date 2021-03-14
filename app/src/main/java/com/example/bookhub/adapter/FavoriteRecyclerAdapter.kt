package com.example.bookhub.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookhub.R
import com.example.bookhub.database.BookEntity
import com.squareup.picasso.Picasso

class FavoriteRecyclerAdapter(val context: Context, val bookList : List<BookEntity>) : RecyclerView.Adapter<FavoriteRecyclerAdapter.FavoriteViewHolder>(){
    class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val txtBookName : TextView = view.findViewById(R.id.txt_bookName)
        val txtBookAuthor : TextView = view.findViewById(R.id.txt_bookAuthor)
        val txtBookPrice : TextView = view.findViewById(R.id.txt_bookPrice)
        val txtBookRating : TextView = view.findViewById(R.id.txt_bookRating)
        val imgBookImage : ImageView = view.findViewById(R.id.img_bookImage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_favorite_single_row,parent,false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val book = bookList[position]
        holder.txtBookName.text = book.bookName
        holder.txtBookAuthor.text = book.bookAuthor
        holder.txtBookPrice.text = book.bookPrice
        holder.txtBookRating.text = book.bookRating
        Picasso.get().load(book.bookImage).error(R.drawable.default_book_cover).into(holder.imgBookImage)

    }

    override fun getItemCount(): Int {
        return bookList.size
    }
}