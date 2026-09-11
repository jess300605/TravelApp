package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandPrimaryText
import com.example.ui.theme.BrandSecondaryText

val AVAILABLE_COUNTRIES = listOf(
    "México",
    "España",
    "Francia",
    "Italia",
    "Japón",
    "Perú",
    "Colombia",
    "Estados Unidos",
    "Costa Rica",
    "Argentina",
    "Brasil",
    "Grecia",
    "Tailandia",
    "Reino Unido",
    "Canadá",
    "Egipto"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountrySpinner(
    selectedCountry: String,
    onCountrySelected: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCountry,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.country_label)) },
            placeholder = { Text(stringResource(R.string.country_select_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = stringResource(R.string.country_label),
                    tint = if (isError) OutlinedTextFieldDefaults.colors().errorLeadingIconColor else BrandPrimary
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = isError,
            supportingText = {
                if (isError && errorMessage != null) {
                    Text(errorMessage)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandPrimary,
                unfocusedBorderColor = BrandSecondaryText.copy(alpha = 0.5f),
                focusedLabelColor = BrandPrimary,
                focusedTextColor = BrandPrimaryText,
                unfocusedTextColor = BrandPrimaryText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .testTag("country_spinner_field")
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag("country_spinner_menu")
        ) {
            AVAILABLE_COUNTRIES.forEach { country ->
                DropdownMenuItem(
                    text = { Text(text = country) },
                    onClick = {
                        onCountrySelected(country)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    modifier = Modifier.testTag("country_item_${country.lowercase()}")
                )
            }
        }
    }
}
