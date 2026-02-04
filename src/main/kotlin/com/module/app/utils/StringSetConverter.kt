package com.module.app.utils

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringSetConverter : AttributeConverter<MutableSet<String>, String> {
    override fun convertToDatabaseColumn(attribute: MutableSet<String>): String {
        return attribute.joinToString(",") { it.trim() }
    }

    override fun convertToEntityAttribute(dbData: String): MutableSet<String> {
        return dbData.split(",").map { it.trim() }.toMutableSet()
    }
}