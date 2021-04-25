package me.melijn

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.io.File
import java.io.InputStreamReader
import java.util.*


class Scoodle(args: Array<String>) {

    private val client = OkHttpClient()
    var url: String = ""
    var folder: String = "nat2"
    var cookie: String = ""
    var start: Int = 837
    var end: Int = 837
    private val jobs = ArrayList<Job>()


    init {
        runBlocking {
            if (args.isEmpty() || !args[0].equals("code", true))
                startQuestions()
            else downloadOne(url, folder, cookie, start, end)
            jobs.joinAll()
        }
    }

    private suspend fun startQuestions() {
        val reader = Scanner(InputStreamReader(System.`in`))

        askUrl(reader)
        askCookie(reader)
        askFromPage(reader)
        askToPage(reader)
        downloadOne(url, "nat2", cookie, start, end)
    }

    private fun askToPage(reader: Scanner) {
        println("To page: ")
        end = reader.nextLine().toInt()
    }

    private fun askFromPage(reader: Scanner) {
        println("From page: ")
        start = reader.nextLine().toInt()
    }

    private fun askCookie(reader: Scanner) {
        println("Enter your knooppunt cookie: ")
        cookie = reader.nextLine()
    }

    private fun askUrl(reader: Scanner) {
        println("Enter questions url (without 001.png): ")
        url = reader.nextLine()
    }

    private suspend fun downloadOne(url: String, folder: String, cookie: String?, start: Int, end: Int) {
        for (i in start .. end) {
            val job = CoroutineScope(Dispatchers.Default).launch {
                val request = Request.Builder()
                    .get()
                    .url(url.replace("%page%", i.toString()))
                    .header("Cookie", cookie!!)
                    .build()

                client.newCall(request).await().use { response ->
                    response.body.use { body ->
                        if (body == null) return@launch
                        val file = File(folder + File.separator + "img-" + i.toString() + ".jpg")
                        file.parentFile.mkdirs()
                        file.writeBytes(body.bytes())

                        println("Wrote file $i to disk")
                    }
                }

            }
            jobs.add(job)
            if (i % 2 == 0)
                delay(2000)
        }
    }
}

fun main(args: Array<String>) {
    Scoodle(args)
}