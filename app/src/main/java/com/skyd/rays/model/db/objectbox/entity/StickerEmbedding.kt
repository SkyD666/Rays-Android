package com.skyd.rays.model.db.objectbox.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class StickerEmbedding(
    @Id var id: Long = 0,
    @Index var uuid: String,
    @HnswIndex(
        dimensions = 1024,
        neighborsPerNode = 128,
        indexingSearchCount = 400,
    )
    var embedding: FloatArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StickerEmbedding

        if (uuid != other.uuid) return false
        if (embedding != null) {
            if (other.embedding == null) return false
            if (!embedding.contentEquals(other.embedding)) return false
        } else if (other.embedding != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        return result
    }
}