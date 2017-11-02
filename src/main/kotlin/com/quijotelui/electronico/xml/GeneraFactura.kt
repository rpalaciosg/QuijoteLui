package com.quijotelui.electronico.xml

import com.quijotelui.electronico.comprobantes.InformacionTributaria
import com.quijotelui.electronico.comprobantes.factura.Factura
import com.quijotelui.electronico.util.Modulo11
import com.quijotelui.electronico.util.Parametros
import com.quijotelui.model.Contribuyente
import com.quijotelui.service.IFacturaService
import comprobantes.CampoAdicional
import comprobantes.InformacionAdicional
import comprobantes.factura.*
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.math.BigDecimal
import java.text.SimpleDateFormat
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller


class GeneraFactura(val facturaService : IFacturaService, val codigo : String, val numero : String) {


    val contribuyenteFactura = facturaService.findContribuyenteByComprobante(codigo, numero)

    val factura = Factura()

    fun xml(){

        factura.setInformacionTributaria(getInformacionTributaria())
        factura.setInformacionFactura(getInformacionFactura())


        val jaxbContext = JAXBContext.newInstance(Factura::class.java)
        val marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.setProperty("jaxb.encoding", "UTF-8")

        val stringWriter = StringWriter()
        stringWriter.use {
            marshaller.marshal(this.factura, stringWriter)
        }

//        val out = OutputStreamWriter(FileOutputStream("1234.xml"), "UTF-8")
//        marshaller.marshal(this.factura, out)

        println(stringWriter)
    }

    fun getInformacionTributaria() : InformacionTributaria{

        val informacionTributaria = InformacionTributaria()


        var contribuyente = getContribuyente(this.contribuyenteFactura)
        var facturaDocumento = getFactura(this.contribuyenteFactura)


        informacionTributaria.ambiente = Parametros.getAmbiente(facturaService.findParametroByNombre("Ambiente"))
        informacionTributaria.tipoEmision = Parametros.getEmision(facturaService.findParametroByNombre("Emisión"))
        informacionTributaria.razonSocial = contribuyente.razonSocial
        informacionTributaria.nombreComercial = contribuyente.nombreComercial
        informacionTributaria.ruc = contribuyente.ruc
        informacionTributaria.claveAcceso = getClaveAcceso(contribuyente, facturaDocumento, informacionTributaria.ambiente!!, informacionTributaria.tipoEmision!!)
        informacionTributaria.codDoc = facturaDocumento.codigoDocumento
        informacionTributaria.estab = facturaDocumento.establecimiento
        informacionTributaria.ptoEmi = facturaDocumento.puntoEmision
        informacionTributaria.secuencial = facturaDocumento.secuencial
        informacionTributaria.dirMatriz = contribuyente.direccion

        return informacionTributaria

    }

    fun getInformacionFactura() : InformacionFactura {

        val informacionFactura = InformacionFactura()

        var contribuyente = getContribuyente(this.contribuyenteFactura)
        var facturaComprobante = getFactura(this.contribuyenteFactura)

        informacionFactura.fechaEmision = SimpleDateFormat("dd/MM/yyyy").format(facturaComprobante.fecha)
        informacionFactura.dirEstablecimiento = facturaComprobante.direccionEstablecimiento
        informacionFactura.contribuyenteEspecial = contribuyente.contribuyenteEspecial
        informacionFactura.obligadoContabilidad = contribuyente.obligadoContabilidad
        informacionFactura.tipoIdentificacionComprador = facturaComprobante.tipoDocumento
        informacionFactura.razonSocialComprador = facturaComprobante.razonSocial
        informacionFactura.identificacionComprador = facturaComprobante.documento
        informacionFactura.direccionComprador = facturaComprobante.direccion
        informacionFactura.totalSinImpuestos = facturaComprobante.totalSinIva!!.setScale(2, BigDecimal.ROUND_HALF_UP) +
                facturaComprobante.totalConIva!!.setScale(2, BigDecimal.ROUND_HALF_UP)
        informacionFactura.totalDescuento = facturaComprobante.descuentos!!.setScale(2, BigDecimal.ROUND_HALF_UP)

        informacionFactura.setTotalConImpuestos(getImpuesto())

        informacionFactura.setPropina(BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP))
        informacionFactura.setImporteTotal(facturaComprobante.total!!.setScale(2, BigDecimal.ROUND_HALF_UP))
        informacionFactura.setMoneda("DOLAR")

