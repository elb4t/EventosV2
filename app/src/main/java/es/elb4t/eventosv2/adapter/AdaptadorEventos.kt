package es.elb4t.eventosv2.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.squareup.picasso.Picasso
import es.elb4t.eventosv2.EventoDetalles
import es.elb4t.eventosv2.R
import es.elb4t.eventosv2.model.Evento

/**
 * Created by eloy on 10/2/18.
 */
class AdaptadorEventos(private val mContext: Context, val options: FirestoreRecyclerOptions<Evento>) :
        FirestoreRecyclerAdapter<Evento, AdaptadorEventos.EventoViewHolder>(options) {

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int, item: Evento) {
        val txtEvento = item.evento
        val txtCiudad = item.ciudad
        val txtFecha = item.fecha

        holder.txtEvento.text = txtEvento
        holder.txtCiudad.text = txtCiudad
        holder.txtFecha.text = txtFecha
        Picasso.with(mContext)
                .load(item.imagen)
                .error(R.mipmap.ic_launcher_round)
                //.networkPolicy(NetworkPolicy.NO_CACHE)
                .resize(300, 200)
                .centerCrop()
                .onlyScaleDown()
                .into(holder.imagen)
        holder.itemView.setOnClickListener {
            val idEvento = options.snapshots.getSnapshot(position).id
            val intent = Intent(mContext, EventoDetalles::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("evento",idEvento)
            mContext.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
         val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.evento, parent, false) as ViewGroup
        return EventoViewHolder(view)
    }

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtEvento: TextView = itemView.findViewById(R.id.txtEvento)
        val txtCiudad: TextView = itemView.findViewById(R.id.txtCiudad)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        val imagen: ImageView = itemView.findViewById(R.id.imgImagen)
    }
}
