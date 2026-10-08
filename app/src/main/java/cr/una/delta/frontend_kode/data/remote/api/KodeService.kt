package cr.una.delta.frontend_kode.data.remote.api

import cr.una.delta.frontend_kode.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface KodeService {

    // SUBJECTS
    @GET("subjects")
    suspend fun getSubjects(): Response<List<SubjectDto>>

    @GET("subjects/{id}/topics")
    suspend fun getTopics(@Path("id") subjectId: Long): Response<List<TopicDto>>

    @GET("subjects/summary")
    suspend fun getSubjectSummaries(): Response<List<SubjectSummaryDto>>

    @GET("subjects/{id}")
    suspend fun getSubjectById(@Path("id") id: Long): Response<SubjectDto>

    @GET("subjects")
    suspend fun getAllSubjects(): Response<List<SubjectDto>>

    // NOTES
    @GET("notes")
    suspend fun getNotes(): Response<List<NoteDto>>

    @GET("notes")
    suspend fun getAllNotes(): Response<List<NoteDto>>

    @POST("notes")
    suspend fun createNote(@Body noteDto: NoteDto): Response<NoteDto>

    // TASKS
    @GET("tasks")
    suspend fun getAllTasks(): Response<List<TaskDto>>

    @GET("tasks/today")
    suspend fun getTasksOfDay(): Response<List<TaskDto>>

    @GET("tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Long): Response<TaskDto>

    @POST("tasks")
    suspend fun createTask(@Body taskDto: TaskDto): Response<TaskDto>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: Long, @Body taskDto: TaskDto): Response<TaskDto>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Long): Response<Unit>

    // CLASSES
    @GET("classes/today")
    suspend fun getTodayClasses(): Response<List<ClassSessionDto>>

    @GET("classes/teachers")
    suspend fun getTodayClassesForTeachers(): Response<List<ClassSessionDto>>

    // REMINDERS
    @GET("reminders")
    suspend fun getReminders(): Response<List<ReminderDto>>

    @GET("reminders/teacher")
    suspend fun getRemindersForTeachers(): Response<List<ReminderDto>>

    // ALERTS
    @GET("alerts")
    suspend fun getAlertOfDay(): Response<AlertDto>

    @GET("alerts/teacher")
    suspend fun getAlertForTeachers(): Response<AlertDto>

    // EVENTS
    @GET("events/next")
    suspend fun getNextEvent(): Response<EventDto>

    @GET("events/next/teacher")
    suspend fun getNextEventForTeachers(): Response<EventDto>

    @GET("events")
    suspend fun getAllEvents(): Response<List<EventDto>>

    @POST("events")
    suspend fun createEvent(@Body eventDto: EventDto): Response<EventDto>

    // MOOD
    @POST("mood")
    suspend fun saveMood(@Body mood: MoodDto): Response<Unit>
}
