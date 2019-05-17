package com.nkrause.jobox

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import java.sql.Time
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ArticleViewHolder(inflater: LayoutInflater, parent: ViewGroup, viewType: Int) :
    RecyclerView.ViewHolder(inflater.inflate(
    when(viewType){
        1 -> R.layout.article_layout_1
        2 -> R.layout.article_layout_2
        3 -> R.layout.article_layout_3
        else -> R.layout.article_layout_1
    }, parent, false)) {

    private var titleView: TextView? = null
    private var descriptionView: TextView? = null
    private var imageView: ImageView
    private var myParent = parent
    private var myViewType = viewType

    init {
        titleView = itemView.findViewById(R.id.article_title)
        descriptionView = itemView.findViewById(R.id.article_description)
        imageView = itemView.findViewById(R.id.article_image)
    }

    fun bind(article: Article) {
        var myTitle = article.title

        if(myViewType == 1){
            myTitle = myTitle.plus(" - ").plus(getFormattedDate(article.dateStamp)).plus(" UTC")
        }
        titleView?.text = myTitle

        //some descriptions are null
        if(article.description != "null") {
            descriptionView?.text = article.description
        }

        //format URL to match https://* for Glide
        if(article.imageURL != "null"){
            if(article.imageURL.startsWith("//")){
                article.imageURL = "https://".plus(article.imageURL.substring(2))
            }
            else if(article.imageURL.startsWith("www")){
                article.imageURL = "https://".plus(article.imageURL)
            }
            else if(article.imageURL.startsWith("http://")){
                article.imageURL = article.imageURL.replace("http://","https://")
            }
            Glide.with(myParent.context).load(article.imageURL).into(imageView);
        }
    }

    private fun getFormattedDate(s: String): String? {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("M/d/yyy HH:mm", Locale.getDefault())
        val date = LocalDateTime.parse(s, inputFormatter)
        return outputFormatter.format(date)
    }
}
