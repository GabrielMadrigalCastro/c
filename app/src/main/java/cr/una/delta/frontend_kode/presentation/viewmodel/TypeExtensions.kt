package cr.una.delta.frontend_kode.presentation.viewmodel

import cr.una.delta.frontend_kode.domain.model.BlockType
import cr.una.delta.frontend_kode.domain.model.EventType

fun BlockType.toEventType(): EventType = when (this) {
    BlockType.CLASS -> EventType.CLASS
    BlockType.STUDY -> EventType.TASK
    BlockType.TRAVEL -> EventType.TRAVEL
    BlockType.MEAL -> EventType.LUNCH
    else -> EventType.OTHER
}
