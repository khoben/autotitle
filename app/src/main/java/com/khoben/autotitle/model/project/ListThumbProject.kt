package com.khoben.autotitle.model.project

import com.khoben.autotitle.App
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import timber.log.Timber
import java.io.File

@ExperimentalSerializationApi
@Serializable
data class ListThumbProject(
    var list: List<ThumbProject>? = null
) {
    private val FILE_CONTENT = App.PROJECTS_FILE_SERIALIZED

    /**
     * Reads projects info from [FILE_CONTENT] file
     *
     * @return [FILE_CONTENT] exists
     */
    fun load(): Boolean {
        Timber.d("Loading recent projects")
        val file = File(FILE_CONTENT)
        if (!file.exists()) {
            Timber.e("File $FILE_CONTENT not found")
            return false
        }
        val bytes = file.readBytes()
        decode(bytes)
        return list != null && list!!.isNotEmpty()
    }

    /**
     * Stores projects list to filesystem
     */
    fun save() {
        Timber.d("Storing recent projects")
        list?.forEach { it.createProjectFolderIfNotExists() }
        val bytes = encode()
        File(FILE_CONTENT).writeBytes(bytes)
    }

    private fun encode() = ProtoBuf.encodeToByteArray(this)

    private fun decode(data: ByteArray) {
        val decoded = ProtoBuf.decodeFromByteArray<ListThumbProject>(data)
        list = decoded.list
    }
}