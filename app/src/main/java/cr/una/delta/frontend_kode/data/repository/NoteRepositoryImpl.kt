package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.NoteMapper
import cr.una.delta.frontend_kode.data.remote.NoteRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.Note
import cr.una.delta.frontend_kode.domain.repository.NoteRepository
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val ds: NoteRemoteDataSource,
    private val mapper: NoteMapper
) : NoteRepository {

    override suspend fun getNotes(): Result<List<Note>> =
        ds.getAllNotes().map { mapper.toDomainList(it) }

    override suspend fun createNote(note: Note): Result<Note> =
        ds.createNote(mapper.mapToDto(note))
            .map { mapper.mapToDomain(it) }
}
