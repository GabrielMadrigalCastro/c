package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Note

interface  NoteRepository {
    suspend fun getNotes(): Result<List<Note>>
    suspend fun createNote(note: Note): Result<Note>
}