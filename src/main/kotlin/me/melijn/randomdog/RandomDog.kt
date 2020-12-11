package me.melijn.randomdog

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.io.File


class RandomDog {

    private val client = HttpClient(OkHttp)
    private val okclient = OkHttpClient()
    private val objectMapper = jacksonObjectMapper()

    init {
        runBlocking {
            doScrape()
        }
    }

    private suspend fun doScrape() {
        val jsonString = client.get<String>("https://random.dog/doggos")
        val json = objectMapper.readTree(jsonString)
        var counter = 0
        json.forEach {
            val url = "https://random.dog/" + it.asText()
            val request = Request.Builder()
                .get()
                .url(url)
                .build()

            val ext = url.split(".").last()
            if (ext.equals("mp4", true)) {
                println("ignored: $url")
                return@forEach
            }

            println("found: $url")

            okclient.newCall(request).await().use { response ->
                response.body?.use { body ->
                    val file = File("dogs/" + File.separator + "dog-" + counter.toString() + ".$ext")
                    file.parentFile.mkdirs()
                    file.writeBytes(body.bytes())

                    println("Wrote file ${counter++}.$ext to disk")
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    RandomDog()
}