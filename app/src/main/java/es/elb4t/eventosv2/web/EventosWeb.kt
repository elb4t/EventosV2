package es.elb4t.eventosv2.web

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import es.elb4t.eventosv2.Comun.Companion.colorFondo
import es.elb4t.eventosv2.R
import kotlinx.android.synthetic.main.eventos_web.*
import java.net.MalformedURLException
import java.net.URL


class EventosWeb : AppCompatActivity() {
    lateinit var navegador: WebView
    var dialogo: ProgressDialog? = null
    val PERMISOS = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_NETWORK_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.eventos_web)
        var extras: Bundle = intent.extras
        var evento = extras.getString("evento")
        navegador = findViewById(R.id.webkit)
        navegador.settings.javaScriptEnabled = true
        navegador.settings.builtInZoomControls = false
        val miInterfazJava: InterfazComunicacion = InterfazComunicacion(this)

        if (comprobarConectividad())
            navegador.loadUrl("https://eventos-3161f.firebaseapp.com/index.html")

        navegador.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                var url_filtro: String = "http://www.androidcurso.com/"
                if (url != url_filtro) {
                    view.loadUrl(url_filtro)
                }
                return false
            }
        }
        navegador.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(this@EventosWeb).setTitle("Mensaje: $evento")
                        .setMessage(message).setPositiveButton(android.R.string.ok, { dialogInterface: DialogInterface, i: Int ->
                            result.confirm()
                        }).setCancelable(false).create().show()
                return true
            }
        }
        navegador.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                dialogo = ProgressDialog(this@EventosWeb)
                dialogo!!.setMessage("Cargando...")
                dialogo!!.setCancelable(true)
                dialogo!!.show()
            }

            override fun onPageFinished(view: WebView, url: String) {
                dialogo!!.dismiss()
                navegador.loadUrl("javascript:colorFondo(\"$colorFondo\")")
                navegador.loadUrl("javascript:muestraEvento(\"$evento\");")
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                val builder = AlertDialog.Builder(this@EventosWeb)
                builder.setMessage(description).setPositiveButton("Aceptar", null).setTitle("onReceivedError")
                builder.show()
            }
        }
        navegador.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val builder = AlertDialog.Builder(this@EventosWeb)
            builder.setTitle("Descarga")
            builder.setMessage("¿Deseas guardar el archivo?")
            builder.setCancelable(false).setPositiveButton("Aceptar") { dialog, id ->
                val urlDescarga: URL
                try {
                    urlDescarga = URL(url)
                    var p: String? = url
                    //DescargaFicheroManager.execute(url,"",this@EventosWeb)
                    DescargarFichero(this@EventosWeb).Descargar(url)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancelar") { dialog, id -> dialog.cancel() }
            builder.create().show()
        }
        navegador.addJavascriptInterface(miInterfazJava, "jsInterfazNativa")

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        ActivityCompat.requestPermissions(this@EventosWeb, PERMISOS, 1)
    }

    override fun onBackPressed() {
        if (navegador.canGoBack() && comprobarConectividad())
            navegador.goBack()
        else
            super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                grantResults.forEach { result ->
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(layout,
                                "Hay permisos necesarios para la aplicación sin activar", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Activar", {
                                    ActivityCompat.requestPermissions(this, PERMISOS, 1)
                                }).show()

                        return
                    }
                }
            }
        }
    }

    private fun comprobarConectividad(): Boolean {
        val connectivityManager = this.getSystemService(
                Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.activeNetworkInfo
        if (info == null || !info.isConnected || !info.isAvailable) {
            Toast.makeText(this@EventosWeb, "Oops! No tienes conexión a internet", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    inner class InterfazComunicacion internal constructor(internal var mContext: Context) {
        @JavascriptInterface
        fun volver() {
            finish()
        }
    }
}
