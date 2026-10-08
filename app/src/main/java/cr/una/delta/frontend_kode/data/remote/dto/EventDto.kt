package cr.una.delta.frontend_kode.data.remote.dto

import java.util.Date

data class EventDto(

    val id: Long,
    val title: String,
    val dateTime: Date,
    val location: String
)