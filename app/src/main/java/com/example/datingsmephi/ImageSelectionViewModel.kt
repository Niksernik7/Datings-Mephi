package com.example.datingsmephi

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ImageSelectionViewModel : ViewModel() {
    private val _imageUris = MutableStateFlow<List<Uri>>(List(6) { Uri.EMPTY })
    val imageUris: StateFlow<List<Uri>> = _imageUris

    fun setImageUris(uris: List<Uri>) {
        _imageUris.value = uris
    }
    // Обновление URI изображения в списке
    fun updateImageUri(index: Int, uri: Uri) {
        val updatedUris = _imageUris.value.toMutableList().apply {
            this[index] = uri
        }
        _imageUris.value = updatedUris
    }

    fun setImageUrisWithPlaceholders(uris: List<Uri>) {
        val filledUris = uris.toMutableList()
        while (filledUris.size < 6) {
            filledUris.add(Uri.EMPTY)
        }
        _imageUris.value = filledUris
    }
}