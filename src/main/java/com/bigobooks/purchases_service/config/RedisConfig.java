package com.bigobooks.purchases_service.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

        @Value("${app.cache.ttl.purchases-minutes:15}")
        private long purchasesTtl;

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // Soporte para LocalDateTime y tipado de objetos en Redis
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

                // 1. Configuración Base: Persistente (sin TTL predeterminado)
                RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                                .disableCachingNullValues();

                // 2. Políticas de Retención segmentadas por Dominio
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // DOMINIO PROVEEDORES: Persistente sin expiración automática (solo se invalida
                // con update/delete)
                cacheConfigurations.put("suppliers", baseConfig.entryTtl(Duration.ZERO));

                // DOMINIO COMPRAS: Temporal con TTL limitado (expira automáticamente tras los
                // minutos configurados)
                cacheConfigurations.put("purchases", baseConfig.entryTtl(Duration.ofMinutes(purchasesTtl)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(baseConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }
}