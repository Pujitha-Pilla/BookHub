package com.example.bookhub.activity

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.database.BookDatabase
import com.example.bookhub.database.BookEntity
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {

    lateinit var bookImage : ImageView
    lateinit var bookName : TextView
    lateinit var bookAuthor : TextView
    lateinit var bookPrice : TextView
    lateinit var bookRating : TextView
    lateinit var bookAbout : TextView
    lateinit var bookDesc : TextView
    lateinit var addToFav : Button
    lateinit var progressBar : ProgressBar
    lateinit var  progressBarLayout : RelativeLayout

    lateinit var toolbar: Toolbar

    var bookID: String? = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        bookImage = findViewById(R.id.img_bookImage)
        bookName = findViewById(R.id.txt_bookName)
        bookAuthor = findViewById(R.id.txt_bookAuthor)
        bookPrice = findViewById(R.id.txt_bookPrice)
        bookRating = findViewById(R.id.txt_bookRating)
        progressBar = findViewById(R.id.progressBar)
        bookAbout = findViewById(R.id.txtAboutBookTitle)
        bookDesc = findViewById(R.id.txtBookDesc)
        progressBarLayout = findViewById(R.id.rlProgress)
        progressBarLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        addToFav = findViewById(R.id.btnAddToFav)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"

        if(intent != null){
            bookID = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(this,"Some unexpected error", Toast.LENGTH_SHORT).show()
        }
        if(bookID == "100"){
            finish()
            Toast.makeText(this,"Some unexpected error", Toast.LENGTH_SHORT).show()
        }

        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v1/book/get_book/"
        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookID)

        val jsonRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {
            try{
                val success = it.getBoolean("success")
                if(success){
                    val bookData = it.getJSONObject("book_data")
                    progressBarLayout.visibility = View.GONE
                    progressBar.visibility = View.GONE

                    val bookImageUrl = bookData.getString("image")
                    bookName.text = bookData.getString("name")
                    bookAuthor.text = bookData.getString("author")
                    bookPrice.text = bookData.getString("price")
                    bookRating.text = bookData.getString("rating")
                    Picasso.get().load(bookImageUrl).error(R.drawable.default_book_cover).into(bookImage)
                    bookDesc.text = bookData.getString("description")


                    val bookEntity = BookEntity(
                    bookID?.toInt() as Int,
                    bookName.text.toString(),
                    bookAuthor.text.toString(),
                    bookPrice.text.toString(),
                    bookRating.text.toString(),
                    bookDesc.text.toString(),
                    bookImageUrl
                    )

                    val checkFav = DBAsyncTask(applicationContext,bookEntity,1).execute()
                    val isFav = checkFav.get()

                    if(isFav){
                        addToFav.text = "Remove from Favorites"
                        val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavorite)
                        addToFav.setBackgroundColor(favColor)
                    } else {
                        addToFav.text = "Add to Favorties"
                        val noFavColor = ContextCompat.getColor(applicationContext,R.color.colorPrimary)
                        addToFav.setBackgroundColor(noFavColor)
                    }

                    addToFav.setOnClickListener {
                            if(!DBAsyncTask(applicationContext,bookEntity,1).execute().get()){
                            val async = DBAsyncTask(applicationContext,bookEntity,2).execute()
                            val result = async.get()
                            if(result){
                                Toast.makeText(this,"Book added to Favorites", Toast.LENGTH_SHORT).show()
                                addToFav.text = "Remove from Favorites"
                                val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavorite)
                                addToFav.setBackgroundColor(favColor)
                            } else {
                                Toast.makeText(this,"Error Occurred! Try again", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val async = DBAsyncTask(applicationContext,bookEntity,3).execute()
                            val result = async.get()
                            if(result){
                                Toast.makeText(this,"Book removed to Favorites", Toast.LENGTH_SHORT).show()
                                addToFav.text = "Add to Favorites"
                                val nofavColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                addToFav.setBackgroundColor(nofavColor)
                            } else {
                                Toast.makeText(this,"Error Occurred! Try again", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                } else {
                    Toast.makeText(this, "The response is a failure",Toast.LENGTH_SHORT).show()
                }
            } catch (e:JSONException){
                Toast.makeText(this, "Raised exception while getting the data",Toast.LENGTH_SHORT).show()
            }
        }, Response.ErrorListener {
                Toast.makeText(this,"Volley Error $it", Toast.LENGTH_SHORT).show()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["token"] = "7db9f2ec963b7d"
                return headers
            }
        }
        queue.add(jsonRequest)

    }

    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode:Int) : AsyncTask<Void, Void, Boolean>(){
        /* Mode 1- check DB if the book is favorite or not
        Mode 2 - Save the book into DB as favorite
        Mode 3 - Remove the book from favorites
         */
        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {

            when(mode){
                1-> {
                    val book : BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }

                2-> {
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }

                3->{
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }

            return false
        }
    }
}