        informacionFactura.setPagos(getPago())

        return informacionFactura

    }

    fun getImpuesto() : TotalConImpuestos {


        val impuestos = facturaService.findImpuestoByComprobante(codigo, numero)
        var totalConImpuestos = TotalConImpuestos()
        var totalImpuesto : TotalImpuesto

        for (impuesto in impuestos){
            totalImpuesto = TotalImpuesto()
            totalImpuesto.codigo = impuesto.codigoImpuesto
            totalImpuesto.codigoPorcentaje = impuesto.codigoPorcentaje
            totalImpuesto.baseImponible = impuesto.baseImponible
            totalImpuesto.tarifa = impuesto.tarifa
            totalImpuesto.valor = impuesto.valor

            totalConImpuestos.setTotalImpuesto(totalImpuesto)
        }

        return totalConImpuestos

    }

    fun getPago() : Pagos {

        val pagosComprobante = facturaService.findPagoByComprobante(codigo, numero)
        var pagos = Pagos()
        var pago : Pago

        for (pagoComprobante in pagosComprobante){
            pago = Pago()
            pago.formaPago = pagoComprobante.formaPago
            pago.total = pagoComprobante.total?.setScale(2, BigDecimal.ROUND_HALF_UP)
            pago.plazo = pagoComprobante.plazo
            pago.unidadTiempo = pagoComprobante.tiempo

            pagos.setPago(pago)
        }

        return pagos
    }

    fun getContribuyente(contribuyenteComprobante: MutableList<Any>) : Contribuyente {
        var contribuyente = Contribuyente()
        for (i in contribuyenteComprobante.indices) {
            val row = contribuyenteComprobante[i] as Array<Any>
            contribuyente = row[0] as Contribuyente
        }
        return contribuyente
    }

    fun getFactura (contribuyenteConprobante : MutableList<Any>) : com.quijotelui.model.Factura {
        var factura = com.quijotelui.model.Factura()
        for (i in contribuyenteConprobante.indices) {
            val row = contribuyenteConprobante[i] as Array<Any>
            factura = row[1] as com.quijotelui.model.Factura
        }
        return factura
    }

    fun getClaveAcceso(contribuyente: Contribuyente, factura: com.quijotelui.model.Factura, ambiente : String, emision : String) : String {

        val m11 = Modulo11()
        val claveAcceso = SimpleDateFormat("ddMMyyyy").format(factura.fecha) +
                factura.codigoDocumento + contribuyente.ruc + ambiente +
                factura.establecimiento + factura.puntoEmision + factura.secuencial +
                "12345678" + emision

        return claveAcceso + m11.modulo11(claveAcceso)
    }

    fun genera() {
        val informacionTributaria = InformacionTributaria()
        informacionTributaria.ambiente = "1"
        informacionTributaria.tipoEmision = "1"
        informacionTributaria.razonSocial = "Quiguango Jorge Luis"
        informacionTributaria.nombreComercial = "QuijoteLui"
        informacionTributaria.ruc = "102245612701"
        informacionTributaria.claveAcceso = "0000000000000000000000000000000000000000000"
        informacionTributaria.codDoc = "01"
        informacionTributaria.estab = "001"
        informacionTributaria.ptoEmi = "010"
        informacionTributaria.secuencial = "000000013"
        informacionTributaria.dirMatriz = "Cananvalle Calle Las Uvillas y Calle Cananvalle"


        val informacionFactura = InformacionFactura()
        informacionFactura.fechaEmision = "03/10/2017"
        informacionFactura.dirEstablecimiento = informacionTributaria.dirMatriz
        informacionFactura.obligadoContabilidad = "NO"
        informacionFactura.tipoIdentificacionComprador = "05"
        informacionFactura.razonSocialComprador = "Arenita"
        informacionFactura.identificacionComprador = "1002345644"
        informacionFactura.direccionComprador = "Priorato"
        informacionFactura.totalSinImpuestos = BigDecimal(10).setScale(2, BigDecimal.ROUND_HALF_UP)
        informacionFactura.totalDescuento = BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP)

        val totalImpuesto = TotalImpuesto()
        totalImpuesto.codigo = "2"
        totalImpuesto.codigoPorcentaje = "2"
        totalImpuesto.baseImponible = BigDecimal(10).setScale(2, BigDecimal.ROUND_HALF_UP)
        totalImpuesto.tarifa = BigDecimal(12).setScale(0, BigDecimal.ROUND_HALF_UP)
        totalImpuesto.valor = BigDecimal(1.20).setScale(2, BigDecimal.ROUND_HALF_UP)

        var totalConImpuestos = TotalConImpuestos()
        totalConImpuestos.setTotalImpuesto(totalImpuesto)

        informacionFactura.setTotalConImpuestos(totalConImpuestos)

        informacionFactura.setPropina(BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP))
        informacionFactura.setImporteTotal(BigDecimal(11.20).setScale(2, BigDecimal.ROUND_HALF_UP))
        informacionFactura.setMoneda("DOLAR")

        val pago = Pago()
        pago.formaPago = "20"
        pago.total = BigDecimal(11.20).setScale(2, BigDecimal.ROUND_HALF_UP)


        var pagos = Pagos()
        pagos.setPago(pago)

        informacionFactura.setPagos(pagos)


        val detalle = Detalle()
        detalle.codigoPrincipal = "1"
        detalle.descripcion = "Servicio de Pruebas"
        detalle.Cantidad = BigDecimal(1).setScale(2, BigDecimal.ROUND_HALF_UP)
        detalle.precioUnitario = BigDecimal(10).setScale(2, BigDecimal.ROUND_HALF_UP)
        detalle.descuento = BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP)
        detalle.precioTotalSinImpuesto = BigDecimal(10).setScale(2, BigDecimal.ROUND_HALF_UP)


        val impuesto = Impuesto()
        impuesto.codigo = "2"
        impuesto.codigoPorcentaje = "2"
        impuesto.tarifa = BigDecimal(12).setScale(2, BigDecimal.ROUND_HALF_UP)
        impuesto.baseImponible = BigDecimal(10).setScale(2, BigDecimal.ROUND_HALF_UP)
        impuesto.valor = BigDecimal(1.2).setScale(2, BigDecimal.ROUND_HALF_UP)

        var impuestos = Impuestos()
        impuestos.setImpuesto(impuesto)

        detalle.setImpuestos(impuestos)

        var detalles = Detalles()
        detalles.setDetalle(detalle)


        val campoAdicional = CampoAdicional()
        campoAdicional.setNombre("Teléfono")
        campoAdicional.setValor("999999999")

        var informacionAdicional = InformacionAdicional()
        informacionAdicional.setCampoAdicional(campoAdicional)

        val factura = Factura()

        factura.setId(id = "comprobante")
        factura.setVersion(version = "1.0.0")

        factura.setInformacionTributaria(informacionTributaria)
        factura.setInformacionFactura(informacionFactura)
        factura.setDetalles(detalles)
        factura.setInformacionAdicional(informacionAdicional)


        val jaxbContext = JAXBContext.newInstance(Factura::class.java)
        val marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.setProperty("jaxb.encoding", "UTF-8")

        val stringWriter = StringWriter()
        stringWriter.use {
            marshaller.marshal(factura, stringWriter)
        }

        val out = OutputStreamWriter(FileOutputStream("1234.xml"), "UTF-8")
        marshaller.marshal(factura, out)

        println(stringWriter)
    }

}
