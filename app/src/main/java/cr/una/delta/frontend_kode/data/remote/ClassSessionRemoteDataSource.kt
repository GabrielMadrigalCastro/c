package cr.una.delta.frontend_kode.data.remote

import android.annotation.SuppressLint
import cr.una.delta.frontend_kode.data.remote.api.ClassSessionService
import cr.una.delta.frontend_kode.data.remote.dto.ClassSessionDto
import javax.inject.Inject

class ClassSessionRemoteDataSource @Inject constructor(
    private val service: ClassSessionService
) {
    suspend fun getAllClassSessions(): Result<List<ClassSessionDto>> =
        safeApiCall { service.getAllClassSessions() }

    suspend fun getClassSessionById(id: Long): Result<ClassSessionDto> =
        safeApiCall { service.getClassSessionById(id) }

    suspend fun getClassSessionsByCourse(courseId: Long): Result<List<ClassSessionDto>> =
        safeApiCall { service.getClassSessionsByCourse(courseId) }

    @SuppressLint("SimpleDateFormat")
    suspend fun getClassSessionsByDate(): Result<List<ClassSessionDto>> {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        return safeApiCall { service.getClassSessionsByDate(today) }
    }

    suspend fun getClassesByProfessor(professorId: Long): Result<List<ClassSessionDto>> =
        safeApiCall { service.getClassesByProfessor(professorId) }

    suspend fun createClassSession(input: ClassSessionDto): Result<ClassSessionDto> =
        safeApiCall { service.createClassSession(input) }

    suspend fun updateClassSession(id: Long, input: ClassSessionDto): Result<ClassSessionDto> =
        safeApiCall { service.updateClassSession(id, input) }

    suspend fun deleteClassSession(id: Long): Result<Unit> =
        safeApiCall { service.deleteClassSession(id) }
    suspend fun updateClassModality(id: Long, modality: String): Result<ClassSessionDto> =
        safeApiCall { service.updateClassModality(id, mapOf("modality" to modality)) }

}
