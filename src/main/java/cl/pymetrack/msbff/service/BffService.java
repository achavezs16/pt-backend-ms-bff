package cl.pymetrack.msbff.service;

import cl.pymetrack.msbff.client.MicroserviceClient;
import cl.pymetrack.msbff.dto.DashboardDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BffService {

    private static final Logger logger = LoggerFactory.getLogger(BffService.class);

    private final MicroserviceClient microserviceClient;

    public BffService(MicroserviceClient microserviceClient) {
        this.microserviceClient = microserviceClient;
    }

    public Mono<DashboardDTO> getDashboardData(Long pymeId) {
        logger.info("Construyendo dashboard para PYME {}", pymeId);

        return Mono.zip(
                microserviceClient.getProductosByPyme(pymeId),
                microserviceClient.getPedidosByPyme(pymeId)
        ).map(tuple -> buildDashboardDTO(pymeId, tuple.getT1(), tuple.getT2()));
    }

    public Mono<Map<String, Object>> getEstadisticasPyme(Long pymeId) {
        return Mono.zip(
                microserviceClient.getProductosByPyme(pymeId),
                microserviceClient.getPedidosByPyme(pymeId)
        ).map(tuple -> {
            List<Map<String, Object>> productos = tuple.getT1();
            List<Map<String, Object>> pedidos = tuple.getT2();

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalProductos", productos.size());
            estadisticas.put("totalPedidos", pedidos.size());
            estadisticas.put("pedidosHoy", calcularPedidosHoy(pedidos));
            estadisticas.put("ingresosTotales", calcularIngresosTotales(pedidos));
            estadisticas.put("ingresosHoy", calcularIngresosHoy(pedidos));
            estadisticas.put("stockBajo", 0L);

            return estadisticas;
        });
    }

    public Mono<List<Map<String, Object>>> getProductosEnriquecidos(Long pymeId) {
        return microserviceClient.getProductosByPyme(pymeId)
                .map(productos -> productos.stream()
                        .map(this::enriquecerProducto)
                        .collect(Collectors.toList()));
    }

    public Mono<List<Map<String, Object>>> getPedidosEnriquecidos(Long pymeId) {
        return microserviceClient.getPedidosByPyme(pymeId)
                .map(pedidos -> pedidos.stream()
                        .map(this::enriquecerPedido)
                        .collect(Collectors.toList()));
    }

    public Mono<Map<String, Object>> getHealthStatus() {
        return microserviceClient.checkServicesHealth()
                .map(health -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("services", health);
                    status.put("bff", "UP");
                    status.put("timestamp", LocalDateTime.now());

                    long active = health.values().stream().filter(Boolean::booleanValue).count();
                    status.put("overall", active == health.size() ? "UP" : "DEGRADED");
                    status.put("activeServices", active);
                    status.put("totalServices", health.size());

                    return status;
                });
    }

    private DashboardDTO buildDashboardDTO(
            Long pymeId,
            List<Map<String, Object>> productos,
            List<Map<String, Object>> pedidos
    ) {
        DashboardDTO dashboard = new DashboardDTO();

        dashboard.setPymeInfo(buildPymeInfo(pymeId));
        dashboard.setEstadisticas(buildEstadisticas(productos, pedidos));
        dashboard.setPedidosRecientes(buildPedidosRecientes(pedidos));
        dashboard.setProductosActivos(buildProductosActivos(productos));
        dashboard.setAlertas(buildAlertas(productos, pedidos));
        dashboard.setUltimaActualizacion(LocalDateTime.now());

        return dashboard;
    }

    private DashboardDTO.PymeInfo buildPymeInfo(Long pymeId) {
        DashboardDTO.PymeInfo info = new DashboardDTO.PymeInfo();
        info.setId(pymeId);

        if (pymeId.equals(2L)) {
            info.setNombrePyme("EcoMarket Demo");
            info.setEmailContacto("contacto@ecomarket.cl");
            info.setRutPyme("76.222.222-2");
        } else {
            info.setNombrePyme("TechStore Demo");
            info.setEmailContacto("contacto@techstore.cl");
            info.setRutPyme("76.111.111-1");
        }

        info.setEstado("ACTIVA");
        return info;
    }

    private DashboardDTO.Estadisticas buildEstadisticas(
            List<Map<String, Object>> productos,
            List<Map<String, Object>> pedidos
    ) {
        DashboardDTO.Estadisticas estadisticas = new DashboardDTO.Estadisticas();
        estadisticas.setProductosActivos((long) productos.size());
        estadisticas.setPedidosTotales((long) pedidos.size());
        estadisticas.setPedidosHoy(calcularPedidosHoy(pedidos));
        estadisticas.setIngresosTotales(calcularIngresosTotales(pedidos));
        estadisticas.setIngresosHoy(calcularIngresosHoy(pedidos));
        estadisticas.setStockBajo(0L);
        return estadisticas;
    }

    private List<DashboardDTO.PedidoReciente> buildPedidosRecientes(List<Map<String, Object>> pedidos) {
        return pedidos.stream()
                .limit(5)
                .map(pedido -> {
                    DashboardDTO.PedidoReciente dto = new DashboardDTO.PedidoReciente();
                    dto.setId(toLong(pedido.get("id")));
                    dto.setNumeroOrden(asString(pedido.get("numeroOrdenPyme")));
                    dto.setCliente(asString(pedido.get("nombreCliente")));
                    dto.setEstado(asString(pedido.get("estadoPedidoPyme")));
                    dto.setTotal(toDouble(pedido.get("totalPedido")));
                    dto.setFecha(asString(pedido.get("creadoEn")));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<DashboardDTO.ProductoActivo> buildProductosActivos(List<Map<String, Object>> productos) {
        return productos.stream()
                .limit(10)
                .map(producto -> {
                    DashboardDTO.ProductoActivo dto = new DashboardDTO.ProductoActivo();
                    dto.setId(toLong(producto.get("id")));
                    dto.setNombreProducto(asString(producto.get("nombreProducto")));
                    dto.setCodigoSKU(asString(producto.get("codigoSKU")));
                    dto.setStock(0);
                    dto.setPrecio(toDouble(producto.get("precioVentaChile")));
                    dto.setCategoria(asString(producto.get("categoriaProducto")));
                    dto.setImagen(asString(producto.get("imagenUrl")));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private List<DashboardDTO.Alerta> buildAlertas(
            List<Map<String, Object>> productos,
            List<Map<String, Object>> pedidos
    ) {
        List<DashboardDTO.Alerta> alertas = new ArrayList<>();

        long pedidosPendientes = pedidos.stream()
                .filter(p -> {
                    String estado = asString(p.get("estadoPedidoPyme"));
                    return estado != null && (
                            estado.equals("DISPONIBLE")
                                    || estado.equals("ASIGNADO")
                                    || estado.equals("PENDIENTE_CHILE")
                                    || estado.equals("CONFIRMADO_CHILE")
                    );
                })
                .count();

        if (pedidosPendientes > 0) {
            DashboardDTO.Alerta alerta = new DashboardDTO.Alerta();
            alerta.setTipo("PEDIDOS_ACTIVOS");
            alerta.setNivel("INFO");
            alerta.setMensaje("Hay " + pedidosPendientes + " pedidos activos en operación.");
            alerta.setCount(pedidosPendientes);
            alertas.add(alerta);
        }

        if (productos.isEmpty()) {
            DashboardDTO.Alerta alerta = new DashboardDTO.Alerta();
            alerta.setTipo("SIN_PRODUCTOS");
            alerta.setNivel("WARNING");
            alerta.setMensaje("La PYME no tiene productos activos registrados.");
            alerta.setCount(0L);
            alertas.add(alerta);
        }

        return alertas;
    }

    private Map<String, Object> enriquecerProducto(Map<String, Object> producto) {
        Map<String, Object> result = new HashMap<>(producto);
        result.put("disponibleParaVenta", Boolean.TRUE.equals(producto.get("activo")));
        return result;
    }

    private Map<String, Object> enriquecerPedido(Map<String, Object> pedido) {
        Map<String, Object> result = new HashMap<>(pedido);
        String estado = asString(pedido.get("estadoPedidoPyme"));
        result.put("estadoLegible", estado != null ? estado.replace("_", " ") : "SIN ESTADO");
        return result;
    }

    private long calcularPedidosHoy(List<Map<String, Object>> pedidos) {
        String today = LocalDate.now().toString();
        return pedidos.stream()
                .filter(p -> asString(p.get("creadoEn")) != null && asString(p.get("creadoEn")).startsWith(today))
                .count();
    }

    private double calcularIngresosTotales(List<Map<String, Object>> pedidos) {
        return pedidos.stream()
                .mapToDouble(p -> toDouble(p.get("totalPedido")))
                .sum();
    }

    private double calcularIngresosHoy(List<Map<String, Object>> pedidos) {
        String today = LocalDate.now().toString();
        return pedidos.stream()
                .filter(p -> asString(p.get("creadoEn")) != null && asString(p.get("creadoEn")).startsWith(today))
                .mapToDouble(p -> toDouble(p.get("totalPedido")))
                .sum();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(value.toString());
    }

    private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number number) return number.doubleValue();
        return Double.parseDouble(value.toString());
    }

    public Mono<Map<String, Object>> getAdminStats(String authorizationHeader) {
        return Mono.zip(
                microserviceClient.getAdminStats(authorizationHeader),
                microserviceClient.getAllPedidos()
        ).map(tuple -> {
            Map<String, Object> userStats = tuple.getT1();
            List<Map<String, Object>> pedidos = tuple.getT2();

            Map<String, Object> result = new HashMap<>(userStats);
            result.put("totalPedidos", pedidos.size());
            result.put("ultimaActualizacion", LocalDateTime.now());

            return result;
        });
    }
}
