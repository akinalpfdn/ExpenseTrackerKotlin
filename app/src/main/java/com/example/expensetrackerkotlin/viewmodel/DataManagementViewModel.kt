package com.example.expensetrackerkotlin.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerkotlin.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class DataManagementState {
    object Idle : DataManagementState()
    object Loading : DataManagementState()
    data class ExportSuccess(val shareIntent: Intent) : DataManagementState()
    data class ExportReadyForStorage(val jsonContent: String, val saveIntent: Intent) : DataManagementState()
    data class StorageSaveSuccess(val message: String) : DataManagementState()
    data class ImportSuccess(val summary: ImportSummary) : DataManagementState()
    data class Error(val message: String) : DataManagementState()
}

class DataManagementViewModel(
    private val database: ExpenseDatabase,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<DataManagementState>(DataManagementState.Idle)
    val state: StateFlow<DataManagementState> = _state.asStateFlow()

    private val exportManager = ExportManager(database)
    private val importManager = ImportManager(database)
    private val fileManager = FileManager(context)

    private var lastExportFile: File? = null
    private var pendingExportJson: String? = null

    fun exportAndShare() {
        viewModelScope.launch {
            _state.value = DataManagementState.Loading

            try {
                // Export data to JSON
                val exportResult = exportManager.exportAllData(context)
                if (exportResult.isFailure) {
                    _state.value = DataManagementState.Error(
                        exportResult.exceptionOrNull()?.message ?: "Export failed"
                    )
                    return@launch
                }

                val jsonContent = exportResult.getOrThrow()

                // Create file
                val fileResult = fileManager.createExportFile(jsonContent)
                if (fileResult.isFailure) {
                    _state.value = DataManagementState.Error(
                        fileResult.exceptionOrNull()?.message ?: "Failed to create file"
                    )
                    return@launch
                }

                val file = fileResult.getOrThrow()
                lastExportFile = file

                // Create share intent
                val shareIntent = fileManager.shareFile(file)
                _state.value = DataManagementState.ExportSuccess(shareIntent)

            } catch (e: Exception) {
                _state.value = DataManagementState.Error(
                    "Export failed: ${e.message}"
                )
            }
        }
    }

    fun exportToStorage() {
        viewModelScope.launch {
            _state.value = DataManagementState.Loading

            try {
                // Export data to JSON
                val exportResult = exportManager.exportAllData(context)
                if (exportResult.isFailure) {
                    _state.value = DataManagementState.Error(
                        exportResult.exceptionOrNull()?.message ?: "Export failed"
                    )
                    return@launch
                }

                val jsonContent = exportResult.getOrThrow()
                pendingExportJson = jsonContent

                // Create save to storage intent
                val saveIntent = fileManager.createSaveToStorageIntent()
                _state.value = DataManagementState.ExportReadyForStorage(jsonContent, saveIntent)

            } catch (e: Exception) {
                _state.value = DataManagementState.Error(
                    "Export failed: ${e.message}"
                )
            }
        }
    }

    fun saveToStorage(uri: Uri) {
        viewModelScope.launch {
            _state.value = DataManagementState.Loading

            try {
                val jsonContent = pendingExportJson
                if (jsonContent == null) {
                    _state.value = DataManagementState.Error("No export data available")
                    return@launch
                }

                val saveResult = fileManager.saveToStorage(uri, jsonContent)
                if (saveResult.isFailure) {
                    _state.value = DataManagementState.Error(
                        saveResult.exceptionOrNull()?.message ?: "Failed to save file"
                    )
                    return@launch
                }

                pendingExportJson = null
                _state.value = DataManagementState.StorageSaveSuccess("Backup saved successfully!")

            } catch (e: Exception) {
                _state.value = DataManagementState.Error(
                    "Save failed: ${e.message}"
                )
            }
        }
    }

    fun importData(uri: Uri, strategy: ImportStrategy) {
        viewModelScope.launch {
            _state.value = DataManagementState.Loading

            try {
                // Read file
                val fileResult = fileManager.readImportFile(uri)
                if (fileResult.isFailure) {
                    _state.value = DataManagementState.Error(
                        fileResult.exceptionOrNull()?.message ?: "Failed to read file"
                    )
                    return@launch
                }

                val jsonContent = fileResult.getOrThrow()

                // Validate file
                val validateResult = importManager.validateImportFile(jsonContent)
                if (validateResult.isFailure) {
                    _state.value = DataManagementState.Error(
                        validateResult.exceptionOrNull()?.message ?: "Invalid import file"
                    )
                    return@launch
                }

                // Import data
                val importResult = importManager.importData(jsonContent, strategy)
                if (importResult.isFailure) {
                    _state.value = DataManagementState.Error(
                        importResult.exceptionOrNull()?.message ?: "Import failed"
                    )
                    return@launch
                }

                val summary = importResult.getOrThrow()
                _state.value = DataManagementState.ImportSuccess(summary)

            } catch (e: Exception) {
                _state.value = DataManagementState.Error(
                    "Import failed: ${e.message}"
                )
            }
        }
    }

    fun cleanupExportFile() {
        lastExportFile?.let { file ->
            fileManager.cleanupExportFile(file)
            lastExportFile = null
        }
    }

    fun resetState() {
        _state.value = DataManagementState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        cleanupExportFile()
    }
}
