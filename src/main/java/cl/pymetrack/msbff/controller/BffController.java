package cl.pymetrack.msbff.controller;

import cl.pymetrack.msbff.dto.DashboardDTO;
import cl.pymetrack.msbff.service.BffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bff")
@Tag(name = "Backend for Frontend", description = "API BFF para agregación y transformación de datos")
public class BffController {

    private static final Logger logger = LoggerFactory.getLogger(BffController.class);

    @Autowired
    private BffService bffService;

    @GetMapping("/dashboard/{pymeId}")
    @Operation(summary = "Obtener dashboard completo", description = "Retorna datos agregados del dashboard para una PYME específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard obtenido exitosamente",
                content = @Content(schema = @Schema(implementation = DashboardDTO.class))),
        @ApiResponse(responseCode = "404", description = "PYME no encontrada"),
        @ApiResponse(responseCode = "503", description = "Servicios no disponibles")
    })
    public Mono<ResponseEntity<DashboardDTO>> getDashboard(
            @Parameter(description = "ID de la PYME", required = true)
            @PathVariable Long pymeId) {
        
        logger.info("Petición de dashboard para PYME: {}", pymeId);
        
        return bffService.getDashboardData(pymeId)
                .map(dashboard -> {
                    logger.info("Dashboard construido exitosamente para PYME: {}", pymeId);
                    return ResponseEntity.ok(dashboard);
                })
                .onErrorResume(error -> {
                    logger.error("Error al obtener dashboard para PYME {}: {}", pymeId, error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                });
    }

    @GetMapping("/pymes/{pymeId}/estadisticas")
    @Operation(summary = "Obtener estadísticas de PYME", description = "Retorna estadísticas agregadas de una PYME")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "404", description = "PYME no encontrada"),
        @ApiResponse(responseCode = "503", description = "Servicios no disponibles")
    })
    public Mono<ResponseEntity<Map<String, Object>>> getEstadisticasPyme(
            @Parameter(description = "ID de la PYME", required = true)
            @PathVariable Long pymeId) {
        
        logger.info("Petición de estadísticas para PYME: {}", pymeId);
        
        return bffService.getEstadisticasPyme(pymeId)
                .map(estadisticas -> ResponseEntity.ok(estadisticas))
                .onErrorResume(error -> {
                    logger.error("Error al obtener estadísticas para PYME {}: {}", pymeId, error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                });
    }

    @GetMapping("/pymes/{pymeId}/productos")
    @Operation(summary = "Obtener productos enriquecidos", description = "Retorna productos con información adicional agregada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente"),
        @ApiResponse(responseCode = "404", description = "PYME no encontrada"),
        @ApiResponse(responseCode = "503", description = "Servicios no disponibles")
    })
    public Mono<ResponseEntity<List<Map<String, Object>>>> getProductosEnriquecidos(
            @Parameter(description = "ID de la PYME", required = true)
            @PathVariable Long pymeId,
            @Parameter(description = "Categoría para filtrar (opcional)")
            @RequestParam(required = false) String categoria) {
        
        logger.info("Petición de productos enriquecidos para PYME: {}", pymeId);
        
        return bffService.getProductosEnriquecidos(pymeId)
                .map(productos -> {
                    // Aquí podríamos filtrar por categoría si fuera necesario
                    List<Map<String, Object>> productosFiltrados = filtrarPorCategoria(productos, categoria);
                    return ResponseEntity.ok(productosFiltrados);
                })
                .onErrorResume(error -> {
                    logger.error("Error al obtener productos enriquecidos para PYME {}: {}", pymeId, error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                });
    }

    @GetMapping("/pymes/{pymeId}/pedidos")
    @Operation(summary = "Obtener pedidos enriquecidos", description = "Retorna pedidos con información adicional agregada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedidos obtenidos exitosamente"),
        @ApiResponse(responseCode = "404", description = "PYME no encontrada"),
        @ApiResponse(responseCode = "503", description = "Servicios no disponibles")
    })
    public Mono<ResponseEntity<List<Map<String, Object>>>> getPedidosEnriquecidos(
            @Parameter(description = "ID de la PYME", required = true)
            @PathVariable Long pymeId,
            @Parameter(description = "Estado para filtrar (opcional)")
            @RequestParam(required = false) String estado) {
        
        logger.info("Petición de pedidos enriquecidos para PYME: {}", pymeId);
        
        return bffService.getPedidosEnriquecidos(pymeId)
                .map(pedidos -> {
                    // Aquí podríamos filtrar por estado si fuera necesario
                    List<Map<String, Object>> pedidosFiltrados = filtrarPorEstado(pedidos, estado);
                    return ResponseEntity.ok(pedidosFiltrados);
                })
                .onErrorResume(error -> {
                    logger.error("Error al obtener pedidos enriquecidos para PYME {}: {}", pymeId, error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                });
    }

    @GetMapping("/pymes/{pymeId}/resumen")
    @Operation(summary = "Obtener resumen de PYME", description = "Retorna un resumen rápido con información clave de la PYME")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "PYME no encontrada"),
        @ApiResponse(responseCode = "503", description = "Servicios no disponibles")
    })
    public Mono<ResponseEntity<Map<String, Object>>> getResumenPyme(
            @Parameter(description = "ID de la PYME", required = true)
            @PathVariable Long pymeId) {
        
        logger.info("Petición de resumen para PYME: {}", pymeId);
        
        // Combinar múltiples llamadas para obtener un resumen completo
        Mono<DashboardDTO> pymeData = bffService.getDashboardData(pymeId);
        Mono<Map<String, Object>> estadisticas = bffService.getEstadisticasPyme(pymeId);
        
        return Mono.zip(pymeData, estadisticas)
                .map(tuple -> {
                    Map<String, Object> resumen = new HashMap<>();
                    DashboardDTO dashboard = (DashboardDTO) tuple.getT1();
                    Map<String, Object> stats = tuple.getT2();
                    
                    // Información básica
                    resumen.put("pyme", dashboard.getPymeInfo());
                    
                    // Métricas clave
                    resumen.put("pedidosTotales", stats.get("totalPedidos"));
                    resumen.put("productosActivos", stats.get("totalProductos"));
                    resumen.put("inggresosHoy", stats.get("ingresosHoy"));
                    
                    // Alertas importantes
                    resumen.put("alertas", dashboard.getAlertas());
                    resumen.put("ultimaActualizacion", dashboard.getUltimaActualizacion());
                    
                    return ResponseEntity.ok(resumen);
                })
                .onErrorResume(error -> {
                    logger.error("Error al obtener resumen para PYME {}: {}", pymeId, error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
                });
    }

    @GetMapping("/health")
    @Operation(summary = "Health check del BFF", description = "Verifica el estado del BFF y los microservicios conectados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "BFF y servicios operativos"),
        @ApiResponse(responseCode = "503", description = "Algunos servicios no disponibles")
    })
    public Mono<ResponseEntity<Map<String, Object>>> getHealthStatus() {
        logger.debug("Petición de health status del BFF");
        
        return bffService.getHealthStatus()
                .map(health -> {
                    if ("UP".equals(health.get("overall"))) {
                        return ResponseEntity.ok(health);
                    } else {
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
                    }
                });
    }

    @GetMapping("/info")
    @Operation(summary = "Información del BFF", description = "Retorna información sobre el servicio BFF y microservicios conectados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente")
    })
    public ResponseEntity<Map<String, Object>> getInfo() {
        logger.debug("Petición de información del BFF");
        
        Map<String, Object> info = new HashMap<>();
        info.put("service", "ms-bff");
        info.put("version", "1.0.0");
        info.put("description", "Backend for Frontend para PymeTrack");
        info.put("endpoints", List.of(
            "/api/v1/bff/dashboard/{pymeId}",
            "/api/v1/bff/pymes/{pymeId}/estadisticas",
            "/api/v1/bff/pymes/{pymeId}/productos",
            "/api/v1/bff/pymes/{pymeId}/pedidos",
            "/api/v1/bff/pymes/{pymeId}/resumen",
            "/api/v1/bff/health",
            "/api/v1/bff/info"
        ));
        info.put("connectedServices", Map.of(
            "ms-user", "Servicio de usuarios y autenticación",
            "ms-productos", "Servicio de productos e inventario",
            "ms-pedidos", "Servicio de pedidos y estados"
        ));
        info.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(info);
    }

    // Métodos auxiliares
    private List<Map<String, Object>> filtrarPorCategoria(List<Map<String, Object>> productos, String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            return productos;
        }
        
        return productos.stream()
                .filter(producto -> {
                    // Lógica de filtrado - en implementación real, verificaríamos la categoría
                    return true; // Por ahora, no filtramos
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private List<Map<String, Object>> filtrarPorEstado(List<Map<String, Object>> pedidos, String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return pedidos;
        }
        
        return pedidos.stream()
                .filter(pedido -> {
                    // Lógica de filtrado - en implementación real, verificaríamos el estado
                    return true; // Por ahora, no filtramos
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
