package me.melijn.scoodle

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request.Builder
import ru.gildor.coroutines.okhttp.await
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayList

class Scoodle(args: Array<String>) {

    private val client = OkHttpClient()
    var url: String = ""
    var answerUrl: String = ""
    var cookie: String = ""
    var start: Int = 0
    var end: Int = 200
    private val jobs = ArrayList<Job>()


    init {
        runBlocking {
            if (args.isEmpty() || !args[0].equals("code", true))
                startQuestions()
            else downloadBoth(url, answerUrl, cookie, start, end)
            jobs.forEach { job -> job.join() }
        }
    }

    private suspend fun startQuestions() {
        val reader = Scanner(InputStreamReader(System.`in`))
        println("Select mode (Answers and Questions: aaq; Answers: a; Questions: q)")
        val mode: String = reader.nextLine()
        when (mode.toLowerCase()) {
            "aaq" -> {
                askQuestionsUrl(reader)
                askAnswersUrl(reader)
                askKnooppuntCookie(reader)
                askFromPage(reader)
                askToPage(reader)
                downloadBoth(url, answerUrl, cookie, start, end)
            }
            "a" -> {
                askAnswersUrl(reader)
                askKnooppuntCookie(reader)
                askFromPage(reader)
                askToPage(reader)
                downloadOne(answerUrl, "answers", cookie, start, end)
            }
            "q" -> {
                askQuestionsUrl(reader)
                askKnooppuntCookie(reader)
                askFromPage(reader)
                askToPage(reader)
                downloadOne(url, "questions", cookie, start, end)
            }
            else -> println("Not a mode")
        }
    }

    private fun askToPage(reader: Scanner) {
        println("To page: ")
        end = reader.nextLine().toInt()
    }

    private fun askFromPage(reader: Scanner) {
        println("From page: ")
        start = reader.nextLine().toInt()
    }

    private fun askKnooppuntCookie(reader: Scanner) {
        println("Enter your knooppunt cookie: ")
        cookie = reader.nextLine()
    }

    private fun askAnswersUrl(reader: Scanner) {
        println("Enter answers url (without 001.png): ")
        answerUrl = reader.nextLine()
    }

    private fun askQuestionsUrl(reader: Scanner) {
        println("Enter questions url (without 001.png): ")
        url = reader.nextLine()
    }

    private suspend fun downloadOne(url: String, folder: String, cookie: String?, start: Int, end: Int) {
        for (i in start until end) {
            val job = CoroutineScope(Dispatchers.Default).launch {
                val request = Builder()
                    .get()
                    .url("$url$i.png")
                    .header("Cookie", cookie!!)
                    .build()

                client.newCall(request).await().use { response ->
                    if (response.header("Content-Type") == null) return@launch
                    if (response.header("Content-Type").equals("image/png", ignoreCase = true)) {
                        response.body.use { body ->
                            if (body == null) return@launch
                            val file = File(folder + File.separator + "img-" + i.toString() + ".png")
                            file.parentFile.mkdirs()
                            file.writeBytes(body.bytes())

                            println("Wrote file $i to disk")
                        }
                    }
                }

            }
            jobs.add(job)
        }
    }

    private suspend fun downloadBoth(url: String, answerUrl: String, cookie: String?, start: Int, end: Int) {
        for (i in start until end) {
            val job = CoroutineScope(Dispatchers.Default).launch {

                val request = Builder()
                    .get()
                    .url("$url$i.png")
                    .header("Cookie", cookie!!)
                    .build()
                val answerRequest = Builder()
                    .get()
                    .url("$answerUrl$i.png")
                    .header("Cookie", cookie)
                    .build()

                //Don't look at the nesting please I'm to lazy to put it into methods
                client.newCall(request).await().use { response ->
                    client.newCall(answerRequest).await().use { response1 ->
                        if (response.header("Content-Type") == null) return@launch
                        if (response.header("Content-Type").equals("image/png", ignoreCase = true)) {
                            response.body.use { body ->
                                if (body == null) return@launch
                                response1.body.use { body1 ->
                                    if (body1 == null) return@launch
                                    var data = body.bytes()
                                    val file = File("normal" + File.separator + "img-" + i.toString() + ".png")

                                    file.parentFile.mkdirs()
                                    file.writeBytes(data)
                                    println("Written file $i to disk")


                                    val answerImage = ImageIO.read(body1.byteStream())
                                    if (answerImage != null) {
                                        val img: BufferedImage = ImageIO.read(file)
                                        val g: Graphics2D = img.createGraphics()
                                        g.drawImage(answerImage, 0, 0, null)
                                        g.dispose()
                                        ByteArrayOutputStream().use {
                                            ImageIO.write(img, "png", it)
                                            data = it.toByteArray()
                                        }

                                    }
                                    val filledFile = File("filled" + File.separator + "img-" + i.toString() + ".png")
                                    filledFile.parentFile.mkdirs()
                                    filledFile.writeBytes(data)
                                    println("Wrote filled file $i to disk")
                                }
                            }
                        }
                    }
                }
            }
            jobs.add(job)
        }
    }
}

fun main(args: Array<String>) {
    Scoodle(args)
}