package com.khoben.autotitle.model

/**
 * AutoGenerated caption/title for video
 *
 * @property text Caption text
 * @property startTime Beginning visible time
 * @property endTime Ending visible time
 */
data class MLCaption(
    var text: String,
    var startTime: Long,
    var endTime: Long
)