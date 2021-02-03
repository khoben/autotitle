package com.khoben.autotitle.database.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "created_at") var createdAt: Long = -1,
    @ColumnInfo(name = "updated_at") var updatedAt: Long = -1,
    @ColumnInfo(name = "thumb_uri") var thumbUri: String = "",
    @ColumnInfo(name = "source_file_uri") val sourceFileUri: String,
    @ColumnInfo(name = "video_file_size_byte") val videoFileSizeBytes: Long,
    @ColumnInfo(name = "video_duration_ms") val videoDuration: Long
) : Parcelable