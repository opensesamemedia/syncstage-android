package media.opensesame.syncstagequickstart

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection

fun getUrlContents(theUrl: String, onSuccess: (data: String) -> Unit, onError: (error: String) -> Unit){
    GlobalScope.launch(Dispatchers.IO) {
        val content = StringBuilder()
        // Use try and catch to avoid the exceptions
        try {
            val url = URL(theUrl) // creating a url object
            val urlConnection: URLConnection = url.openConnection() // creating a urlconnection object

            // wrapping the urlconnection in a bufferedreader
            val bufferedReader = BufferedReader(InputStreamReader(urlConnection.getInputStream()))
            // reading from the urlconnection using the bufferedreader
            while (true) {
                val line = bufferedReader.readLine() ?: break
                content.append(
                    """
                    $line
                    """.trimIndent()
                )
            }
            bufferedReader.close()
            onSuccess(content.toString().trim())
        } catch (e: Exception) {
            e.printStackTrace()
            onError("Could not get the data")
        }
    }

}