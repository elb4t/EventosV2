package es.elb4t.eventosv2.model

/**
 * Created by eloy on 10/2/18.
 */

class EventoItem {
    var id: String? = null
    var evento: String? = null
    var ciudad: String? = null
    var fecha: String? = null
    var imagen: String? = null

    constructor() {}

    constructor(id: String, evento: String, ciudad: String,
                fecha: String, imagen: String) {
        this.id = id
        this.evento = evento
        this.ciudad = ciudad
        this.fecha = fecha
        this.imagen = imagen
    }
}