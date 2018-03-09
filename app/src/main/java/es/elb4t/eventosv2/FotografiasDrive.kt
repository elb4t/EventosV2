package es.elb4t.eventosv2

import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*


/**
 * Created by eloy on 9/3/18.
 */
class FotografiasDrive : AppCompatActivity() {
    var mDisplay: TextView? = null
    internal var evento: String? = null
    var servicio: Drive? = null
    lateinit var credencial: GoogleAccountCredential
    var nombreCuenta: String? = null
    val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
    val DISPLAY_MESSAGE_ACTION = "org.example.eventos.DISPLAY_MESSAGE"
    private val manejador = Handler()
    private val carga = Handler()
    private var dialogo: ProgressDialog? = null
    private var noAutoriza = false
    val SOLICITUD_SELECCION_CUENTA = 1
    val SOLICITUD_AUTORIZACION = 2
    val SOLICITUD_SELECCIONAR_FOTOGRAFIA = 3
    val SOLICITUD_HACER_FOTOGRAFIA = 4
    private val uriFichero: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fotografias_drive)
        val extras = intent.extras
        evento = extras!!.getString("evento")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = FotografiasDrive::class.java.simpleName

        credencial = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE))
        val prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        nombreCuenta = prefs.getString("nombreCuenta", null)
        noAutoriza = prefs.getBoolean("noAutoriza", false)
        if (!noAutoriza) {
            if (nombreCuenta == null) {
                PedirCredenciales()
            } else {
                credencial.selectedAccountName = nombreCuenta
                servicio = obtenerServicioDrive(credencial)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_drive, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vista = findViewById<View>(android.R.id.content) as View
        val id = item.getItemId()
        when (id) {
            R.id.action_camara -> {
            }
            R.id.action_galeria -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun mostrarMensaje(context: Context, mensaje: String) {
        manejador.post { Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show() }
    }

    fun mostrarCarga(context: Context, mensaje: String) {
        carga.post {
            dialogo = ProgressDialog(context)
            dialogo!!.setMessage(mensaje)
            dialogo!!.show()
        }
    }

    fun ocultarCarga(context: Context) {
        carga.post { dialogo!!.dismiss() }
    }

    private fun PedirCredenciales() {
        if (nombreCuenta == null) {
            startActivityForResult(credencial!!.newChooseAccountIntent(), SOLICITUD_SELECCION_CUENTA)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SOLICITUD_SELECCION_CUENTA -> {
                if (resultCode == Activity.RESULT_OK && data != null
                        && data.extras != null) {
                    nombreCuenta = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (nombreCuenta != null) {
                        credencial.setSelectedAccountName(nombreCuenta)
                        servicio = obtenerServicioDrive(credencial)
                        val prefs = getSharedPreferences(
                                "Preferencias", Context.MODE_PRIVATE)
                        val editor = prefs.edit()
                        editor.putString("nombreCuenta", nombreCuenta)
                        editor.commit()
                    }
                }
            }
            SOLICITUD_HACER_FOTOGRAFIA -> {
            }
            SOLICITUD_SELECCIONAR_FOTOGRAFIA -> {
            }
            SOLICITUD_AUTORIZACION -> {
            }
        }
    }

    private fun obtenerServicioDrive(credential: GoogleAccountCredential): Drive {
        return Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential).build()
    }
}