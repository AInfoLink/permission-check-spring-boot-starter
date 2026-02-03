import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.module.multitenantbookingservice.core.strategy.*
import java.time.LocalTime

fun main() {
    println("Testing Jackson LocalTime serialization...")

    // Create mapper with Java Time support (same as ConfigMapperService)
    val mapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    // Create a simple BookingTimeSlot
    val timeSlot = BookingTimeSlot(
        slotType = TimeSlotType.REGULAR,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(10, 0),
        priceMultiplier = 1.0,
        basePrice = 100
    )

    // Test serialization
    try {
        val jsonString = mapper.writeValueAsString(timeSlot)
        println("✓ BookingTimeSlot serialized successfully:")
        println(jsonString)

        // Test with BookingTimeSlotConfig with default slots
        val config = BookingTimeSlotConfig().withDefault(TimeSlotInterval.HOURLY)
        println("\n✓ BookingTimeSlotConfig with ${config.timeSlots.size} slots")

        // Test objectToMap conversion
        val configMap = mapper.writeValueAsString(config)
            .let { mapper.readValue(it, Map::class.java) as Map<String, Any> }

        println("✓ Config converted to map successfully:")
        println("- ID: ${configMap["id"]}")
        println("- isConfigured: ${configMap["isConfigured"]}")
        val timeSlots = configMap["timeSlots"] as? List<*>
        println("- timeSlots count: ${timeSlots?.size ?: 0}")

        if (!timeSlots.isNullOrEmpty()) {
            val firstSlot = timeSlots.first() as? Map<*, *>
            println("- First slot example: startTime=${firstSlot?.get("startTime")}, endTime=${firstSlot?.get("endTime")}")
        }

    } catch (e: Exception) {
        println("✗ Serialization failed: ${e.message}")
        e.printStackTrace()
    }
}