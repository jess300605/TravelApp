package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.AuthRepository
import com.example.data.DestinationRepository
import com.example.model.Destination
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandDarkPrimary
import com.example.ui.theme.BrandDivider
import com.example.ui.theme.BrandLightPrimary
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryText
import com.example.ui.theme.BrandSecondaryText
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ui.adapter.DestinationAdapter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    destinationRepository: DestinationRepository,
    authRepository: AuthRepository,
    onCreateClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val destinations by destinationRepository.destinations.collectAsState()
    val isSyncing by destinationRepository.isSyncing.collectAsState()
    val currentUser by authRepository.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCountryFilter by remember { mutableStateOf<String?>(null) }
    var useRecyclerViewXml by remember { mutableStateOf(true) }

    var destinationToDelete by remember { mutableStateOf<Destination?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val deletedSuccessMsg = stringResource(R.string.destination_deleted_success)

    // Countries available in current list for quick filtering
    val countries = remember(destinations) {
        destinations.map { it.country }.filter { it.isNotBlank() }.distinct().sorted()
    }

    // Filtered list
    val filteredDestinations = destinations.filter { dest ->
        val matchesQuery = searchQuery.isBlank() ||
                dest.name.contains(searchQuery, ignoreCase = true) ||
                dest.country.contains(searchQuery, ignoreCase = true) ||
                dest.description.contains(searchQuery, ignoreCase = true)

        val matchesCountry = selectedCountryFilter == null || dest.country == selectedCountryFilter

        matchesQuery && matchesCountry
    }

    // Delete Confirmation Dialog
    if (destinationToDelete != null) {
        val dest = destinationToDelete!!
        AlertDialog(
            onDismissRequest = { destinationToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_dialog_message, dest.name),
                    color = BrandPrimaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDeleteId = dest.id
                        destinationToDelete = null
                        coroutineScope.launch {
                            val result = destinationRepository.deleteDestination(toDeleteId)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar(deletedSuccessMsg)
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(
                        text = stringResource(R.string.btn_delete_confirm),
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { destinationToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text(text = stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = BrandPrimary
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.logout_confirm_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_confirm_message),
                    color = BrandPrimaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authRepository.logout()
                        onLogout()
                    },
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text(
                        text = stringResource(R.string.btn_confirm),
                        color = BrandPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.testTag("cancel_logout_button")
                ) {
                    Text(text = stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.catalog_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        currentUser?.let { user ->
                            Text(
                                text = "${stringResource(R.string.agent_session_active)} ${user.displayName.ifEmpty { user.email }}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrandLightPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                    }
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.testTag("catalog_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.logout),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDarkPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = BrandAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_destination_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.btn_add_destination),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar & Filter Section
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = BrandPrimary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = BrandDivider
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_destinations_input")
                    )

                    // Country Chips
                    if (countries.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCountryFilter == null,
                                    onClick = { selectedCountryFilter = null },
                                    label = { Text(stringResource(R.string.filter_all)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandLightPrimary,
                                        selectedLabelColor = BrandDarkPrimary
                                    )
                                )
                            }
                            items(countries) { country ->
                                FilterChip(
                                    selected = selectedCountryFilter == country,
                                    onClick = {
                                        selectedCountryFilter =
                                            if (selectedCountryFilter == country) null else country
                                    },
                                    label = { Text(country) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandLightPrimary,
                                        selectedLabelColor = BrandDarkPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Destinations List (RecyclerView with CardView pattern)
            if (filteredDestinations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            tint = BrandDivider,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_destinations_found),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimaryText
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_destinations_cta),
                            style = MaterialTheme.typography.bodyMedium.copy(color = BrandSecondaryText),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else if (useRecyclerViewXml) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.total_destinations_count, filteredDestinations.size),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = BrandSecondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        FilterChip(
                            selected = useRecyclerViewXml,
                            onClick = { useRecyclerViewXml = !useRecyclerViewXml },
                            label = { Text(if (useRecyclerViewXml) "RecyclerView XML" else "Compose") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandLightPrimary,
                                selectedLabelColor = BrandDarkPrimary
                            )
                        )
                    }

                    AndroidView(
                        factory = { ctx ->
                            val rv = LayoutInflater.from(ctx).inflate(
                                R.layout.layout_destinations_recycler,
                                null,
                                false
                            ) as RecyclerView
                            rv.layoutManager = LinearLayoutManager(ctx)
                            rv.adapter = DestinationAdapter(
                                items = filteredDestinations,
                                onEditClick = { onEditClick(it.id) },
                                onDeleteClick = { destinationToDelete = it }
                            )
                            rv
                        },
                        update = { rv ->
                            (rv.adapter as? DestinationAdapter)?.updateData(filteredDestinations)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("destinations_recycler_view")
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("destinations_recycler_view"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.total_destinations_count, filteredDestinations.size),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = BrandSecondaryText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            FilterChip(
                                selected = useRecyclerViewXml,
                                onClick = { useRecyclerViewXml = !useRecyclerViewXml },
                                label = { Text(if (useRecyclerViewXml) "RecyclerView XML" else "Compose") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandLightPrimary,
                                    selectedLabelColor = BrandDarkPrimary
                                )
                            )
                        }
                    }

                    items(
                        items = filteredDestinations,
                        key = { it.id }
                    ) { destination ->
                        DestinationCardItem(
                            destination = destination,
                            onEdit = { onEditClick(destination.id) },
                            onDelete = { destinationToDelete = destination }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DestinationCardItem(
    destination: Destination,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("destination_card_${destination.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Destination Image (Coil Image Loading)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(destination.imageUri.ifEmpty { R.drawable.travel_cancun })
                        .crossfade(true)
                        .build(),
                    contentDescription = destination.name,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.travel_cancun),
                    error = painterResource(R.drawable.travel_cancun),
                    modifier = Modifier.fillMaxSize()
                )

                // Country Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BrandDarkPrimary.copy(alpha = 0.9f),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = destination.country,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Price Tag Badge (Accent Color #8BC34A)
                Surface(
                    shape = RoundedCornerShape(bottomStart = 16.dp),
                    color = BrandAccent,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = String.format(Locale.US, "$%.2f USD", destination.price),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Card Body Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Name
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimaryText
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Description
                Text(
                    text = destination.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = BrandSecondaryText,
                        lineHeight = 20.sp
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BrandDivider.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Edit & Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_destination_btn_${destination.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.btn_edit),
                            tint = BrandDarkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.btn_edit),
                            color = BrandDarkPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_destination_btn_${destination.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.btn_delete),
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.btn_delete),
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
