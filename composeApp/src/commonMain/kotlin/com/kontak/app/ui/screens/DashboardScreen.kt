package com.kontak.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kontak.app.model.Profile
import com.kontak.app.ui.theme.KontakBgSoft
import com.kontak.app.ui.theme.KontakGold
import com.kontak.app.ui.theme.KontakGreen
import com.kontak.app.ui.theme.KontakGreenDark
import com.kontak.app.ui.theme.KontakInk
import com.kontak.app.ui.theme.KontakLine
import com.kontak.app.ui.theme.KontakMuted
import com.kontak.app.ui.theme.KontakOrange

@Composable
fun DashboardScreen(
    profile: Profile,
    onLogout: () -> Unit,
) {
    val initials = remember(profile) {
        val source = profile.business ?: profile.nom ?: ""
        source.trim().split(Regex("\\s+")).mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("Mon espace", style = MaterialTheme.typography.labelSmall, color = KontakOrange)
        Text(
            "Bonjour ${profile.nom ?: ""}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )

        // ---- Carte résumé de la fiche ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(KontakInk),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials.ifBlank { "--" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.business ?: profile.nom ?: "—", style = MaterialTheme.typography.titleLarge)
                Text(
                    listOfNotNull(profile.ville, profile.pays).joinToString(", ").ifBlank { "—" },
                    color = KontakMuted,
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Ligne de statistiques ----
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox(label = "Secteur", value = profile.secteur ?: "—", modifier = Modifier.weight(1f))
            StatBox(label = "Contact", value = profile.nom ?: "—", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ---- Carte crédits (visuel dégradé, comme le site) ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(KontakInk, KontakGreenDark, KontakGreen)
                    ),
                    RoundedCornerShape(20.dp),
                )
                .padding(22.dp),
        ) {
            Text("Kontak", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "${profile.credits ?: 0}",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("crédits", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Solde disponible", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Text(
                    "${profile.solde ?: 0} FCFA",
                    color = KontakGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Solde de crédits", style = MaterialTheme.typography.titleLarge)
        Text(
            "Contacter un membre sur WhatsApp coûte 2 crédits.",
            color = KontakMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        Button(
            onClick = { /* TODO: naviguer vers l'écran Abonnement (prochain écran à construire) */ },
            colors = ButtonDefaults.buttonColors(containerColor = KontakGreen),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Acheter des crédits")
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Déconnexion")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(KontakBgSoft, RoundedCornerShape(14.dp))
            .padding(16.dp),
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, color = KontakMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
