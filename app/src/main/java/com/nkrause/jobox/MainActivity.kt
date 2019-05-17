package com.nkrause.jobox

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import org.json.JSONObject
import android.support.v7.widget.DividerItemDecoration
import java.lang.Integer.parseInt
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        const val PREF_NAME = "jobox"
        const val PREF_ARTICLE_LIST = "article_list"
        const val NEWS_API_URL = "https://newsapi.org/v2/top-headlines?country=us&apiKey=be8e94398ebf43c1bfcd794a2ae0e488"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
        //if user is connected to wifi or data, setup the most recent article list
        //otherwise setup the article list from storage
        if(isNetworkAvailable()){
            setupArticleList()
        }
        else{
            setupOfflineArticleList()
        }

        //setup swipe to refresh
        swipeRefreshLayout.setOnRefreshListener{
            if(isNetworkAvailable()){
                setupArticleList()
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    private fun setupArticleList(){
        Thread {
            //fetch most recent articles from URL and convert to JSON
            try{
                val result = URL(NEWS_API_URL).readText()
                val jsonObject = JSONObject(result)
                val articles = jsonObject.getJSONArray("articles")
                var articleList = mutableListOf<Article>()
                //iterate over the array and create a list of Article objects
                //for each entry, randomly generate a number from 1 to 3 that will correspond to its layout
                for (i in 0 until articles.length()) {
                    val randInt = Random.nextInt(1, 4)
                    var item = articles.getJSONObject(i)
                    item.put("type",randInt)
                    var tmpArticle = Article()
                    tmpArticle.type = randInt
                    tmpArticle.title = item["title"].toString()
                    tmpArticle.description = item["description"].toString()
                    tmpArticle.imageURL = item["urlToImage"].toString()
                    tmpArticle.dateStamp = item["publishedAt"].toString()
                    articleList.add(tmpArticle)
                }

                //save article list to storage
                val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString(PREF_ARTICLE_LIST,jsonObject.toString())
                editor.apply()

                //update UI
                runOnUiThread {
                    recyclerView.adapter = ArticleAdapter(articleList)
                }
            }
            catch(e: Exception){
                println("Error:"+e.printStackTrace())
            }

        }.start()
    }

    private fun setupOfflineArticleList(){
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val storedList = sharedPref.getString(PREF_ARTICLE_LIST,"")
        if(storedList != ""){
            val jsonObject = JSONObject(sharedPref.getString(PREF_ARTICLE_LIST,""))
            val articles = jsonObject.getJSONArray("articles")
            var articleList = mutableListOf<Article>()
            for (i in 0 until articles.length()) {
                val item = articles.getJSONObject(i)
                var tmpArticle = Article()
                tmpArticle.type = parseInt(item["type"].toString())
                tmpArticle.title = item["title"].toString()
                tmpArticle.description = item["description"].toString()
                tmpArticle.imageURL = item["urlToImage"].toString()
                tmpArticle.dateStamp = item["publishedAt"].toString()
                articleList.add(tmpArticle)
            }
            runOnUiThread {
                recyclerView.adapter = ArticleAdapter(articleList)
            }
        }
    }
}
