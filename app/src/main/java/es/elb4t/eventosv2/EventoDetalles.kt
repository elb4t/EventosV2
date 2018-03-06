package es.elb4t.eventosv2

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import es.elb4t.eventosv2.Comun.Companion.getStorageReference
import es.elb4t.eventosv2.Comun.Companion.mostrarDialogo
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class EventoDetalles : AppCompatActivity() {
    var txtEvento: TextView? = null
    var txtFecha: TextView? = null
    var txtCiudad: TextView? = null
    var imgImagen: ImageView? = null
    var evento: String? = null
    var registros: CollectionReference? = null
    val SOLICITUD_SUBIR_PUTDATA = 0
    val solicitud_subir_putdata = 0
    val SOLICITUD_SUBIR_PUTSTREAM = 1
    val SOLICITUD_SUBIR_PUTFILE = 2
    val SOLICITUD_SELECCION_STREAM = 100
    val SOLICITUD_SELECCION_PUTFILE = 101
    val progresoSubida: ProgressDialog? = null
    var subiendoDatos: Boolean? = false
    lateinit var imagenRef: StorageReference

    companion object {
        var uploadTask: UploadTask? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.evento_detalles)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = EventoDetalles::class.java.simpleName

        txtEvento = findViewById(R.id.txtEvento)
        txtFecha = findViewById(R.id.txtFecha)
        txtCiudad = findViewById(R.id.txtCiudad)
        imgImagen = findViewById(R.id.imgImagen)

        val extras = intent.extras
        evento = extras.getString("evento")
        if (evento == null) evento = ""

        registros = FirebaseFirestore.getInstance().collection("eventos")
        registros!!.document(evento!!).get().addOnCompleteListener({
            if (it.isSuccessful && it.result.exists()) {
                txtEvento!!.text = it.result.get("evento").toString()
                txtCiudad!!.text = it.result.get("ciudad").toString()
                txtFecha!!.text = it.result.get("fecha").toString()
                Picasso.with(applicationContext)
                        .load(it.result.get("imagen").toString())
                        .error(R.mipmap.ic_launcher_round)
                        //.networkPolicy(NetworkPolicy.NO_CACHE)
                        .resize(300, 200)
                        .centerCrop()
                        .onlyScaleDown()
                        .into(imgImagen)
            } else {
                Toast.makeText(applicationContext, "El índice de evento solicitado no es válido", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var ficheroSeleccionado: Uri
        var cursor: Cursor
        var rutaImagen: String

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SOLICITUD_SELECCION_STREAM -> {
                    ficheroSeleccionado = data!!.data
                    val proyeccionStream = arrayOf(MediaStore.Images.Media.DATA)
                    cursor = contentResolver.query(ficheroSeleccionado, proyeccionStream, null, null, null)
                    cursor.moveToFirst()
                    rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionStream[0]))
                    cursor.close()
                    subirAFirebaseStorage(SOLICITUD_SUBIR_PUTSTREAM, rutaImagen)
                }
                SOLICITUD_SELECCION_PUTFILE -> {
                    Log.e("EVENTO", "PUT FILE")
                    ficheroSeleccionado = data!!.data
                    val proyeccionFile = arrayOf(MediaStore.Images.Media.DATA)
                    cursor = contentResolver.query(ficheroSeleccionado, proyeccionFile, null, null, null)
                    cursor.moveToFirst()
                    rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionFile[0]))
                    cursor.close()
                    subirAFirebaseStorage(SOLICITUD_SUBIR_PUTFILE, rutaImagen)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detalles, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val vista = findViewById<View>(android.R.id.content) as View

        when (item.itemId) {
            R.id.action_putData -> subirAFirebaseStorage(SOLICITUD_SUBIR_PUTDATA, null)
            R.id.action_streamData -> seleccionarFotografiaDispositivo(vista, SOLICITUD_SELECCION_STREAM)
            R.id.action_putFile -> seleccionarFotografiaDispositivo(vista, SOLICITUD_SELECCION_PUTFILE)
        }

        return super.onOptionsItemSelected(item)
    }

    fun seleccionarFotografiaDispositivo(v: View, solicitud: Int?) {
        val seleccionFotografiaIntent = Intent(Intent.ACTION_PICK)
        seleccionFotografiaIntent.type = "image/*"
        startActivityForResult(seleccionFotografiaIntent, solicitud!!)
    }

    fun subirAFirebaseStorage(opcion: Int, ficheroDispositivo: String?) {
        Log.e("EVENTO","subir a firebase- opción: $opcion :: fichero: $ficheroDispositivo")
        var fichero: String = evento!!
        imagenRef = getStorageReference().child(fichero)
        try {
            when (opcion) {
                SOLICITUD_SUBIR_PUTDATA -> {
                    imgImagen?.isDrawingCacheEnabled = true
                    imgImagen?.buildDrawingCache()
                    var bitmap: Bitmap = imgImagen?.drawingCache!!
                    var baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    var data: ByteArray = baos.toByteArray()
                    uploadTask = imagenRef.putBytes(data)
                }
                SOLICITUD_SUBIR_PUTSTREAM -> {
                    var stream: InputStream = FileInputStream(File(ficheroDispositivo))
                    uploadTask = imagenRef.putStream(stream)
                }
                SOLICITUD_SUBIR_PUTFILE -> {
                    Log.e("EVENTO","subir put file")
                    var file: Uri = Uri.fromFile(File(ficheroDispositivo))
                    uploadTask = imagenRef.putFile(file)
                    Log.e("EVENTO","subir put file end")
                }
            }

        }catch (e: Exception){
            mostrarDialogo(applicationContext, e.toString(),"")
        }
    }
}
