package com.kontak.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kontak.app.data.ApiResult
import com.kontak.app.data.PaymentsRepository
import com.kontak.app.model.CREDIT_PACKS
import com.kontak.app.model.CreditPack
import com.kontak.app.model.Plan
import com.kontak.app.platform.openUrl
import com.kontak.app.ui.theme.KontakBgSoft
import com.kontak.app.ui.theme.KontakGreen
import com.kontak.app.ui.theme.KontakGreenDark
import com.kontak.app.ui.theme.KontakMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Composable
fun AbonnementScreen(
    onCreditsUpdated: () -> Unit,
    onBack: () -> Unit,
) {
    val paymentsRepository = remember { PaymentsRepository() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var plans by remember { mutableStateOf<List<Plan>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingPayment by remember { mutableStateOf(false) }

    // Charge les forfaits une fois à l'ouverture de l'écran
    DisposableEffect(Unit) {
        coroutineScope.launch {
            when (val result = paymentsRepository.listPlans()) {
                is ApiResult.Success -> plans = result.data
                is ApiResult.Error -> Unit
            }
        }
        onDispose { }
    }

    fun launchPayment(amount: Int, description: String, metadataPairs: Map<String, String>, returnPath: String) {
        coroutineScope.launch {
            statusMessage = "Redirection vers le paiement…"
            val metadata = buildJsonObject {
                metadataPairs.forEach { (key, value) -> put(key, JsonPrimitive(value)) }
            }
            when (val result = paymentsRepository.createCheckout(amount, description, metadata, returnPath)) {
                is ApiResult.Success -> {
                    openUrl(result.data)
                    statusMessage = "Une fois le paiement terminé, reviens ici et appuie sur \"Vérifier mon paiement\"."
                }
                is ApiResult.Error -> statusMessage = result.message
            }
        }
    }

    fun checkPayment() {
        coroutineScope.launch {
            isCheckingPayment = true
            statusMessage = "Vérification du paiement…"
            repeat(6) { attempt ->
                when (val result = paymentsRepository.checkPendingPayment()) {
                    is ApiResult.Success -> {
                        when (result.data) {
                            "paid" -> {
                                statusMessage = "Paiement confirmé ✓"
                                isCheckingPayment = false
                                onCreditsUpdated()
                                return@launch
                            }
                            "failed", "cancelled", "expired" -> {
                                statusMessage = "Le paiement n'a pas abouti."
                                isCheckingPayment = false
                                return@launch
                            }
                            null -> {
                                statusMessage = "Aucun paiement en attente."
                                isCheckingPayment = false
                                return@launch
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        statusMessage = result.message
                        isCheckingPayment = false
                        return@launch
                    }
                }
                delay(2500)
            }
            statusMessage = "Toujours en attente — réessaie dans un instant."
            isCheckingPayment = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(20.dp)) {
            androidx.compose.material3.TextButton(onClick = onBack, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                Text("← Retour", fontSize = 13.sp, color = KontakMuted)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Recharge ton compte", style = MaterialTheme.typography.labelSmall)
            Text("Crédits & Abonnement", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 4.dp))
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Crédits") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Abonnement") })
        }

        statusMessage?.let {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp, 12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                if (isCheckingPayment) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 10.dp))
                }
                Text(it, fontSize = 13.sp, color = KontakMuted)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            if (selectedTab == 0) {
                Text(
                    "2 crédits sont débités à chaque contact WhatsApp ou téléchargement de fiche.",
                    color = KontakMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(CREDIT_PACKS) { pack ->
                        CreditPackCard(pack) {
                            launchPayment(
                                amount = pack.price,
                                description = "${pack.credits} crédits Kontak",
                                metadataPairs = mapOf("kind" to "credits", "credits" to pack.credits.toString()),
                                returnPath = "abonnement",
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { checkPayment() },
                    enabled = !isCheckingPayment,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Vérifier mon paiement")
                }
            } else {
                if (plans.isEmpty()) {
                    Text("Aucun forfait disponible pour l'instant.", color = KontakMuted)
                } else {
                    plans.forEach { plan ->
                        PlanCard(plan) {
                            launchPayment(
                                amount = plan.price,
                                description = "Abonnement Kontak — ${plan.name}",
                                metadataPairs = mapOf(
                                    "kind" to "subscription",
                                    "plan_id" to plan.id,
                                    "plan_name" to plan.name,
                                ),
                                returnPath = "abonnement",
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Button(
                        onClick = { checkPayment() },
                        enabled = !isCheckingPayment,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Vérifier mon paiement")
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CreditPackCard(pack: CreditPack, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text("${pack.credits}", style = MaterialTheme.typography.titleLarge, color = KontakGreenDark)
        Text("crédits", fontSize = 10.sp, color = KontakMuted)
        Spacer(modifier = Modifier.height(8.dp))
        Text("${pack.price} FCFA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = KontakGreen),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Acheter", fontSize = 12.sp)
        }
    }
}

@Composable
private fun PlanCard(plan: Plan, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KontakBgSoft, RoundedCornerShape(16.dp))
            .padding(18.dp),
    ) {
        Text(plan.name, style = MaterialTheme.typography.titleLarge)
        Text("${plan.price} FCFA / ${plan.period}", color = KontakGreenDark, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        plan.trialDays?.let {
            if (it > 0) Text("Essai gratuit $it jours", fontSize = 11.sp, color = KontakMuted, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        plan.features.forEach { feature ->
            Text("• $feature", fontSize = 12.sp, color = KontakMuted, modifier = Modifier.padding(bottom = 2.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = KontakGreen),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("S'abonner")
        }
    }
}
