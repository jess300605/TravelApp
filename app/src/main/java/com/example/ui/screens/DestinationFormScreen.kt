package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.DestinationRepository
import com.example.model.Destination
import com.example.ui.components.CountrySpinner
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandDarkPrimary
import com.example.ui.theme.BrandDivider
import com.example.ui.theme.BrandLightPrimary
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryText
import com.example.ui.theme.BrandSecondaryText
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationFormScreen(
    destinationId: String?,
    destinationRepository: DestinationRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = !destinationId.isNullOrBlank()

    // Form field states
    var name by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }

    // Validation error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var countryError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }

    // String resources for validations
    val errEmptyName = stringResource(R.string.error_empty_name)
    val errEmptyCountry = stringResource(R.string.error_empty_country)
    val errEmptyPrice = stringResource(R.string.error_empty_price)
    val errInvalidPrice = stringResource(R.string.error_invalid_price)
    val errEmptyDesc = stringResource(R.string.error_empty_description)
    val errShortDesc = stringResource(R.string.error_short_description)
    val errEmptyImage = stringResource(R.string.error_empty_image)
    val errGeneral = stringResource(R.string.error_general_validation)
    val successSaveMsg = stringResource(if (isEditMode) R.string.destination_updated_success else R.string.destination_saved_success)

    // Pre-populate if editing
    LaunchedEffect(destinationId) {
        if (isEditMode && destinationId != null) {
            val existing = destinationRepository.getDestinationById(destinationId)
            if (existing != null) {
                name = existing.name
                selectedCountry = existing.country
                priceText = if (existing.price > 0) String.format(Locale.US, "%.2f", existing.price) else ""
                description = existing.description
                imageUri = existing.imageUri
            }
        }
    }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localSavedPath = ImageStorageHelper.saveImageToInternalStorage(context, uri)
            if (localSavedPath != null) {
                imageUri = localSavedPath
                imageError = null
            }
        }
    }

    // Sample images available out of the box for quick selection
    val sampleImageResources = listOf(
        Pair("Cancún", "android.resource://${context.packageName}/${R.drawable.travel_cancun}"),
        Pair("París", "android.resource://${context.packageName}/${R.drawable.travel_paris}"),
        Pair("Tokio", "android.resource://${context.packageName}/${R.drawable.travel_tokyo}")
    )

    fun validateForm(): Boolean {
        var isValid = true
        nameError = null
        countryError = null
        priceError = null
        descriptionError = null
        imageError = null
        generalError = null

        // 1. Nombre no nulo ni vacío
        if (name.trim().isEmpty()) {
            nameError = errEmptyName
            isValid = false
        }

        // 2. País seleccionado
        if (selectedCountry.trim().isEmpty()) {
            countryError = errEmptyCountry
            isValid = false
        }

        // 3. Precio mayor a 0 y formato numérico
        val priceVal = priceText.trim().toDoubleOrNull()
        if (priceText.trim().isEmpty()) {
            priceError = errEmptyPrice
            isValid = false
        } else if (priceVal == null || priceVal <= 0.0) {
            priceError = errInvalidPrice
            isValid = false
        }

        // 4. Descripción no vacía y mínimo 20 caracteres
        val descTrimmed = description.trim()
        if (descTrimmed.isEmpty()) {
            descriptionError = errEmptyDesc
            isValid = false
        } else if (descTrimmed.length < 20) {
            descriptionError = errShortDesc
            isValid = false
        }

        // 5. Imagen obligatoria
        if (imageUri.trim().isEmpty()) {
            imageError = errEmptyImage
            isValid = false
        }

        if (!isValid) {
            generalError = errGeneral
        }

        return isValid
    }

    fun submitDestination() {
        if (!validateForm()) return

        isSubmitting = true
        coroutineScope.launch {
            try {
                val parsedPrice = priceText.trim().toDouble()
                val targetId = destinationId ?: UUID.randomUUID().toString()

                val destination = Destination(
                    id = targetId,
                    name = name.trim(),
                    country = selectedCountry.trim(),
                    price = parsedPrice,
                    description = description.trim(),
                    imageUri = imageUri.trim(),
                    createdAt = System.currentTimeMillis()
                )

                val result = if (isEditMode) {
                    destinationRepository.updateDestination(destination)
                } else {
                    destinationRepository.createDestination(destination)
                }

                if (result.isSuccess) {
                    snackbarHostState.showSnackbar(successSaveMsg)
                    onNavigateBack()
                } else {
                    generalError = result.exceptionOrNull()?.localizedMessage ?: "Error al guardar destino."
                }
            } catch (e: Exception) {
                generalError = e.localizedMessage ?: "Error inesperado."
            } finally {
                isSubmitting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(if (isEditMode) R.string.edit_destination_title else R.string.create_destination_title),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("form_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_cancel),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDarkPrimary)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FDFE))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // General Validation Error Notice
            AnimatedVisibility(visible = generalError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = generalError ?: "",
                            color = Color(0xFFC62828),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // SECTION 1: Multimedia Image Selection (Mandatory)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_image_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.image_section_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Image Preview Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandLightPrimary.copy(alpha = 0.3f))
                            .border(
                                BorderStroke(
                                    width = if (imageError != null) 2.dp else 1.dp,
                                    color = if (imageError != null) MaterialTheme.colorScheme.error else BrandDivider
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = stringResource(R.string.image_section_title),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.btn_select_image),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = BrandDarkPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }

                    // Image error message
                    if (imageError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = imageError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gallery Selection Button
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_gallery_image_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = BrandDarkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(if (imageUri.isNotEmpty()) R.string.btn_change_image else R.string.btn_select_image),
                            color = BrandDarkPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sample Photos Quick Selector (very useful in emulator)
                    Text(
                        text = stringResource(R.string.or_choose_sample),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandSecondaryText,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sampleImageResources) { sample ->
                            val isSelected = imageUri == sample.second
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BrandAccent else BrandDivider
                                ),
                                color = Color.White,
                                modifier = Modifier
                                    .clickable {
                                        imageUri = sample.second
                                        imageError = null
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = BrandAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = sample.first,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BrandDarkPrimary else BrandPrimaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: Destination Form Fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Field 1: Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError != null) nameError = null
                        },
                        label = { Text(stringResource(R.string.destination_name_label)) },
                        placeholder = { Text(stringResource(R.string.destination_name_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = BrandPrimary
                            )
                        },
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) {
                                Text(nameError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            focusedLabelColor = BrandPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 2: Country Spinner
                    CountrySpinner(
                        selectedCountry = selectedCountry,
                        onCountrySelected = {
                            selectedCountry = it
                            if (countryError != null) countryError = null
                        },
                        isError = countryError != null,
                        errorMessage = countryError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 3: Price (Numeric validation, > 0)
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { input ->
                            // Allow numbers and dot
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                priceText = input
                                if (priceError != null) priceError = null
                            }
                        },
                        label = { Text(stringResource(R.string.price_label)) },
                        placeholder = { Text(stringResource(R.string.price_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = BrandAccent
                            )
                        },
                        isError = priceError != null,
                        supportingText = {
                            if (priceError != null) {
                                Text(priceError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            focusedLabelColor = BrandPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_price_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 4: Description (Minimum 20 characters)
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            if (descriptionError != null) descriptionError = null
                        },
                        label = { Text(stringResource(R.string.description_label)) },
                        placeholder = { Text(stringResource(R.string.description_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = BrandPrimary
                            )
                        },
                        isError = descriptionError != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (descriptionError != null) {
                                    Text(
                                        text = descriptionError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Text(
                                    text = stringResource(R.string.description_char_counter, description.trim().length),
                                    color = if (description.trim().length >= 20) BrandAccent else BrandSecondaryText,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        minLines = 3,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            focusedLabelColor = BrandPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_description_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SUBMIT BUTTON (Save / Update)
            Button(
                onClick = { submitDestination() },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_destination_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(if (isEditMode) R.string.btn_update_destination else R.string.btn_save_destination),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
