package cr.una.delta.frontend_kode.data.repository

import cr.una.delta.frontend_kode.data.mapper.ClassSessionMapper
import cr.una.delta.frontend_kode.data.remote.ClassSessionRemoteDataSource
import cr.una.delta.frontend_kode.domain.model.ClassSession
import cr.una.delta.frontend_kode.domain.repository.ClassSessionRepository
import javax.inject.Inject

class ClassSessionRepositoryImpl @Inject constructor(
    private val ds: ClassSessionRemoteDataSource,
    private val mapper: ClassSessionMapper
) : ClassSessionRepository {

    override suspend fun getAllClasses(): Result<List<ClassSession>> =
        ds.getAllClassSessions().map { mapper.toDomainList(it) }

    override suspend fun getClassById(id: Long): Result<ClassSession> =
        ds.getClassSessionById(id).map { mapper.toDomain(it) }

    override suspend fun getClassesByCourse(courseId: Long): Result<List<ClassSession>> =
        ds.getClassSessionsByCourse(courseId).map { mapper.toDomainList(it) }

    override suspend fun getClassesByProfessor(professorId: Long): Result<List<ClassSession>> =
        ds.getClassesByProfessor(professorId).map { mapper.toDomainList(it) }

    override suspend fun getTodayClasses(): Result<List<ClassSession>> =
        ds.getClassSessionsByDate().map { mapper.toDomainList(it) }

    override suspend fun createClassSession(classSession: ClassSession): Result<ClassSession> =
        ds.createClassSession(mapper.toDto(classSession)).map { mapper.toDomain(it) }

    override suspend fun updateClassSession(id: Long, classSession: ClassSession): Result<ClassSession> =
        ds.updateClassSession(id, mapper.toDto(classSession)).map { mapper.toDomain(it) }

    override suspend fun deleteClassSession(id: Long): Result<Unit> =
        ds.deleteClassSession(id)
    override suspend fun updateModality(id: Long, modality: String): Result<ClassSession> =
        ds.updateClassModality(id, modality).map { mapper.toDomain(it) }

}
