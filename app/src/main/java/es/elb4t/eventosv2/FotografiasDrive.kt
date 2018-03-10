package es.elb4t.eventosv2

import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.io.IOException
import java.text.SimpleDateFormat
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
    private var uriFichero: Uri? = null

    private var idCarpeta: String? = ""
    private var idCarpetaEvento: String? = ""

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
        idCarpeta = prefs.getString("idCarpeta", null)
        idCarpetaEvento = prefs.getString("idCarpeta_$evento", null)
        if (!noAutoriza) {
            if (nombreCuenta == null) {
                PedirCredenciales()
            } else {
                credencial.selectedAccountName = nombreCuenta
                servicio = obtenerServicioDrive(credencial)
                if (idCarpetaEvento == null) {
                    crearCarpetaEnDrive(evento!!, idCarpeta)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_drive, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vista = findViewById<View>(android.R.id.content) as View
        val id = item.itemId
        when (id) {
            R.id.action_camara -> {
                if (!noAutoriza) {
                    hacerFoto(vista)
                }
            }
            R.id.action_galeria -> {
                if (!noAutoriza) {
                    seleccionarFoto(vista)
                }
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
                        credencial.selectedAccountName = nombreCuenta
                        servicio = obtenerServicioDrive(credencial)
                        val prefs = getSharedPreferences(
                                "Preferencias", Context.MODE_PRIVATE)
                        val editor = prefs.edit()
                        editor.putString("nombreCuenta", nombreCuenta)
                        editor.commit()
                        crearCarpetaEnDrive(evento!!, idCarpeta)
                    }
                }
            }
            SOLICITUD_HACER_FOTOGRAFIA -> {
                if (resultCode == Activity.RESULT_OK) {
                    guardarFicheroEnDrive(this.findViewById<View>(android.R.id.content))
                }
            }
            SOLICITUD_SELECCIONAR_FOTOGRAFIA -> {
                if (resultCode == Activity.RESULT_OK) {
                    var ficheroSeleccionado: Uri = data!!.data
                    var proyeccion: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
                    var cursor: Cursor = managedQuery(ficheroSeleccionado, proyeccion, null, null, null)
                    var column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    cursor.moveToFirst()
                    uriFichero = Uri.fromFile(java.io.File(cursor.getString(column_index)))
                    guardarFicheroEnDrive(this.findViewById<View>(android.R.id.content))
                }
            }
            SOLICITUD_AUTORIZACION -> {
                if (resultCode == Activity.RESULT_OK) {
                    crearCarpetaEnDrive(evento!!, idCarpeta)
                } else {
                    noAutoriza = true;
                    var prefs: SharedPreferences = getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
                    var editor: SharedPreferences.Editor = prefs.edit()
                    editor.putBoolean("noAutoriza", true)
                    editor.commit()
                    mostrarMensaje(this, "El usuario no autoriza usar Google Drive")
                }
            }
        }
    }

    private fun obtenerServicioDrive(credential: GoogleAccountCredential): Drive {
        return Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential).build()
    }

    private fun crearCarpetaEnDrive(nombreCarpeta: String, carpetaPadre: String?) {
        var t: Thread = Thread(Runnable {
            try {
                var idCarpetaPadre = carpetaPadre
                mostrarCarga(this@FotografiasDrive, "Creando carpeta...")
                //Crear carpeta EventosDrive
                if (idCarpeta == null) {
                    val metadataFichero = File()
                    metadataFichero.name = "EventosDrive"
                    metadataFichero.mimeType = "application/vnd.google-apps.folder"
                    val fichero = servicio!!.files().create(metadataFichero)
                            .setFields("id").execute()
                    if (fichero.id != null) {
                        val prefs = getSharedPreferences("Preferencias",
                                Context.MODE_PRIVATE)
                        val editor = prefs.edit()
                        editor.putString("idCarpeta", fichero.id)
                        editor.commit()
                        idCarpetaPadre = fichero.id
                    }
                }
                val metadataFichero = File()
                metadataFichero.name = nombreCarpeta
                metadataFichero.mimeType = "application/vnd.google-apps.folder"
                if (idCarpetaPadre != "") {
                    metadataFichero.parents = Collections.singletonList(idCarpetaPadre)
                }
                val fichero = servicio!!.files().create(metadataFichero).setFields("id").execute()
                if (fichero.id != null) {
                    val prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
                    var editor: SharedPreferences.Editor = prefs.edit()
                    editor.putString("idCarpeta_" + evento, fichero.id)
                    editor.commit()
                    idCarpetaEvento = fichero.id
                    mostrarMensaje(this@FotografiasDrive, "¡Carpeta creada!")
                }
                ocultarCarga(this@FotografiasDrive)
            } catch (e: UserRecoverableAuthIOException) {
                ocultarCarga(this@FotografiasDrive)
                startActivityForResult(e.intent, SOLICITUD_AUTORIZACION)
            } catch (e: IOException) {
                mostrarMensaje(this@FotografiasDrive, "Error; ${e.message}")
                ocultarCarga(this@FotografiasDrive)
                e.printStackTrace()
            }
        })
        t.start()
    }

    fun hacerFoto(v: View) {
        if (nombreCuenta == null) {
            mostrarMensaje(this, "Debes seleccionar una cuenta de Google Drive")
        } else {
            var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                var ficheroFoto: java.io.File? = null
                try {
                    ficheroFoto = crearFicheroImagen()
                    if (ficheroFoto != null) {
                        var fichero: Uri = FileProvider.getUriForFile(
                                this@FotografiasDrive,
                                BuildConfig.APPLICATION_ID + ".provider",
                                ficheroFoto)
                        uriFichero = Uri.parse("content://${ficheroFoto.absolutePath}")
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fichero)
                        startActivityForResult(takePictureIntent, SOLICITUD_HACER_FOTOGRAFIA)
                    }
                } catch (ex: IOException) {
                    return
                }
            }
        }
    }

    private fun crearFicheroImagen(): java.io.File {
        var tiempo: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var nombreFichero: String = "JPEG_${tiempo}_"
        var dirAlmacenaje: java.io.File = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera")
        var ficheroImagen: java.io.File = java.io.File.createTempFile(nombreFichero, ".jpg", dirAlmacenaje)
        return ficheroImagen
    }

    fun seleccionarFoto(v: View) {
        if (nombreCuenta == null) {
            mostrarMensaje(this, "Debes seleccionar una cuenta de Google Drive")
        } else {
            val seleccionFotografiaIntent = Intent()
            seleccionFotografiaIntent.type = "image/*"
            seleccionFotografiaIntent.action = Intent.ACTION_PICK
            startActivityForResult(Intent.createChooser(seleccionFotografiaIntent,
                    "Seleccionar fotografía"), SOLICITUD_SELECCIONAR_FOTOGRAFIA)
        }
    }

    private fun guardarFicheroEnDrive(view: View) {
        var t: Thread = Thread(Runnable {
            try {
                mostrarCarga(this@FotografiasDrive, "Subiendo imagen...")
                var ficheroJava: java.io.File = java.io.File(uriFichero!!.path)
                var contenido: FileContent = FileContent("image/jpeg", ficheroJava)
                val ficheroDrive = File()
                ficheroDrive.name = ficheroJava.name
                ficheroDrive.mimeType = "image/jpeg"
                ficheroDrive.parents = Collections.singletonList(idCarpetaEvento)
                val ficheroSubido = servicio!!.files().create(ficheroDrive, contenido).setFields("id").execute()
                if (ficheroSubido.id != null) {
                    mostrarMensaje(this@FotografiasDrive, "¡Foto subida!")
                }
                ocultarCarga(this@FotografiasDrive)
            } catch (e: UserRecoverableAuthIOException) {
                ocultarCarga(this@FotografiasDrive)
                startActivityForResult(e.intent, SOLICITUD_AUTORIZACION)
            } catch (e: IOException) {
                mostrarMensaje(this@FotografiasDrive, "Error; ${e.message}")
                ocultarCarga(this@FotografiasDrive)
                e.printStackTrace()
            }
        })
        t.start()
    }
}