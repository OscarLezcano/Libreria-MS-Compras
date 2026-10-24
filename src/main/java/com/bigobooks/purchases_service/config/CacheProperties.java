package com.bigobooks.purchases_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
// Vincula el prefijo 'cache' del application.properties con los atributos de
// esta clase
@ConfigurationProperties(prefix = "cache")
@Getter
@Setter
public class CacheProperties {
    /**
     * Mapa donde la clave es el nombre del caché (ej. "purchases", "suppliers")
     * y el valor es la duración leída automáticamente por Spring (ej. 15m, 0s).
     * El nombre del atributo 'ttl' completa la ruta: cache.ttl.<nombre>
     */
    private Map<String, Duration> ttl;
}
