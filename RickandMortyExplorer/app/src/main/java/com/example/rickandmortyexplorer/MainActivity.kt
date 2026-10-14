
package com.example.rickandmortyexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rickandmortyexplorer.data.Character
import com.example.rickandmortyexplorer.data.Episode
import com.example.rickandmortyexplorer.data.Location
import com.example.rickandmortyexplorer.data.RetrofitInstance
import com.example.rickandmortyexplorer.ui.theme.RickAndMortyExplorerTheme
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import kotlin.inc

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RickAndMortyExplorerTheme {
                Column() {
                    Header(modifier = Modifier.fillMaxWidth())
                    RickAndMortyScreen(
                        modifier = Modifier.fillMaxSize())
                }

            }
        }
    }
}

enum class Category {
    CHARACTER,
    EPISODE,
    LOCATION
}

@Composable
fun RickAndMortyScreen(modifier: Modifier) {
    var category by remember { mutableStateOf(Category.CHARACTER) }
    var searchText by remember { mutableStateOf("") }
    var appliedSearch by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<Any?>(null) }
    var totalResults by remember { mutableIntStateOf(0) }
    var imagesRetrieved by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rollCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(category, appliedSearch, rollCounter) {
        loading = true
        errorMessage = null
        selectedItem = null
        totalResults = 0
        imagesRetrieved = 0

        try {
            var result: Any? = null

            when (category) {
                Category.CHARACTER -> {
                    val response = RetrofitInstance.api.getCharacters(
                        name = appliedSearch.ifBlank { null }
                    )

                    totalResults = response.info.count

                    imagesRetrieved = response.results.count {
                        it.image.isNotBlank()
                    }

                    result = response.results.randomOrNull()
                }

                Category.EPISODE -> {
                    val response = RetrofitInstance.api.getEpisodes(
                        name = appliedSearch.ifBlank { null }
                    )

                    totalResults = response.info.count
                    result = response.results.randomOrNull()
                }

                Category.LOCATION -> {
                    val response = RetrofitInstance.api.getLocations(
                        name = appliedSearch.ifBlank { null }
                    )

                    totalResults = response.info.count
                    result = response.results.randomOrNull()
                }
            }

            selectedItem = result

            if (result == null) {
                errorMessage = "Não foram encontrados resultados."
            }

        } catch (e: CancellationException) {
            throw e

        } catch (e: HttpException) {
            errorMessage = if (e.code() == 404) {
                "Não foram encontrados resultados."
            } else {
                "Erro da API: ${e.code()}"
            }

        } catch (e: Exception) {
            android.util.Log.e(
                "RickAndMortyAPI",
                "Erro ao carregar os dados",
                e
            )

            errorMessage = "Erro: ${e.javaClass.simpleName}: ${e.message}"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Body(
            modifier = Modifier.fillMaxWidth(),
            category = category,
            onCategoryChange = { category = it },
            searchText = searchText,
            onSearchTextChange = { searchText = it },
            onSearch = {
                appliedSearch = searchText.trim()
            },
            imagesRetrieved = imagesRetrieved,
            totalResults =  totalResults
        )
        when {
            loading -> {
                CircularProgressIndicator()
                Text("A carregar dados da API...")
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )

                OutlinedButton(
                    onClick = { rollCounter++ }
                ) {
                    Text("Tentar novamente")
                }
            }

            else -> {
                Box2(modifier = Modifier, imagesRetrieved = imagesRetrieved, selectedItem = selectedItem)
                //Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { rollCounter++ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Roll Random Item",
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Header(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF4E8ED9))
            .padding(top=60.dp, start=10.dp, bottom=10.dp)
    ) {
        Text(
            text = "Rick and Morty Explorer",
            fontSize = 20.sp,
            textAlign = TextAlign.Left
            //style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun Body(
    modifier: Modifier = Modifier,
    category: Category,
    onCategoryChange: (Category) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    imagesRetrieved: Int, totalResults: Int
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Select category",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            CategoryButton(
                text = "Character",
                selected = category == Category.CHARACTER,
                modifier = Modifier.weight(1f),
                onClick = {
                    onCategoryChange(Category.CHARACTER)
                }
            )

            CategoryButton(
                text = "Episode",
                selected = category == Category.EPISODE,
                modifier = Modifier.weight(1f),
                onClick = {
                    onCategoryChange(Category.EPISODE)
                }
            )

            CategoryButton(
                text = "Location",
                selected = category == Category.LOCATION,
                modifier = Modifier.weight(1f),
                onClick = {
                    onCategoryChange(Category.LOCATION)
                }
            )
        }

        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search by name") },
            placeholder = { Text("Ex.: Rick") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSearch()
                }
            )
        )

        Box1(
            modifier = Modifier,
            imagesRetrieved = imagesRetrieved,
            totalResults =  totalResults
        )
    }
}

@Composable
fun Box1(modifier: Modifier, imagesRetrieved: Int, totalResults: Int){
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF78ACC5))
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            text = "API Stats",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Total Characters in API: $totalResults \n" +
                    "Retrieved in current view: $imagesRetrieved items",
            fontSize = 15.sp
        )
    }
}

@Composable
fun Box2(modifier: Modifier, imagesRetrieved: Int, selectedItem: Any?){
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF78ACC5))
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        when (val item = selectedItem) {
            is Character -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    AsyncImage(
                        model = item.image,
                        contentDescription = item.name,
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ){
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ){
                        Text(
                            text = "Status",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.status
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Species",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.species
                        )
                    }
                }
            }

            is Episode -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ){
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ){
                        Text(
                            text = "Air Date",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.airDate
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Episode",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.episode
                        )
                    }
                }
            }

            is Location -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ){
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ){
                        Text(
                            text = "Type",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.type
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Dimension",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.dimension
                        )
                    }
                }
            }

            else -> {
                Text("Não existe nenhum resultado para mostrar.")
            }
        }
    }
}

@Composable
fun CategoryButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text)
        }
    }
}