package com.meryemarpaci.catsbook

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.meryemarpaci.catsbook.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var catList: ArrayList<Cat>
    private lateinit var catAdapter : CatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        catList=ArrayList<Cat>()
        catAdapter= CatAdapter(catList)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = catAdapter

        try {
            val database = this.openOrCreateDatabase("Cats", MODE_PRIVATE, null)
            val cursor = database.rawQuery("select * from cats", null)
            val catNameIx = cursor.getColumnIndex("catname")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()) {
                val name = cursor.getString(catNameIx)
                val id = cursor.getInt(idIx)
                val cat=Cat(name,id)
                catList.add(cat)
            }
            catAdapter.notifyDataSetChanged()

            cursor.close()


        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    fun addCatClick(view: View) {
        val intent = Intent(
            this@MainActivity,
            DetailsActivity::class.java
        )
        startActivity(intent)

    }

}

