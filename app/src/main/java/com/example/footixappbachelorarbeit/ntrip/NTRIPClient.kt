package com.example.footixappbachelorarbeit.ntrip

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.io.PrintWriter
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.Socket
import java.net.URL
import java.net.UnknownHostException

class NTRIPClient {

    fun connect(serverUrl: String, mountPoint: String, callback: (success: Boolean, data: String) -> Unit) {
        val connectTask = ConnectTask(serverUrl, mountPoint, callback)
        connectTask.execute()
    }

    private class ConnectTask(
        val serverUrl: String ,
        val mountPoint: String,
        val callback: (success: Boolean, data: String) -> Unit
    ) : AsyncTask<Void, Void, Pair<Boolean, String>>() {

        private var socket: Socket? = null
        private var output: PrintWriter? = null
        private var input: BufferedInputStream? = null

        override fun doInBackground(vararg params: Void?): Pair<Boolean, String> {
            try {
                val url = URL(serverUrl)
                val address = InetAddress.getByName(url.host)
                socket = Socket(address, 2101)
                output = PrintWriter(socket!!.getOutputStream(), true)
                input = BufferedInputStream(socket!!.getInputStream())

                val requestMessage = "GET /$mountPoint HTTP/1.1\r\n" +
                        "Ntrip-Version: Ntrip/2.0\r\n" +
                        "Connection: Keep-Alive\r\n" +
                        "Accept-Encoding: identity\r\n" +
                        "\r\n"
                output!!.println(requestMessage)

                val buffer = ByteArray(1024)
                val receivedData = StringBuilder()
                var bytesRead: Int
                while (input!!.read(buffer).also { bytesRead = it } != -1) {
                    receivedData.append(String(buffer, 0, bytesRead))
                }
                return Pair(true, receivedData.toString())
            } catch (e: MalformedURLException) {
                Log.e("NTRIP", "Malformed URL: ${e.message}")
                return Pair(false, "Malformed URL: ${e.message}")
            } catch (e: UnknownHostException) {
                Log.e("NTRIP", "Unknown host: ${e.message}")
                return Pair(false, "Unknown host: ${e.message}")
            } catch (e: IOException) {
                Log.e("NTRIP", "IO Exception: ${e.message}")
                return Pair(false, "IO Exception: ${e.message}")
            } finally {
                socket?.close()
                output?.close()
                input?.close()
            }
        }

        override fun onPostExecute(result: Pair<Boolean, String>) {
            super.onPostExecute(result)
            callback(result.first, result.second)
        }
    }
}
