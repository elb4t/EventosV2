package es.elb4t.eventosv2.utils

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley


/**
 * Created by eloy on 26/9/17.
 */
class RequestVolley(private val context: Context?) {
    val TAG = "---- " + RequestVolley::class.java.simpleName.toUpperCase()

    private var mInstance: RequestVolley? = null

    var requestQueue: RequestQueue? = null
        get() {
            if (field == null) {
                return Volley.newRequestQueue(context)
            }
            return field
        }

    @Synchronized
    fun getInstance(context: Context): RequestVolley {
        if (mInstance == null) {
            mInstance = RequestVolley(context)
        }
        return mInstance as RequestVolley
    }

    private var mStatusCode: Int = 0

    /** Petición POST a una api rest
     * @param path String con la ruta de la api definidas
     * @param bodyJson JSONObject para definir el body de la petición
     * @return response, code - String con el resultado de la petición - Codigo de respuesta del servidor.
     */
    fun post(path: String, params: HashMap<String,String>, completionHandler: (response: String, code: Int) -> Unit) {
        val request = object : StringRequest(Request.Method.GET, path,
                Response.Listener { response ->
                    Log.e(TAG, "/post $path request OK!")
                    Log.e(TAG, "Params: ${params["idapp"]} ||| ${params["iddevice"]}")
                    Log.e(TAG, "Code: $mStatusCode - Response: $response")
                    completionHandler(response.toString(), mStatusCode)
                }, Response.ErrorListener { error ->
                    val resp = String(error.networkResponse.data)
                    Log.e(TAG, "/post $path request fail! Error: ${error.networkResponse.statusCode}")
                    Log.e(TAG, "Response: $resp")
                    completionHandler(resp, error.networkResponse.statusCode)
        }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                mStatusCode = response.statusCode
                return super.parseNetworkResponse(response)
            }

            @Throws(AuthFailureError::class)
            override fun getParams(): MutableMap<String, String> {
                return params
            }
        }
        requestQueue?.add(request)
    }
}
