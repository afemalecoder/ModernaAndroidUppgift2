package com.example.modernaandroiduppgift2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DogScreen()
        }
    }
}
// Retrofit Interface
interface DogAPI {
    @GET("breeds/list/all")
    suspend fun fetchBreeds(): Response<BreedsResponse>
}

// Data classes for responses
data class BreedsResponse(val message: Map<String, List<String>>, val status: String)

// ViewModel to handle data and network calls
class DogViewModel : ViewModel() {
    private val _breeds = MutableLiveData<List<String>>()
    val breeds: LiveData<List<String>> = _breeds

    private val dogService = Retrofit.Builder()
        .baseUrl("https://dog.ceo/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DogAPI::class.java)

    fun fetchBreeds() {
        viewModelScope.launch {
            try {
                val response = dogService.fetchBreeds()
                if (response.isSuccessful) {
                    val decodedData = response.body()
                    decodedData?.let {
                        val breedsList = it.message.keys.toList()
                        _breeds.postValue(breedsList)
                        Log.e("DogViewModel", "Fetched breeds: $breedsList")
                    }
                } else {
                    Log.e("DogViewModel", "Error fetching breeds: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("DogViewModel", "Exception: ${e.message}")
            }
        }
    }
}

@Composable
fun DogScreen(dogViewModel: DogViewModel = viewModel()) {
    val breedState = dogViewModel.breeds.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                dogViewModel.fetchBreeds()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Fetch Dog Breeds")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            val breeds = breedState.value
            breeds?.forEach { breed ->
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(
                            text = breed,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}