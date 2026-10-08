package cr.una.delta.frontend_kode.data.di

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import cr.una.delta.frontend_kode.data.local.SessionManager
import cr.una.delta.frontend_kode.data.remote.api.*
import cr.una.delta.frontend_kode.data.remote.dto.*
import cr.una.delta.frontend_kode.data.remote.interceptor.ResponseInterceptor
import cr.una.delta.frontend_kode.data.remote.serializer.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    init {
        Log.d("NetworkModule", "🔥 NetworkModule cargado correctamente")
    }

    private const val BASE_URL = "https://b-mpbp.onrender.com/v1/"

    // ========================================
    //  🔹 GSON Configuration
    // ========================================
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(UserDto::class.java, UserTypeAdapter())
        .registerTypeAdapter(AlertDto::class.java, AlertDeserializer())
        .registerTypeAdapter(ClassSessionDto::class.java, ClassSessionDeserializer())
        .registerTypeAdapter(EventDto::class.java, EventDeserializer())
        .registerTypeAdapter(ReminderDto::class.java, ReminderDeserializer())
        .registerTypeAdapter(SubjectDto::class.java, SubjectDeserializer())
        .registerTypeAdapter(TopicDto::class.java, TopicDeserializer())
        .registerTypeAdapter(MoodDto::class.java, MoodDeserializer())
        .registerTypeAdapter(TaskDto::class.java, TaskDeserializer())
        .registerTypeAdapter(NoteDto::class.java, NoteDeserializer())
        .registerTypeAdapter(CourseDto::class.java, CourseDeserializer())
        .registerTypeAdapter(CourseRubricDto::class.java, CourseRubricDeserializer())
        .registerTypeAdapter(AssignmentDto::class.java, AssignmentDeserializer())
        .registerTypeAdapter(DayPlanResponse::class.java, DayPlanDeserializer())
        .setLenient()
        .setPrettyPrinting()
        .serializeNulls()
        .setDateFormat("yyyy-MM-dd")
        .create()

    // ========================================
    //  🔹 OkHttpClient
    // ========================================
    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor { message -> Log.d("OkHttp", message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        responseInterceptor: ResponseInterceptor,
        authInterceptor: cr.una.delta.frontend_kode.data.remote.interceptor.AuthInterceptor
    ): OkHttpClient {
        Log.d("NetworkModule", "Creating OkHttpClient with BASE_URL: $BASE_URL")
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .addInterceptor(responseInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                Log.d("OkHttp", "═══════════════════════════════════════")
                Log.d("OkHttp", "REQUEST: ${request.method} ${request.url}")
                Log.d("OkHttp", "Headers: ${request.headers}")
                val response = chain.proceed(request)
                Log.d("OkHttp", "RESPONSE: ${response.code} ${response.message}")
                Log.d("OkHttp", "═══════════════════════════════════════")
                response
            }
            // Render Free duerme el backend por inactividad y despertarlo tarda
            // ~40-60s (cold start). Con timeouts cortos, la 1ra llamada se corta
            // antes de despertar. Damos margen para que el primer request aguante.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(70, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(80, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    // ========================================
    //  🔹 Retrofit
    // ========================================
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()

    // ========================================
    //  🔹 Services
    // ========================================
    @Provides @Singleton fun subjectService(r: Retrofit): SubjectService = r.create(SubjectService::class.java)
    @Provides @Singleton fun topicService(r: Retrofit): TopicService = r.create(TopicService::class.java)
    @Provides @Singleton fun eventService(r: Retrofit): EventService = r.create(EventService::class.java)
    @Provides @Singleton fun classSessionService(r: Retrofit): ClassSessionService = r.create(ClassSessionService::class.java)
    @Provides @Singleton fun reminderService(r: Retrofit): ReminderService = r.create(ReminderService::class.java)
    @Provides @Singleton fun alertService(r: Retrofit): AlertService = r.create(AlertService::class.java)
    @Provides @Singleton fun moodService(r: Retrofit): MoodService = r.create(MoodService::class.java)
    @Provides @Singleton fun taskService(r: Retrofit): TaskService = r.create(TaskService::class.java)
    @Provides @Singleton fun noteService(r: Retrofit): NoteService = r.create(NoteService::class.java)
    @Provides @Singleton fun subjectSummaryService(r: Retrofit): SubjectSummarService = r.create(SubjectSummarService::class.java)
    @Provides @Singleton fun provideCourseService(r: Retrofit): CourseService = r.create(CourseService::class.java)
    @Provides @Singleton fun provideEvaluationService(r: Retrofit): EvaluationService = r.create(EvaluationService::class.java)
    @Provides @Singleton fun provideCourseRubricService(r: Retrofit): CourseRubricService = r.create(CourseRubricService::class.java)
    @Provides @Singleton fun provideRubricGradeService(r: Retrofit): RubricGradeService = r.create(RubricGradeService::class.java)
    @Provides @Singleton fun provideAssignmentService(r: Retrofit): AssignmentService = r.create(AssignmentService::class.java)
    @Provides @Singleton fun provideApunteService(r: Retrofit): ApunteService = r.create(ApunteService::class.java)
    @Provides @Singleton fun provideImageProcessingService(r: Retrofit): cr.una.delta.frontend_kode.data.remote.api.ImageProcessingService = r.create(cr.una.delta.frontend_kode.data.remote.api.ImageProcessingService::class.java)
    @Provides @Singleton fun provideIaExtrasService(r: Retrofit): IaExtrasService = r.create(IaExtrasService::class.java)

    // ========================================
    //  🔹 Authentication + IA
    // ========================================
    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    @Provides
    @Singleton
    fun provideIAApiService(retrofit: Retrofit): IAApiService =
        retrofit.create(IAApiService::class.java)

    // ========================================
    //  🔹 Session Manager
    // ========================================
    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager =
        SessionManager(context)

    // ========================================
    //  🔹 Enrollment Module
    // ========================================
    @Provides
    @Singleton
    fun provideEnrollmentService(retrofit: Retrofit): EnrollmentService =
        retrofit.create(EnrollmentService::class.java)

    @Provides
    @Singleton
    fun provideEnrollmentRepository(
        ds: cr.una.delta.frontend_kode.data.remote.EnrollmentRemoteDataSource,
        mapper: cr.una.delta.frontend_kode.data.mapper.EnrollmentMapper
    ): cr.una.delta.frontend_kode.domain.repository.EnrollmentRepository =
        cr.una.delta.frontend_kode.data.repository.EnrollmentRepositoryImpl(ds, mapper)

}
