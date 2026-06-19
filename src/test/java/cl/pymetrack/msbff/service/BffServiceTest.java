package cl.pymetrack.msbff.service;

import cl.pymetrack.msbff.client.MicroserviceClient;
import cl.pymetrack.msbff.dto.DashboardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BffServiceTest {

    @Mock
    private MicroserviceClient microserviceClient;

    @InjectMocks
    private BffService bffService;

    private List<Map<String, Object>> mockProductos;
    private List<Map<String, Object>> mockPedidos;

    @BeforeEach
    void setUp() {
        // Preparamos datos simulados ricos en casuísticas
        mockProductos = new ArrayList<>();
        Map<String, Object> producto = new HashMap<>();
        producto.put("id", 100); // Integer para forzar la conversión a Long
        producto.put("nombreProducto", "Producto Test");
        producto.put("codigoSKU", "SKU123");
        producto.put("precioVentaChile", 1500.50); // Double
        producto.put("categoriaProducto", "Electrónica");
        producto.put("imagenUrl", "url.jpg");
        producto.put("activo", true);
        mockProductos.add(producto);

        mockPedidos = new ArrayList<>();
        Map<String, Object> pedido1 = new HashMap<>();
        pedido1.put("id", "200"); // String para forzar conversión a Long
        pedido1.put("numeroOrdenPyme", "ORD-001");
        pedido1.put("nombreCliente", "Juan Perez");
        pedido1.put("estadoPedidoPyme", "DISPONIBLE"); // Forzamos alerta activa
        pedido1.put("totalPedido", "5000"); // String para conversión a Double
        pedido1.put("creadoEn", LocalDate.now().toString() + "T10:00:00"); // Forzamos estadísticas de 'hoy'
        mockPedidos.add(pedido1);
        
        Map<String, Object> pedido2 = new HashMap<>();
        pedido2.put("id", 201L); // Long directo
        pedido2.put("estadoPedidoPyme", "ENTREGADO_CHILE"); // No genera alerta activa
        pedido2.put("totalPedido", 3000.0);
        pedido2.put("creadoEn", "2020-01-01T10:00:00"); // Creado en el pasado
        mockPedidos.add(pedido2);
    }

    @Test
    void getDashboardData_DebeConstruirDashboardConInformacionCompleta() {
        when(microserviceClient.getProductosByPyme(2L)).thenReturn(Mono.just(mockProductos));
        when(microserviceClient.getPedidosByPyme(2L)).thenReturn(Mono.just(mockPedidos));

        // Forzamos el ID 2 para pasar por el bloque "EcoMarket Demo"
        DashboardDTO result = bffService.getDashboardData(2L).block();

        assertNotNull(result);
        assertEquals("EcoMarket Demo", result.getPymeInfo().getNombrePyme());
        assertEquals(1, result.getEstadisticas().getProductosActivos());
        assertEquals(2, result.getEstadisticas().getPedidosTotales());
        // Pedidos de hoy debe ser 1 (pedido1)
        assertEquals(1, result.getEstadisticas().getPedidosHoy()); 
        // Ingresos totales = 5000 + 3000 = 8000
        assertEquals(8000.0, result.getEstadisticas().getIngresosTotales()); 
        assertEquals(1, result.getAlertas().size()); // Solo debe haber alerta de pedidos activos
    }

    @Test
    void getDashboardData_ConPymeDistintaA2_Y_SinProductos() {
        // Simulamos lista vacía para forzar la alerta "SIN_PRODUCTOS"
        when(microserviceClient.getProductosByPyme(99L)).thenReturn(Mono.just(Collections.emptyList()));
        when(microserviceClient.getPedidosByPyme(99L)).thenReturn(Mono.just(mockPedidos));

        // ID 99 forzará el bloque "TechStore Demo"
        DashboardDTO result = bffService.getDashboardData(99L).block();

        assertNotNull(result);
        assertEquals("TechStore Demo", result.getPymeInfo().getNombrePyme());
        
        // Debe haber 2 alertas: PEDIDOS_ACTIVOS y SIN_PRODUCTOS
        assertEquals(2, result.getAlertas().size());
        assertTrue(result.getAlertas().stream().anyMatch(a -> a.getTipo().equals("SIN_PRODUCTOS")));
    }

    @Test
    void getEstadisticasPyme_DebeCalcularMapeoCorrectamente() {
        when(microserviceClient.getProductosByPyme(1L)).thenReturn(Mono.just(mockProductos));
        when(microserviceClient.getPedidosByPyme(1L)).thenReturn(Mono.just(mockPedidos));

        Map<String, Object> stats = bffService.getEstadisticasPyme(1L).block();

        assertNotNull(stats);
        assertEquals(1, stats.get("totalProductos"));
        assertEquals(2, stats.get("totalPedidos"));
        assertEquals(1L, stats.get("pedidosHoy"));
        assertEquals(8000.0, stats.get("ingresosTotales"));
        assertEquals(5000.0, stats.get("ingresosHoy")); // Solo el pedido1 es de hoy
    }

    @Test
    void getProductosEnriquecidos_DebeAñadirCampoDisponibleParaVenta() {
        when(microserviceClient.getProductosByPyme(anyLong())).thenReturn(Mono.just(mockProductos));

        List<Map<String, Object>> result = bffService.getProductosEnriquecidos(1L).block();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue((Boolean) result.get(0).get("disponibleParaVenta")); // Verificamos que leyó el 'activo' true
    }

    @Test
    void getPedidosEnriquecidos_DebeFormatearEstadoLegible() {
        // Añadimos un estado con guión bajo para forzar el replace("_", " ")
        Map<String, Object> pedidoEspecial = new HashMap<>();
        pedidoEspecial.put("estadoPedidoPyme", "PENDIENTE_CHILE");
        mockPedidos.add(pedidoEspecial);

        when(microserviceClient.getPedidosByPyme(anyLong())).thenReturn(Mono.just(mockPedidos));

        List<Map<String, Object>> result = bffService.getPedidosEnriquecidos(1L).block();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("PENDIENTE CHILE", result.get(2).get("estadoLegible")); // Verifica el reemplazo
    }

    @Test
    void getHealthStatus_CuandoTodosLosServiciosEstanArriba() {
        Map<String, Boolean> mockHealth = new HashMap<>();
        mockHealth.put("auth", true);
        mockHealth.put("products", true);

        when(microserviceClient.checkServicesHealth()).thenReturn(Mono.just(mockHealth));

        Map<String, Object> health = bffService.getHealthStatus().block();

        assertNotNull(health);
        assertEquals("UP", health.get("bff"));
        assertEquals("UP", health.get("overall")); // Coinciden activos con total
        assertEquals(2L, health.get("activeServices"));
    }

    @Test
    void getHealthStatus_CuandoHayServiciosCaidos() {
        Map<String, Boolean> mockHealth = new HashMap<>();
        mockHealth.put("auth", true);
        mockHealth.put("products", false); // Un servicio abajo

        when(microserviceClient.checkServicesHealth()).thenReturn(Mono.just(mockHealth));

        Map<String, Object> health = bffService.getHealthStatus().block();

        assertNotNull(health);
        assertEquals("DEGRADED", health.get("overall")); // No coinciden
        assertEquals(1L, health.get("activeServices"));
    }

    @Test
    void getAdminStats_DebeRetornarDatosCombinados() {
        Map<String, Object> mockAdminStats = new HashMap<>();
        mockAdminStats.put("usuarios", 50);

        when(microserviceClient.getAdminStats(anyString())).thenReturn(Mono.just(mockAdminStats));
        when(microserviceClient.getAllPedidos()).thenReturn(Mono.just(mockPedidos));

        Map<String, Object> adminStats = bffService.getAdminStats("Bearer token").block();

        assertNotNull(adminStats);
        assertEquals(50, adminStats.get("usuarios"));
        assertEquals(2, adminStats.get("totalPedidos"));
        assertNotNull(adminStats.get("ultimaActualizacion"));
    }
    
    // Prueba de borde: Manejo de valores nulos en conversiones privadas
    @Test
    void metodosDeConversion_DeberianManejarNulosCorrectamente() {
        // Forzamos un pedido con valores nulos para que pase por los "if (value == null)" de toLong y toDouble
        Map<String, Object> pedidoNulo = new HashMap<>();
        pedidoNulo.put("totalPedido", null);
        pedidoNulo.put("estadoPedidoPyme", null);
        
        when(microserviceClient.getPedidosByPyme(1L)).thenReturn(Mono.just(Collections.singletonList(pedidoNulo)));
        
        List<Map<String, Object>> result = bffService.getPedidosEnriquecidos(1L).block();
        
        assertNotNull(result);
        assertEquals("SIN ESTADO", result.get(0).get("estadoLegible"));
    }
}