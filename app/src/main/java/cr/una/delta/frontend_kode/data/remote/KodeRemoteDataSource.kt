package cr.una.delta.frontend_kode.data.remote

import cr.una.delta.frontend_kode.data.remote.api.KodeService
import cr.una.delta.frontend_kode.data.remote.dto.*
import retrofit2.Response
import javax.inject.Inject

class KodeRemoteDataSource @Inject constructor(
    private val kodeService: KodeService
) {

    // ---------------- TASKS ----------------
    suspend fun getTasksOfDay(): Result<List<TaskDto>> = safeApiCall { kodeService.getTasksOfDay() }
    suspend fun getTaskById(id: Long): Result<TaskDto> = safeApiCall { kodeService.getTaskById(id) }
    suspend fun createTask(taskDto: TaskDto): Result<TaskDto> = safeApiCall { kodeService.createTask(taskDto) }
    suspend fun updateTask(id: Long, taskDto: TaskDto): Result<TaskDto> = safeApiCall { kodeService.updateTask(id, taskDto) }
    suspend fun deleteTask(id: Long): Result<Unit> = safeApiCall { kodeService.deleteTask(id) }

    // ---------------- NOTES ----------------
    suspend fun getAllNotes(): Result<List<NoteDto>> = safeApiCall { kodeService.getAllNotes() }
    suspend fun createNote(noteDto: NoteDto): Result<NoteDto> = safeApiCall { kodeService.createNote(noteDto) }

    // ---------------- SUBJECTS ----------------
    suspend fun getAllSubjects(): Result<List<SubjectDto>> = safeApiCall { kodeService.getAllSubjects() }
    suspend fun getSubjectById(id: Long): Result<SubjectDto> = safeApiCall { kodeService.getSubjectById(id) }

    // ---------------- TOPICS ----------------
    suspend fun getTopics(subjectId: Long): Result<List<TopicDto>> = safeApiCall { kodeService.getTopics(subjectId) }

    // ---------------- EVENTS ----------------
    suspend fun getNextEvent(): Result<EventDto?> = safeApiCall { kodeService.getNextEvent() }
    suspend fun getNextEventForTeachers(): Result<EventDto?> = safeApiCall { kodeService.getNextEventForTeachers() }
    suspend fun getAllEvents(): Result<List<EventDto>> = safeApiCall { kodeService.getAllEvents() }
    suspend fun createEvent(eventDto: EventDto): Result<EventDto> = safeApiCall { kodeService.createEvent(eventDto) }

    // ---------------- CLASSES ----------------
    suspend fun getTodayClasses(): Result<List<ClassSessionDto>> = safeApiCall { kodeService.getTodayClasses() }
    suspend fun getTodayClassesForTeachers(): Result<List<ClassSessionDto>> = safeApiCall { kodeService.getTodayClassesForTeachers() }

    // ---------------- REMINDERS ----------------
    suspend fun getReminders(): Result<List<ReminderDto>> = safeApiCall { kodeService.getReminders() }
    suspend fun getRemindersForTeachers(): Result<List<ReminderDto>> = safeApiCall { kodeService.getRemindersForTeachers() }

    // ---------------- ALERTS ----------------
    suspend fun getAlertOfDay(): Result<AlertDto?> = safeApiCall { kodeService.getAlertOfDay() }
    suspend fun getAlertForTeachers(): Result<AlertDto?> = safeApiCall { kodeService.getAlertForTeachers() }

    // ---------------- MOOD ----------------
    suspend fun saveMood(level: MoodDto): Result<Unit> = safeApiCall { kodeService.saveMood(level) }

    // ---------------- SUBJECT SUMMARIES ----------------
    suspend fun getSubjectSummaries(): Result<List<SubjectSummaryDto>> = safeApiCall { kodeService.getSubjectSummaries() }

    // ---------------- HELPERS ----------------
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> = try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Response body was null"))
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception("API error ${response.code()}: $errorBody"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
