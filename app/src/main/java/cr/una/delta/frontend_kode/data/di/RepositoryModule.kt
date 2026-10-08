package cr.una.delta.frontend_kode.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import cr.una.delta.frontend_kode.domain.repository.*
import cr.una.delta.frontend_kode.data.repository.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository
    @Binds @Singleton abstract fun bindTopicRepository(impl: TopicRepositoryImpl): TopicRepository
    @Binds @Singleton abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository
    @Binds @Singleton abstract fun bindClassSessionRepository(impl: ClassSessionRepositoryImpl): ClassSessionRepository
    @Binds @Singleton abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
    @Binds @Singleton abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository
    @Binds @Singleton abstract fun bindMoodRepository(impl: MoodRepositoryImpl): MoodRepository
    @Binds @Singleton abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
    @Binds @Singleton abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
    @Binds @Singleton abstract fun bindSubjectSummaryRepository(impl: SubjectSummaryRepositoryImpl): SubjectSummaryRepository
    @Binds @Singleton abstract fun bindCourseRubricRepository(impl: CourseRubricRepositoryImpl): CourseRubricRepository
    @Binds @Singleton abstract fun bindCourseRepository(impl: CourseRepositoryImpl): CourseRepository
    @Binds @Singleton abstract fun bindEvaluationRepository(impl: EvaluationRepositoryImpl): EvaluationRepository
    //Authentication
    @Binds @Singleton abstract fun bindAuthRepository(impl: cr.una.delta.frontend_kode.data.repository.AuthRepositoryImpl): cr.una.delta.frontend_kode.domain.repository.AuthRepository
    @Binds @Singleton abstract fun bindStudyPlanRepository(impl: StudyPlanRepositoryImpl): StudyPlanRepository

}
