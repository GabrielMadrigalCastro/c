package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.NoteDto
import retrofit2.Response
import retrofit2.http.*
interface NoteService {

    // Obtener todas las notas
    @GET("notes")
    suspend fun getAllNotes(): Response<List<NoteDto>>

    // Obtener una nota por ID
    @GET("notes/{id}")
    suspend fun getNoteById(@Path("id") id: Long): Response<NoteDto>

    // Crear una nota
    @POST("notes")
    suspend fun createNote(@Body noteDto: NoteDto): Response<NoteDto>
}