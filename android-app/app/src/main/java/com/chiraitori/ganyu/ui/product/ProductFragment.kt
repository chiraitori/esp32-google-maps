package com.chiraitori.ganyu.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.chiraitori.ganyu.R

class ProductFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_product, container, false)
        val textView: TextView = root.findViewById(R.id.text_product)
        textView.text = "Product Section\nComing Soon"
        return root
    }
}
