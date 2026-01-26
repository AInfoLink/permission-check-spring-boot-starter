package com.module.multitenantbookingservice.core.config

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

@Component
class ItemCategoryConfig(
    @Value("\${item.category.config.path}")
    val configPath: String
) {

    data class SystemCategoryConfigYaml(
        var code: String = "",
        var name: String = "",
        var description: String = ""
    )

    data class CategoryConfigRoot(
        var systemManagedCategories: List<SystemCategoryConfigYaml> = emptyList()
    )

    lateinit var systemManagedCategories: List<SystemCategoryConfigYaml>
    private val logger = LoggerFactory.getLogger(ItemCategoryConfig::class.java)

    @PostConstruct
    fun loadConfig() {
        try {
            val resource = ClassPathResource(configPath)
            val yaml = Yaml()
            val config = yaml.loadAs(resource.inputStream, CategoryConfigRoot::class.java)
            systemManagedCategories = config.systemManagedCategories

            logger.info("Loaded ${systemManagedCategories.size} system category configurations")
            systemManagedCategories.forEach {
                logger.info("   - ${it.code}: ${it.name}")
            }
        } catch (e: Exception) {
            logger.error("Failed to load category configuration file: ${e.message}")
            systemManagedCategories = getDefaultCategories()
        }
    }

    /**
     * 提供預設分類，當 YAML 載入失敗時使用
     */
    private fun getDefaultCategories(): List<SystemCategoryConfigYaml> {
        return listOf(
            SystemCategoryConfigYaml("BOOKING", "預約", "場地預約、活動預約等"),
            SystemCategoryConfigYaml("RENTAL", "租借", "設備租借、器材租借等"),
            SystemCategoryConfigYaml("SERVICE", "服務", "各類服務費用"),
            SystemCategoryConfigYaml("MEMBERSHIP", "會員", "會員費、會員相關費用"),
            SystemCategoryConfigYaml("TOPUP", "儲值", "錢包儲值、點數購買等"),
            SystemCategoryConfigYaml("MERCHANDISE", "商品", "實體商品販售"),
            SystemCategoryConfigYaml("FEE", "費用", "手續費、服務費等"),
            SystemCategoryConfigYaml("DISCOUNT", "折扣", "各類折扣優惠"),
            SystemCategoryConfigYaml("REFUND", "退款", "退費、退款等"),
            SystemCategoryConfigYaml("OTHER", "其他", "其他未分類項目")
        )
    }
}