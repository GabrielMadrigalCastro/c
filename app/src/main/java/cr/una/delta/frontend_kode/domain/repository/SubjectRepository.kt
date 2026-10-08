package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.Subject

interface SubjectRepository {
    suspend fun getSubjects(): Result<List<Subject>>
}
