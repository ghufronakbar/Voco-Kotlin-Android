package com.example.voco.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.adapter.ProductAdapter
import com.example.voco.api.ApiInterface
import com.example.voco.api.RetrofitClient
import com.example.voco.model.ApiRes
import com.example.voco.model.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class ProductActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: List<Product>
    private lateinit var retrofit: Retrofit
    private lateinit var apiInterface: ApiInterface
    private var isFetched: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product)

        recyclerView = findViewById(R.id.rvProduct)
        recyclerView.layoutManager = LinearLayoutManager(this)

        retrofit = RetrofitClient.getClient(this)
        apiInterface = retrofit.create(ApiInterface::class.java)

        fetchData()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchData() {
        if(!isFetched){
        val call = apiInterface.getAllProducts()
            call.enqueue(object : Callback<ApiRes<List<Product>>> {
                override fun onResponse(
                    call: Call<ApiRes<List<Product>>>,
                    response: Response<ApiRes<List<Product>>>
                ) {

                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data?.success == true && data.data != null) {
                            productList = data.data
                            isFetched = true
                            productAdapter = ProductAdapter(this@ProductActivity, productList)
                            recyclerView.adapter = productAdapter
                        } else {
                            Toast.makeText(this@ProductActivity, data?.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Log.e("Error", response.errorBody().toString())
                        Toast.makeText(
                            this@ProductActivity,
                            "Terjadi kesalahan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                override fun onFailure(call: Call<ApiRes<List<Product>>>, t: Throwable) {
                    Log.e("Error", t.message.toString())
                    Toast.makeText(this@ProductActivity, "Terjadi kesalahan", Toast.LENGTH_SHORT)
                        .show()

                }
            })
        }
    }
}