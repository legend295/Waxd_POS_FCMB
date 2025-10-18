package com.waxd.pos.fcmb.ui.main.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waxd.pos.fcmb.data.UploadRepository
import com.waxd.pos.fcmb.model.StatusCounts
import com.waxd.pos.fcmb.room.dao.UploadDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: UploadRepository) : ViewModel() {

    fun dispatchNow() {
        repo.dispatchNow()
    }

    val filesCount: StateFlow<StatusCounts> =
        repo.getPendingUploadsCount().stateIn(viewModelScope, SharingStarted.Lazily, StatusCounts())

    fun enqueue(file: File, mime: String) {
        viewModelScope.launch {
            repo.enqueueFile("", file, mime)
        }
    }
}