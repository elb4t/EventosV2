package es.elb4t.eventosv2

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import es.elb4t.eventosv2.Comun.Companion.getStorageReference
import es.elb4t.eventosv2.Comun.Companion.mFirebaseAnalytics
import es.elb4t.eventosv2.Comun.Companion.mostrarDialogo
import es.elb4t.eventosv2.Comun.Companion.storage
import es.elb4t.eventosv2.web.EventosWeb
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
    val SOLICITUD_SUBIR_PUTSTREAM = 1
    val SOLICITUD_SUBIR_PUTFILE = 2
    val SOLICITUD_SELECCION_STREAM = 100
    val SOLICITUD_SELECCION_PUTFILE = 101
    val SOLICITUD_FOTOGRAFIAS_DRIVE = 102
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
        imagenRef = storage.reference
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
        val bundle = Bundle()
        when (item.itemId) {
            R.id.action_putData -> {
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "subir_imagen")
                mFirebaseAnalytics?.logEvent("menus", bundle)
                subirAFirebaseStorage(SOLICITUD_SUBIR_PUTDATA, null)
            }
            R.id.action_streamData -> {
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "subir_stream")
                mFirebaseAnalytics?.logEvent("menus", bundle)
                seleccionarFotografiaDispositivo(vista, SOLICITUD_SELECCION_STREAM)
            }
            R.id.action_putFile -> {
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "subir_fichero")
                mFirebaseAnalytics?.logEvent("menus", bundle)
                seleccionarFotografiaDispositivo(vista, SOLICITUD_SELECCION_PUTFILE)
            }
            R.id.action_getFile -> {
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "descargar_fichero")
                mFirebaseAnalytics?.logEvent("menus", bundle)
                descargarDeFirebaseStorage(evento!!)
            }
            R.id.action_deleteFile -> {
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "eliminar_fichero")
                mFirebaseAnalytics?.logEvent("menus", bundle)
                eliminarDeFirebaseStorage(evento!!)
            }
            R.id.action_fotografiasDrive -> {
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "fotografias_drive")
                mFirebaseAnalytics?.logEvent("menus", bundle)
                val intent = Intent(baseContext, FotografiasDrive::class.java)
                intent.putExtra("evento", evento)
                startActivity(intent)
            }
            R.id.action_fotografiasDriveCompartidas -> {
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "fotografias_drive_compartidas")
                mFirebaseAnalytics?.logEvent("menus", bundle)
                val intent = Intent(baseContext, FotografiasDrive::class.java)
                intent.putExtra("evento", evento)
                intent.putExtra("compartida", true)
                startActivity(intent)
            }
            R.id.action_acercaDe -> {
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "acerca_de")
                mFirebaseAnalytics?.logEvent("menus", bundle)
                var intentWeb = Intent(baseContext, EventosWeb::class.java)
                intentWeb.putExtra("evento", evento)
                startActivity(intentWeb)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun seleccionarFotografiaDispositivo(v: View, solicitud: Int?) {
        val seleccionFotografiaIntent = Intent(Intent.ACTION_PICK)
        seleccionFotografiaIntent.type = "image/*"
        startActivityForResult(seleccionFotografiaIntent, solicitud!!)
    }

    fun subirAFirebaseStorage(opcion: Int, ficheroDispositivo: String?) {
        var progresoSubida: ProgressDialog = ProgressDialog(this)
        progresoSubida.setTitle("Subiendo...")
        progresoSubida.setMessage("Espere...")
        progresoSubida.setCancelable(true)
        progresoSubida.setCanceledOnTouchOutside(false)
        progresoSubida.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", { dialogInterface, i ->
            uploadTask!!.cancel()
        })

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
                    Log.e("EVENTO", "subir put file")
                    var file: Uri = Uri.fromFile(File(ficheroDispositivo))
                    uploadTask = imagenRef.putFile(file)
                    Log.e("EVENTO", "subir put file end")
                }
            }

            uploadTask!!.addOnFailureListener {
                subiendoDatos = false
                mostrarDialogo(applicationContext,
                        "Ha ocurrido un error al subir la imagen o el usuario ha cancelado la subida.", "")
            }.addOnSuccessListener {
                var datos: HashMap<String, String> = hashMapOf()
                datos.put("imagen", it.downloadUrl.toString())
                FirebaseFirestore.getInstance().collection("eventos")
                        .document(evento!!).set(datos.toMap(), SetOptions.merge())
                Picasso.with(applicationContext)
                        .load(it.downloadUrl.toString())
                        .error(R.mipmap.ic_launcher_round)
                        //.networkPolicy(NetworkPolicy.NO_CACHE)
                        .resize(300, 200)
                        .centerCrop()
                        .onlyScaleDown()
                        .into(imgImagen)
                progresoSubida.dismiss()
                subiendoDatos = false
                mostrarDialogo(applicationContext, "Imagen subida correctamente.", "")
            }.addOnProgressListener {
                if (!subiendoDatos!!) {
                    progresoSubida.show()
                    subiendoDatos = true
                } else {
                    if (it.totalByteCount > 0) {
                        progresoSubida.setMessage("Espere... " + (100 * it.bytesTransferred / it.totalByteCount) + "%")
                    }
                }
            }.addOnPausedListener {
                subiendoDatos = false
                mostrarDialogo(applicationContext, "La subida ha sido pausada.", "")
            }
        } catch (e: Exception) {
            mostrarDialogo(applicationContext, e.toString(), "")
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (imagenRef != null) {
            outState!!.putString("EXTRA_STORAGE_REFERENCE_KEY", imagenRef.toString())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        var stringRef: String = savedInstanceState!!.getString("EXTRA_STORAGE_REFERENCE_KEY")
        if (stringRef == null) {
            return
        }
        imagenRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef)
        var tasks: List<UploadTask> = imagenRef.activeUploadTasks

        for (task in tasks) {
            task.addOnFailureListener { exception ->
                upload_error(exception)
            }.addOnSuccessListener { taskSnapshot ->
                upload_exito(taskSnapshot)
            }.addOnProgressListener { taskSnapshot ->
                upload_progreso(taskSnapshot)
            }.addOnPausedListener { taskSnapshot ->
                upload_pausa(taskSnapshot)
            }
        }
    }

    private fun upload_pausa(taskSnapshot: UploadTask.TaskSnapshot) {
        subiendoDatos = false
        mostrarDialogo(applicationContext, "La subida ha sido pausada.", "")
    }

    private fun upload_progreso(taskSnapshot: UploadTask.TaskSnapshot) {
        if (!subiendoDatos!!) {
            var progresoSubida: ProgressDialog = ProgressDialog(this)
            progresoSubida.setTitle("Subiendo...")
            progresoSubida.setMessage("Espere...")
            progresoSubida.setCancelable(true)
            progresoSubida.setCanceledOnTouchOutside(false)
            progresoSubida.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", { dialogInterface, i ->
                uploadTask!!.cancel()
            })
            progresoSubida.show()
            subiendoDatos = true
        }
    }

    private fun upload_error(exception: Exception) {
        subiendoDatos = false
        mostrarDialogo(applicationContext,
                "Ha ocurrido un error al subir la imagen o el usuario ha cancelado la subida.", "")
    }

    private fun upload_exito(taskSnapshot: UploadTask.TaskSnapshot) {
        var datos: HashMap<String, String> = hashMapOf()
        datos.put("imagen", taskSnapshot.downloadUrl.toString())
        FirebaseFirestore.getInstance().collection("eventos")
                .document(evento!!).set(datos.toMap(), SetOptions.merge())
        Picasso.with(applicationContext)
                .load(taskSnapshot.downloadUrl.toString())
                .error(R.mipmap.ic_launcher_round)
                //.networkPolicy(NetworkPolicy.NO_CACHE)
                .resize(300, 200)
                .centerCrop()
                .onlyScaleDown()
                .into(imgImagen)
        progresoSubida!!.dismiss()
        subiendoDatos = false
        mostrarDialogo(applicationContext, "Imagen subida correctamente.", "")
    }

    fun descargarDeFirebaseStorage(fichero: String) {
        var referenciaFichero = getStorageReference().child(fichero)
        var rootPath: File = File(Environment.getExternalStorageDirectory(), "Eventos")
        if (!rootPath.exists()) {
            rootPath.mkdirs()
        }
        val localFile: File = File(rootPath, "$evento.jpg")
        referenciaFichero.getFile(localFile).addOnSuccessListener {
            mostrarDialogo(applicationContext, "Fichero descargado con éxito: " + localFile.toString(), "")
        }.addOnFailureListener {
            mostrarDialogo(applicationContext, "Error al descargar el fichero.", "")
        }
    }

    fun eliminarDeFirebaseStorage(fichero: String) {
        var storageRef = storage.getReferenceFromUrl("gs://eventos-3161f.appspot.com")
        var referenciaFichero = storageRef.child(fichero)
        referenciaFichero.delete()
                .addOnSuccessListener {
                    mostrarDialogo(applicationContext, "Fichero borrado con éxito", "")
                }.addOnFailureListener {
                    mostrarDialogo(applicationContext, "Error al borrar el fichero.", "")
                }
    }


}
