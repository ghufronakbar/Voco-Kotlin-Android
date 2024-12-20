package com.example.voco.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.voco.ui.DetailProductActivity
import com.example.voco.R
import com.example.voco.model.Product

class ProductAdapter(private val context: Context, private val productList: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        val tvHistoryId: TextView = itemView.findViewById(R.id.tvHistoryId)
        val tvBrandType: TextView = itemView.findViewById(R.id.tvBrandType)
        val tvExpiredDate: TextView = itemView.findViewById(R.id.tvExpiredDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val product = productList[position]
                    val intent = Intent(context, DetailProductActivity::class.java)

                    intent.putExtra("productId", product.id)
                    intent.putExtra("productQrCode", product.qrCode)
                    intent.putExtra("productBrand", product.brand)
                    intent.putExtra("productVariant", product.variant)
                    intent.putExtra("productExpiredDate", product.expiredDate)
                    intent.putExtra("productImage", product.image)
                    intent.putExtra("productText", product.text)

                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.tvHistoryId.text = product.id
        holder.tvBrandType.text = product.brand
        holder.tvExpiredDate.text = "Tanggal Kadaluarsa: ${product.expiredDate}"

        Glide.with(context)
            .load(product.image)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.ivProduct)
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
