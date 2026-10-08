package cr.una.delta.frontend_kode.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import cr.una.delta.frontend_kode.domain.model.*
import cr.una.delta.frontend_kode.domain.repository.MoodRepository
import cr.una.delta.frontend_kode.domain.repository.SubjectRepository
import cr.una.delta.frontend_kode.domain.repository.TopicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MoodState {
    data object Initial : MoodState()
    data object Loading : MoodState()
    data class Success(
        val mood: MoodLevel?, // mood seleccionado
        val subjects: List<Subject>,
        val selectedSubject: Subject?,
        val topics: List<Topic>
    ) : MoodState()
    data class Error(val message: String) : MoodState()
}

@HiltViewModel
class MoodLearningViewModel @Inject constructor(
    private val moodRepo: MoodRepository,
    private val subjectRepo: SubjectRepository,
    private val topicRepo: TopicRepository
) : ViewModel() {

    private val _moods = MutableStateFlow<List<MoodLevel>>(emptyList())
    val moods = _moods.asStateFlow()

    private val _selectedMood = MutableStateFlow<MoodLevel?>(null)
    val selectedMood = _selectedMood.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    private val _topics = MutableStateFlow<List<Topic>>(emptyList())

    private val _state = MutableStateFlow<MoodState>(MoodState.Initial)
    val state = _state.asStateFlow()

    init {
        loadSubjects()
        loadMoods() // ✅ Cargar moods desde el mock api
    }

    private fun loadMoods() {
        viewModelScope.launch {
            moodRepo.getMoods()
                .onSuccess { _moods.value = it }
                .onFailure { e ->
                    Log.e("MoodLearningVM", "Error loading moods: ${e.message}")
                }
        }
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _state.value = MoodState.Loading
            subjectRepo.getSubjects()
                .onSuccess { list ->
                    _subjects.value = list
                    if (_selectedSubject.value == null && list.isNotEmpty()) {
                        selectSubject(list.first())
                    }
                    _state.value = MoodState.Success(
                        mood = _selectedMood.value,
                        subjects = _subjects.value,
                        selectedSubject = _selectedSubject.value,
                        topics = _topics.value
                    )
                }
                .onFailure { e ->
                    _state.value = MoodState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun selectSubject(subject: Subject) {
        _selectedSubject.value = subject
        viewModelScope.launch {
            _state.value = MoodState.Loading
            topicRepo.getTopics(subject.id)
                .onSuccess { list ->
                    _topics.value = list
                    _state.value = MoodState.Success(
                        mood = _selectedMood.value,
                        subjects = _subjects.value,
                        selectedSubject = _selectedSubject.value,
                        topics = _topics.value
                    )
                }
                .onFailure { e ->
                    _state.value = MoodState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun setMood(level: MoodLevel) {
        _selectedMood.value = level
        _state.value = MoodState.Success(
            mood = _selectedMood.value,
            subjects = _subjects.value,
            selectedSubject = _selectedSubject.value,
            topics = _topics.value
        )
    }
}
