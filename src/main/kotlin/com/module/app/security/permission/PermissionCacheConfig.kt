package com.module.app.security.permission

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

// DISABLED: Using permission-check-spring-boot-starter cache configuration instead
// @Configuration
// @EnableCaching
// @Profile("memory-cache")
class InMemoryPermissionCacheConfig {

    @Bean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager("userPermissions")
    }
}

//@Configuration
//@EnableCaching
//@Profile("!memory-cache")
//class RedisPermissionCacheConfig {
//
//    @Bean
//    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
//        val objectMapper = ObjectMapper()
//            .registerKotlinModule()
//            .activateDefaultTyping(
//                BasicPolymorphicTypeValidator.builder()
//                    .allowIfSubType(Any::class.java)
//                    .build(),
//                ObjectMapper.DefaultTyping.NON_FINAL,
//                JsonTypeInfo.As.PROPERTY
//            )
//
//        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
//            .entryTtl(Duration.ofMinutes(30)) // 权限缓存 30 分钟过期
//            .disableCachingNullValues() // 不缓存 null 值
//            .serializeKeysWith(
//                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
//            )
//            .serializeValuesWith(
//                RedisSerializationContext.SerializationPair.fromSerializer(
//                    GenericJackson2JsonRedisSerializer(objectMapper)
//                )
//            )
//
//        return RedisCacheManager.builder(redisConnectionFactory)
//            .cacheDefaults(redisCacheConfiguration)
//            .withCacheConfiguration("userPermissions", redisCacheConfiguration)
//            .build()
//    }
//}