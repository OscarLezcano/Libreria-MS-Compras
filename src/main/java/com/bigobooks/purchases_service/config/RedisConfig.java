package com.bigobooks.purchases_service.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching // Habilita el soporte de anotaciones @Cacheable y @CacheEvict en Spring
public class RedisConfig {

        @Bean
        public RedisCacheConfiguration cacheConfiguration() {
                // 1. Configuramos Jackson para soportar fechas Java 8 (LocalDateTime) y
                // polimorfismo
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // Guarda el tipo de clase en el JSON para poder reconstruir los DTOs
                // correctamente al leer de Redis
                objectMapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

                // 2. Construcción de la configuración por defecto
                return RedisCacheConfiguration.defaultCacheConfig()
                                // Evita almacenar llaves con valor nulo en Redis si la BD no encuentra el
                                // registro
                                .disableCachingNullValues()
                                // Serializa las claves como texto legible (String)
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                // Serializa los valores como JSON estructurado
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        }

        /**
         * Personaliza el RedisCacheManager que Spring Boot crea automáticamente.
         * Recorre el mapa de TTLs definido en application.properties y aplica
         * el tiempo de vida correspondiente a cada caché sin codificar nombres a mano
         * en Java.
         */
        @Bean
        public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
                        CacheProperties cp,
                        RedisCacheConfiguration defaultConfig) {

                return builder -> {
                        // Verificamos que existan propiedades configuradas para evitar
                        // NullPointerException
                        if (cp.getTtl() != null) {
                                // Itera dinámicamente: cacheName = "purchases", duration = 15m
                                // (Duration.ofMinutes(15))
                                cp.getTtl().forEach((cacheName, duration) -> builder.withCacheConfiguration(cacheName,
                                                defaultConfig.entryTtl(duration)));
                        }
                };
        }
}