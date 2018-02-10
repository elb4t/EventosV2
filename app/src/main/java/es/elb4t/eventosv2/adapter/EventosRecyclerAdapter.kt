package es.elb4t.eventosv2.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import es.elb4t.eventosv2.R
import es.elb4t.eventosv2.model.EventoItem

/**
 * Created by eloy on 10/2/18.
 */
class EventosRecyclerAdapter(modelLayout: Int, ref: DatabaseReference, val mContext: Context) :
        FirebaseRecyclerAdapter<EventoItem, EventosRecyclerAdapter.EventoViewHolder>(
                EventoItem::class.java, modelLayout, EventosRecyclerAdapter.EventoViewHolder::class.java, ref) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.getContext())
                .inflate(mModelLayout, parent, false) as ViewGroup
        return EventoViewHolder(view)
    }

    override fun populateViewHolder(holder: EventoViewHolder, item: EventoItem, position: Int) {
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

    }

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtEvento: TextView = itemView.findViewById(R.id.txtEvento)
        val txtCiudad: TextView = itemView.findViewById(R.id.txtCiudad)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        val imagen: ImageView = itemView.findViewById(R.id.imgImagen)
    }
}
