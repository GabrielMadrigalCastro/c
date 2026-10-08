package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.NoteService
import cr.una.delta.frontend_kode.data.remote.dto.NoteDto
import jakarta.inject.Inject

class NoteRemoteDataSource  @Inject constructor(
    private val service: NoteService
)
{
    suspend fun getAllNotes(): Result<List<NoteDto>> = safeApiCall { service.getAllNotes() }
    suspend fun createNote(Dto: NoteDto): Result<NoteDto> = safeApiCall { service.createNote(Dto) }

}