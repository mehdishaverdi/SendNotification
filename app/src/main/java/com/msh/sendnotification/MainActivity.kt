package com.msh.sendnotification

import android.content.Context
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class MainActivity : AppCompatActivity() {

    var message :String = ""
    var phoneNumber :String = ""
    lateinit var context :Context
    private val fontPath = arrayOf("fonts/IRANYekanRegularFaNum.ttf", "fonts/IRANYekanBoldFaNum.ttf")
    private val fonts = arrayOfNulls<Typeface>(2)
    private var fontsLoaded = false
    lateinit var tf :Typeface
    lateinit var tf_bold :Typeface
    var one : Boolean = false
    var all : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        tf = getTypeface(context, 0)!!
        tf_bold = getTypeface(context, 1)!!

        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val phoneNumberEditText = findViewById<EditText>(R.id.phoneNumberEditText)
        val txtTitle = findViewById<TextView>(R.id.txtTitle)

        messageEditText.setTypeface(tf)
        phoneNumberEditText.setTypeface(tf)
        txtTitle.setTypeface(tf_bold)

        val btn_ok = findViewById<Button>(R.id.btn_ok)
        btn_ok.setTypeface(tf_bold)
        btn_ok.setOnClickListener({
            one = true
            all = false

            message = messageEditText.text.toString()
            phoneNumber = phoneNumberEditText.text.toString()

            if (phoneNumber.equals(""))
                Toast.makeText(context, "شماره تلفن را وارد کنید", Toast.LENGTH_LONG).show()
            else if (message.equals(""))
                Toast.makeText(context, "پیام را وارد کنید", Toast.LENGTH_LONG).show()
            else
            {
                var  task :someTask = someTask()
                task.execute()
            }
        })

        val btn_oks = findViewById<Button>(R.id.btn_oks)
        btn_oks.setTypeface(tf_bold)
        btn_oks.setOnClickListener({
            one = false
            all = true

            message = messageEditText.text.toString()
            phoneNumber = phoneNumberEditText.text.toString()

            if (message.equals(""))
                Toast.makeText(context, "پیام را وارد کنید", Toast.LENGTH_LONG).show()

            var  task :someTasks = someTasks()
            task.execute()
        })
    }

    inner class someTask() : AsyncTask<Void, Void, String>() {

        var receivedData :String = ""

        override fun doInBackground(vararg params: Void): String? {
            receivedData = sendParamsInBody(phoneNumber!!, message!!)
            return receivedData
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Toast.makeText(context, receivedData, Toast.LENGTH_LONG).show()
        }
    }

    inner class someTasks() : AsyncTask<Void, Void, String>() {

        var receivedData :String = ""

        override fun doInBackground(vararg params: Void): String? {
            receivedData = sendParamsInBody(phoneNumber!!, message!!)
            return receivedData
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Toast.makeText(context, receivedData, Toast.LENGTH_LONG).show()
        }
    }

    private fun sendParamsInBody(phoneNumber :String, message :String): String {
        var urlConn: HttpURLConnection? = null
        var uri: URL? = null
        var requestBody : String

        return try {
            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<X509TrustManager>(object : X509TrustManager {

                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }
            }), SecureRandom())

            if (one)
                uri = URL("http://82.115.17.20:9092/notifications/send")
            else
                uri = URL("http://82.115.17.20:9092/notifications/send-all")

            urlConn = uri?.openConnection() as HttpURLConnection
//            urlConn.sslSocketFactory(sslContext.getSocketFactory())

            createURLConnection(urlConn)

            if (one)
                requestBody = buildPostParameters(phoneNumber, message)!!
            else
                requestBody = buildPostParameters("", message)!!

            Log.d("OniPod_RESTAPI", "post request URL: $uri")
            Log.d("OniPod_RESTAPI", "post request requestBody: $requestBody")

            return callWebService(urlConn, requestBody)
        } catch (ex: Exception) {
            ex.printStackTrace()
            "-1"
        }
    }

    private fun buildPostParameters(phoneNumber :String, message :String) : String? {

        val mParams = JSONObject()
        if (!phoneNumber.equals(""))
            mParams.put("mobileNumber", phoneNumber)
        if (!message.equals(""))
            mParams.put("message", message)

        return mParams.toString()

//        val jo = JSONObject()
//        val packedData = StringBuffer()
//
//        try {
//            var firstValue = true
//
//            jo.put("mobileNumber", phoneNumber);
//            jo.put("message", message);
//
//            val it: Iterator<*> = jo.keys()
//            do {
//                val key = it.next().toString()
//                val value = jo[key].toString()
//
//                if (firstValue)
//                    firstValue = false
//                else
//                    packedData.append("&")
//
//                packedData.append(key)
//                packedData.append("=")
//                packedData.append(value)
//            } while (it.hasNext())
//
//        } catch (e : JSONException) {
//            e.printStackTrace()
//        }
//        return packedData.toString()
    }

    @Throws(ProtocolException::class)
    private fun createURLConnection(urlConn: HttpURLConnection?) {
        urlConn!!.connectTimeout = 40000
        urlConn.readTimeout = 40000
        urlConn.requestMethod = "POST"
        urlConn.allowUserInteraction = false
        urlConn.doOutput = true
        urlConn.useCaches = false
        urlConn.addRequestProperty("Content-Type", "application/json")
    }

    private fun callWebService(urlConn: HttpURLConnection?, requestBody: String): String {
        var inputStream: InputStream? = null
        var receivedData: String? = ""
        try {
            if (urlConn!!.requestMethod == "POST") {
                val os = urlConn.outputStream
                val bw = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                bw.write(requestBody)
                bw.flush()
                bw.close()
                os.close()
            }
            val responseCode = urlConn.responseCode
            inputStream = if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST)
                urlConn.inputStream
            else
                urlConn.errorStream
            val br = BufferedReader(InputStreamReader(inputStream))
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                receivedData += line
            }
            br.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("OniPod", "restAPI exception: " + e.message)
            return "-1"
        }
        return receivedData.toString()
    }

    fun getTypeface(context: Context, fontIdentifier: Int): Typeface? {
        if (!fontsLoaded) {
            loadFonts(context)
        }
        return fonts.get(fontIdentifier)
    }

    private fun loadFonts(context: Context) {
        try {
            for (i in fonts.indices) {
                fonts[i] = Typeface.createFromAsset(context.assets, fontPath.get(i))
            }
            fontsLoaded = true
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }
}
