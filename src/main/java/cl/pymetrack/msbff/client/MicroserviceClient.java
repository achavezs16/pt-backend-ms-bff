package cl.pymetrack.msbff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;


@Component
public class MicroserviceClient {

    private static final Logger logger = LoggerFactory.getLogger(MicroserviceClient.class);

    private final WebClient webClient;

    @Value("${services.ms-user:http://172.31.84.36:8085/api/v1}")
    private String msUserUrl;

    @Value("${services.ms-productos:http://172.31.93.143:8081/api/v1}")
    private String msProductosUrl;

    @Value("${services.ms-pedidos:http://172.31.76.154:8082/api/v1}")
    private String msPedidosUrl;

    public MicroserviceClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    public Mono<List<Map<String, Object>>> getProductosByPyme(Long pymeId) {
        logger.debug("Obteniendo productos de PYME {} desde ms-productos", pymeId);

        return webClient.get()
                .uri(msProductosUrl + "/productos/pyme/" + pymeId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .timeout(Duration.ofSeconds(5));
    }

    public Mono<List<Map<String, Object>>> getPedidosByPyme(Long pymeId) {
        logger.debug("Obteniendo pedidos de PYME {} desde ms-pedidos", pymeId);

        return webClient.get()
                .uri(msPedidosUrl + "/pedidos/pyme/" + pymeId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .timeout(Duration.ofSeconds(5));
    }

    public Mono<Map<String, Object>> validateToken(String token) {
        logger.debug("Validando token con ms-user");

        return webClient.post()
                .uri(msUserUrl + "/auth/validate")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(5));
    }

    public Mono<Map<String, Boolean>> checkServicesHealth() {
        Mono<Boolean> userHealth = checkServiceHealth(msUserUrl + "/auth/health");
        Mono<Boolean> productosHealth = checkServiceHealth(msProductosUrl + "/actuator/health");
        Mono<Boolean> pedidosHealth = checkServiceHealth(msPedidosUrl + "/actuator/health");

        return Mono.zip(userHealth, productosHealth, pedidosHealth)
                .map(tuple -> Map.of(
                        "ms-user", tuple.getT1(),
                        "ms-productos", tuple.getT2(),
                        "ms-pedidos", tuple.getT3()
                ));
    }

    private Mono<Boolean> checkServiceHealth(String healthUrl) {
        return webClient.get()
                .uri(healthUrl)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .timeout(Duration.ofSeconds(3))
                .onErrorReturn(false);
    }

    public Mono<Map<String, Object>> getAdminStats(String authorizationHeader) {
        logger.debug("Obteniendo estadísticas admin desde ms-user");

        WebClient.RequestHeadersSpec<?> request = webClient.get()
                .uri(msUserUrl + "/admin/stats");

        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            request = request.header("Authorization", authorizationHeader);
        }

        return request
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(Duration.ofSeconds(5));
    }

    public Mono<List<Map<String, Object>>> getAllPedidos() {
        logger.debug("Obteniendo todos los pedidos desde ms-pedidos");

        return webClient.get()
                .uri(msPedidosUrl + "/pedidos")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .timeout(Duration.ofSeconds(5));
    }
}