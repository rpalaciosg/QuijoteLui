package com.quijotelui.model

import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Type
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Immutable
@Table(name = "v_ele_facturas")
class Factura : Serializable {

    @Id
    @Column(name = "id")
    var id : Long? = null

    @Column(name = "id_contribuyente")
    var idContribuyente : Long? = null

    @Column(name = "codigo")
    var codigo : String? = null

    @Column(name = "numero")
    var numero : String? = null

    @Column(name = "codigo_documento")
    var codigoDocumento : String? = null

    @Column(name = "establecimiento")
    var establecimiento : String? = null

    @Column(name = "punto_emision")
    var puntoEmision : String? = null

    @Column(name = "secuencial")
    var secuencial : String? = null

    @Column(name = "fecha")
    @Type(type="date")
    var fecha : Date? = null

    @Column(name = "total_sin_iva")
    var totalSinIva : BigDecimal? = null

    @Column(name = "total_con_iva")
    var totalConIva : BigDecimal? = null

    @Column(name = "iva")
    var iva : BigDecimal? = null

    @Column(name = "descuentos")
    var descuentos : BigDecimal? = null

    @Column(name = "total")
    var total : BigDecimal? = null

    @Column(name = "tipo_documento")
    var tipoDocumento : String? = null

    @Column(name = "documento")
    var documento : String? = null

    @Column(name = "razon_social")
    var razonSocial : String? = null

    @Column(name = "direccion")
    var direccion : String? = null

    @Column(name = "guia_remision")
    var guiaRemision : String? = null

    @Column(name = "direccion_establecimiento")
    var direccionEstablecimiento : String? = null

}