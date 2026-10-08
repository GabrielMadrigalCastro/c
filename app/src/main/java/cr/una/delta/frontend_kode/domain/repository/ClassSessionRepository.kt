package cr.una.delta.frontend_kode.domain.repository

import cr.una.delta.frontend_kode.domain.model.ClassSession

interface ClassSessionRepository {

        suspend fun getAllClasses(): Result<List<ClassSession>>
        suspend fun getClassById(id: Long): Result<ClassSession>
        suspend fun getClassesByCourse(courseId: Long): Result<List<ClassSession>>
        suspend fun getClassesByProfessor(professorId: Long): Result<List<ClassSession>>
        suspend fun getTodayClasses(): Result<List<ClassSession>>
        suspend fun createClassSession(classSession: ClassSession): Result<ClassSession>
        suspend fun updateClassSession(id: Long, classSession: ClassSession): Result<ClassSession>
        suspend fun deleteClassSession(id: Long): Result<Unit>
    suspend fun updateModality(id: Long, modality: String): Result<ClassSession>

}


