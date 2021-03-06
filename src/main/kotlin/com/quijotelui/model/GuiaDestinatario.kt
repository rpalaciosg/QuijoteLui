package com.quijotelui.model

import org.hibernate.annotations.Immutable
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Immutable
@Table(name = "v_ele_guias_receptor")
class GuiaDestinatario : Serializable {

    @Id
    @Column(name = "id")
    var id : Long? = null

    @Column(name = "codigo")
    var codigo : String? = null

    @Column(name = "numero")
    var numero : String? = null

    @Column(name = "documento")
    var documento : String? = null

    @Column(name = "razon_social")
    var razonSocial : String? = null

    @Column(name = "direccion")
    var direccion : String? = null

    @Column(name = "motivo_traslado")
    var motivoTraslado : String? = null

}