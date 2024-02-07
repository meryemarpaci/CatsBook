package com.meryemarpaci.catsbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meryemarpaci.catsbook.databinding.RecyclerRowBinding

class CatAdapter(val catList:ArrayList<Cat>) : RecyclerView.Adapter<CatAdapter.CatHolder> (){
    class CatHolder(val binding : RecyclerRowBinding):RecyclerView.ViewHolder(binding.root) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CatHolder(binding)
    }

    override fun onBindViewHolder(holder: CatHolder, position: Int) {
        holder.binding.recyclerVieww.text = catList.get(position).name
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,DetailsActivity::class.java)
            intent .putExtra("info","old")
            intent.putExtra("id",catList[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return catList.size

    }


}