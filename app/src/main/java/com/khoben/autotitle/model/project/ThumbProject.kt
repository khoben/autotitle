package com.khoben.autotitle.model.project

import kotlinx.serialization.Serializable

/**
 * Item of recent projects on main screen
 * @property id Unique id
 * @property title Visible name of project
 * @property sourceVideoThumbPath Path to thumb
 * @property dateCreated Unix timestamp of first time opened
 * @property dateUpdated Unix timestamp of last update
 */
@Serializable
data class ThumbProject(
        val id: String,
        var title: String? = null,
        val sourceVideoThumbPath: String = "thumb",
        val dateCreated: Long = -1,
        var dateUpdated: Long = -1,
)