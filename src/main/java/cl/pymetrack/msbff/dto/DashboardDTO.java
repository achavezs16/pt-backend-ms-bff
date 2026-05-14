package cl.pymetrack.msbff.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardDTO {

    private PymeInfo pymeInfo;
    private Estadisticas estadisticas;
    private List<PedidoReciente> pedidosRecientes;
    private List<ProductoActivo> productosActivos;
    private List<Alerta> alertas;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ultimaActualizacion;

    public DashboardDTO() {}

    // Getters y Setters
    public PymeInfo getPymeInfo() {
        return pymeInfo;
    }

    public void setPymeInfo(PymeInfo pymeInfo) {
        this.pymeInfo = pymeInfo;
    }

    public Estadisticas getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(Estadisticas estadisticas) {
        this.estadisticas = estadisticas;
    }

    public List<PedidoReciente> getPedidosRecientes() {
        return pedidosRecientes;
    }

    public void setPedidosRecientes(List<PedidoReciente> pedidosRecientes) {
        this.pedidosRecientes = pedidosRecientes;
    }

    public List<ProductoActivo> getProductosActivos() {
        return productosActivos;
    }

    public void setProductosActivos(List<ProductoActivo> productosActivos) {
        this.productosActivos = productosActivos;
    }

    public List<Alerta> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<Alerta> alertas) {
        this.alertas = alertas;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    // Clases internas
    public static class PymeInfo {
        private Long id;
        private String nombrePyme;
        private String emailContacto;
        private String rutPyme;
        private String estado;

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombrePyme() { return nombrePyme; }
        public void setNombrePyme(String nombrePyme) { this.nombrePyme = nombrePyme; }
        public String getEmailContacto() { return emailContacto; }
        public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
        public String getRutPyme() { return rutPyme; }
        public void setRutPyme(String rutPyme) { this.rutPyme = rutPyme; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }

    public static class Estadisticas {
        private Long pedidosTotales;
        private Long pedidosHoy;
        private Long productosActivos;
        private Long stockBajo;
        private Double ingresosTotales;
        private Double ingresosHoy;

        // Getters y Setters
        public Long getPedidosTotales() { return pedidosTotales; }
        public void setPedidosTotales(Long pedidosTotales) { this.pedidosTotales = pedidosTotales; }
        public Long getPedidosHoy() { return pedidosHoy; }
        public void setPedidosHoy(Long pedidosHoy) { this.pedidosHoy = pedidosHoy; }
        public Long getProductosActivos() { return productosActivos; }
        public void setProductosActivos(Long productosActivos) { this.productosActivos = productosActivos; }
        public Long getStockBajo() { return stockBajo; }
        public void setStockBajo(Long stockBajo) { this.stockBajo = stockBajo; }
        public Double getIngresosTotales() { return ingresosTotales; }
        public void setIngresosTotales(Double ingresosTotales) { this.ingresosTotales = ingresosTotales; }
        public Double getIngresosHoy() { return ingresosHoy; }
        public void setIngresosHoy(Double ingresosHoy) { this.ingresosHoy = ingresosHoy; }
    }

    public static class PedidoReciente {
        private Long id;
        private String numeroOrden;
        private String cliente;
        private String estado;
        private Double total;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private String fecha;

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNumeroOrden() { return numeroOrden; }
        public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }
        public String getCliente() { return cliente; }
        public void setCliente(String cliente) { this.cliente = cliente; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }
    }

    public static class ProductoActivo {
        private Long id;
        private String nombreProducto;
        private String codigoSKU;
        private Integer stock;
        private Double precio;
        private String categoria;
        private String imagen;

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombreProducto() { return nombreProducto; }
        public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
        public String getCodigoSKU() { return codigoSKU; }
        public void setCodigoSKU(String codigoSKU) { this.codigoSKU = codigoSKU; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        public Double getPrecio() { return precio; }
        public void setPrecio(Double precio) { this.precio = precio; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }
    }

    public static class Alerta {
        private String tipo;
        private String mensaje;
        private String nivel; // INFO, WARNING, ERROR
        private Long count;

        // Getters y Setters
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        public String getNivel() { return nivel; }
        public void setNivel(String nivel) { this.nivel = nivel; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }
}
