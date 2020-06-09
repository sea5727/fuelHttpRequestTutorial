package com.example.fuelhttprequesttutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.extensions.cUrlString
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.core.requests.CancellableRequest
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import kotlinx.android.synthetic.main.activity_main.*
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.result.Result;
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("DEBUG", "onCreate Thread Id : ${Thread.currentThread().id}")

        btn_http_request_1.setOnClickListener {
            Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")
            asyncHttpRequest()
        }
        btn_http_request_2.setOnClickListener{
            Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")
            syncHttpRequest()
        }
        btn_http_request_3.setOnClickListener{
            Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")
            simple_coding_1()
        }
        btn_http_request_4.setOnClickListener{
            Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")
            simple_coding_2()
        }
        btn_http_request_5.setOnClickListener{
            Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")
            coroutineHttpRequest()
        }
        btn_http_request_6.setOnClickListener{
            Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")
            httpPut()
        }
    }

    fun asyncHttpRequest(){
        CancellableRequest
        val httpAsync = "http://192.168.0.2:3000/HelloWorld"
            .httpGet()
            .responseString { request, response, result ->
                Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")
                update(result)
            }
//        httpAsync.join()
    }

    fun syncHttpRequest(){
        val (request, response, result) = "http://192.168.0.2:3000/HelloWorld"
            .httpGet()
            .responseString()
        Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")


        when (result) {
            is Result.Failure -> {
                Log.d("DEBUG", "Failure Thread Id : ${Thread.currentThread().id}")
                val ex = result.getException()
                println(ex)
            }
            is Result.Success -> {
                Log.d("DEBUG", "Success Thread Id : ${Thread.currentThread().id}")
                val data = result.get()
                println(data)
            }
        }
    }
    private fun <T : Any> update(result: Result<T, FuelError>) {
        result.fold(success = {
            println(it)
        }, failure = {
            println(it)
        })
    }


    fun simple_coding_1(){
        Fuel.get("http://192.168.0.2:3000/HelloWorld")
            .also { Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}, it:" + it)}
            .responseString { _,_, result -> update(result)  }
    }
    fun simple_coding_2(){
        "http://192.168.0.2:3000/HelloWorld"
            .httpGet()
            .also { Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}, it:" + it)}
            .responseString { _,_, result -> update(result)  }
    }

    fun coroutineHttpRequest(){

        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            var (request, response, result) = Fuel.get("http://192.168.0.2:3000/HelloWorld").awaitStringResponseResult()
            Log.d("DEBUG", "Thread Id : ${Thread.currentThread().id}")
            update(result)
            runOnUiThread { Log.d("DEBUG", "RunOnUiThread Id : ${Thread.currentThread().id}") }
        }
    }
    private fun httpPut() {

        val json = JSONObject()
        json.put("body", "foo")

        Fuel.post("http://192.168.0.2:3000/HelloWorld")
            .body("My Post Body")
            .also { println(it) }
            .response { result -> println(result)}


        Fuel.put("http://192.168.0.2:3000/HelloWorld", listOf("foo" to "foo", "bar" to "bar"))
            .appendHeader("Content-Type", "application/json")
            .jsonBody(json.toString())
            .also { Log.d("DEBUG", it.cUrlString() + it.parameters ) }
            .responseString { _, _, result -> update(result) }

        "http://192.168.0.2:3000/HelloWorld"
            .httpPut(listOf("foo" to "foo", "bar" to "bar"))
            .also { Log.d("DEBUG", it.cUrlString()) }
            .responseString { _, _, result -> update(result) }


        "http://192.168.0.2:3000/HelloWorld"
            .httpPut()
            .jsonBody(json.toString())
            .responseString { _, _, result ->
                result.fold({
                    println("DEBUG success? $it")
                }, {
                    println("DEBUG failure? $it")
                })
            }


    }

}
