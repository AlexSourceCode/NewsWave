package com.example.newswave.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.newswave.R
import com.example.newswave.utils.StringUtils

class CustomArrayAdapter(context: Context, resource: Int, objects: List<String>) :
    ArrayAdapter<String>(context, resource, objects){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View{
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.tvText)
        val item = getItem(position)

        textView.text = item?.let {
            StringUtils.truncateText(it, 25)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_dropdown_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.tvText)
        textView.text = getItem(position)
        return view
    }
}