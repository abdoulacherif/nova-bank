package com.kontak.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kontak.app.data.ApiResult
import com.kontak.app.data.CreateProfileRequest
import com.kontak.app.data.ProfileRepository
import com.kontak.app.model.Profile
import com.kontak.app.model.SECTORS
import kotlinx.coroutines.launch

@Composable
fun CompleteProfileScreen(
    onProfileCreated: (Profile) -> Unit,
) {
    val profileRepository = remember { ProfileRepository() }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var business by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf(SECTORS.first()) }
    var sectorExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text("Dernière étape", style = MaterialTheme.typography.labelSmall)
        Text(
            "Complète ta fiche",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        Text(
            "Ton compte existe mais aucune fiche n'y est associée. Remplis ces informations pour apparaître dans l'annuaire.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom complet") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Numéro WhatsApp") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        OutlinedTextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Pays") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Ville") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )
        OutlinedTextField(
            value = business,
            onValueChange = { business = it },
            label = { Text("Nom du business") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        )

        ExposedDropdownMenuBox(
            expanded = sectorExpanded,
            onExpandedChange = { sectorExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        ) {
            OutlinedTextField(
                value = sector,
                onValueChange = {},
                readOnly = true,
                label = { Text("Secteur d'activité") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectorExpanded) },
                modifier = Modifier.fillMaxWidth(),
            )
            androidx.compose.material3.ExposedDropdownMenu(
                expanded = sectorExpanded,
                onDismissRequest = { sectorExpanded = false },
            ) {
                SECTORS.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            sector = option
                            sectorExpanded = false
                        },
                    )
                }
            }
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            enabled = !isLoading,
            onClick = {
                if (name.isBlank() || phone.isBlank() || country.isBlank() || city.isBlank() || business.isBlank()) {
                    errorMessage = "Merci de remplir tous les champs."
                    return@Button
                }
                errorMessage = null
                isLoading = true
                coroutineScope.launch {
                    val result = profileRepository.createProfile(
                        CreateProfileRequest(
                            nom = name.trim(),
                            whatsapp = phone.trim(),
                            pays = country.trim(),
                            ville = city.trim(),
                            business = business.trim(),
                            secteur = sector,
                        )
                    )
                    isLoading = false
                    when (result) {
                        is ApiResult.Success -> onProfileCreated(result.data)
                        is ApiResult.Error -> errorMessage = result.message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Text(if (isLoading) "Création…" else "Créer ma fiche", fontWeight = FontWeight.Bold)
        }
    }
}
