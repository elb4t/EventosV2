package es.elb4t.eventosv2.web

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

/**
 * Created by eloy on 11/3/18.
 */
class DescargarFichero(private val ctx: Context) {

    fun Descargar(urlDescarga: String) {
        val fichero = urlDescarga.substring(
                urlDescarga.lastIndexOf("/") + 1,
                if (urlDescarga.indexOf("?") == -1) urlDescarga.length else urlDescarga.indexOf("?"))

        val file = File(Environment.getExternalStorageDirectory().absolutePath + "/EventosWeb/" + fichero)

        val request = DownloadManager.Request(Uri.parse(urlDescarga))
        request.setDescription("EventosWeb")
        request.setTitle(fichero)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationUri(Uri.fromFile(file))

        val manager = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}