package com.module.app.commons.utils

import com.module.app.commons.annotation.TenantConfig
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import kotlin.reflect.KClass

/**
 * Utility object for extracting configuration keys from TenantConfig annotations.
 */
object TenantConfigUtils {
    private val PACKAGE_NAME = "com.module.app"
    /**
     * Extracts the config key from the @TenantConfig annotation on the given class.
     *
     * @param configClass The class annotated with @TenantConfig
     * @return The config key specified in the annotation
     * @throws IllegalArgumentException if the class is not annotated with @TenantConfig
     */
    fun getConfigKey(configClass: KClass<*>): String {
        val annotation = configClass.java.getAnnotation(TenantConfig::class.java)
            ?: throw IllegalArgumentException("Class ${configClass.simpleName} is not annotated with @TenantConfig")
        return annotation.key
    }

    /**
     * Finds all classes annotated with @TenantConfig in the specified package using Spring's classpath scanning.
     *
     * @param packageName The package to scan (e.g., "com.module.app")
     * @return Set of classes annotated with @TenantConfig
     */
    fun findAllTenantConfigClasses(): Set<Class<*>> {
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AnnotationTypeFilter(TenantConfig::class.java))

        return scanner.findCandidateComponents(PACKAGE_NAME)
            .mapNotNull { beanDefinition ->
                try {
                    Class.forName(beanDefinition.beanClassName)
                } catch (e: ClassNotFoundException) {
                    null
                }
            }.toSet()
    }

    /**
     * Gets all tenant config classes with their respective keys.
     *
     * @param packageName The package to scan
     * @return Map of config key to class
     */
    fun getAllTenantConfigs(): Map<String, Class<*>> {
        return findAllTenantConfigClasses().associateBy { clazz ->
            getConfigKey(clazz.kotlin)
        }
    }
}