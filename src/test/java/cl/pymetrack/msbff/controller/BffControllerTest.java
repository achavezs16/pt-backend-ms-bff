package cl.pymetrack.msbff.controller;

import cl.pymetrack.msbff.dto.DashboardDTO;
import cl.pymetrack.msbff.service.BffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BffControllerTest {

    // 1. "Mockeamos" (Fingimos) el servicio para no depender de la base de datos real
    @Mock
    private BffService bffService;

    // 2. Inyectamos ese servicio falso en el controlador que vamos a probar
    @InjectMocks
    private BffController bffController;

    @BeforeEach
    void setUp() {
        // Este método se ejecuta antes de cada prueba para limpiar todo
    }

    @Test
    void getInfo_DeberiaRetornarInformacionDelBff() {
        // Actuar: Llamamos al método directamente
        ResponseEntity<Map<String, Object>> response = bffController.getInfo();

        // Afirmar: Verificamos que sea un éxito 200 OK y traiga datos
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ms-bff", response.getBody().get("service"));
    }

    @Test
    void getProductosEnriquecidos_DeberiaRetornarListaDeProductos() {
        // Preparar: Creamos datos falsos
        Long pymeId = 1L;
        List<Map<String, Object>> listaFalsa = new ArrayList<>();
        Map<String, Object> productoFalso = new HashMap<>();
        productoFalso.put("nombre", "Pan de Masa Madre");
        listaFalsa.add(productoFalso);

        // Le decimos a Mockito: "Cuando el controlador te pida los productos, devuélvele esta lista falsa"
        when(bffService.getProductosEnriquecidos(pymeId)).thenReturn(Mono.just(listaFalsa));

        // Actuar: Ejecutamos el controlador y "bloqueamos" (block) para extraer la respuesta
        ResponseEntity<List<Map<String, Object>>> response = bffController.getProductosEnriquecidos(pymeId, null).block();

        // Afirmar: Comprobamos rigurosamente el resultado
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Pan de Masa Madre", response.getBody().get(0).get("nombre"));
    }

    @Test
    void getDashboard_CuandoHayError_DeberiaRetornar503() {
        // Preparar: Simulamos que el servicio (o el bodeguero) falló o se cayó
        Long pymeId = 1L;
        when(bffService.getDashboardData(anyLong())).thenReturn(Mono.error(new RuntimeException("Servicio caído")));

        // Actuar: Ejecutamos el controlador
        ResponseEntity<DashboardDTO> response = bffController.getDashboard(pymeId).block();

        // Afirmar: Verificamos que el controlador capture el error y devuelva un 503
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }
}