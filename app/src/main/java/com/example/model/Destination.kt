package com.example.model

import java.util.UUID

data class Destination(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val country: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val imageUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Convert to Firestore map
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "country" to country,
            "price" to price,
            "description" to description,
            "imageUri" to imageUri,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Destination {
            return Destination(
                id = id,
                name = map["name"] as? String ?: "",
                country = map["country"] as? String ?: "",
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                description = map["description"] as? String ?: "",
                imageUri = map["imageUri"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
