package com.arz.rates.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arz.rates.data.RateItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ArzApp(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Explore, null) },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            0 -> HomeScreen(state, vm, Modifier.padding(padding))
            1 -> ExploreScreen(state, vm, Modifier.padding(padding))
            else -> SettingsScreen(state, vm, Modifier.padding(padding))
        }
    }
}

@Composable
private fun HomeScreen(state: AppState, vm: AppViewModel, modifier: Modifier) {
    var firstRun by remember(state.selected) { mutableStateOf(state.selected.isEmpty()) }

    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Your Rates", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (state.loading) "Updating…" else "Live market dashboard",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = vm::refresh) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }

        AnimatedVisibility(state.error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(state.error ?: "", Modifier.weight(1f))
                    TextButton(onClick = vm::refresh) { Text("Retry") }
                }
            }
        }

        if (firstRun) {
            WelcomeSelector(state, vm) { firstRun = false }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                items(
                    state.selected.mapNotNull { key -> state.rates.find { it.key == key } },
                    key = { it.key }
                ) { item ->
                    RateCard(item)
                }
                item {
                    if (state.selected.isEmpty()) {
                        Text("Add currencies from Explore.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeSelector(state: AppState, vm: AppViewModel, done: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = state.rates.filter {
        it.title.contains(query, true) || it.key.contains(query, true)
    }

    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(22.dp))
        Text("Build your dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Choose the rates you want to see on Home. You can change them anytime.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search currencies…") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true
        )
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filtered, key = { it.key }) { item ->
                SelectRow(item, item.key in state.selected) { vm.toggle(item.key) }
            }
        }
        Button(
            onClick = done,
            enabled = state.selected.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) { Text("Continue with ${state.selected.size} selected") }
    }
}

@Composable
private fun ExploreScreen(state: AppState, vm: AppViewModel, modifier: Modifier) {
    var query by remember { mutableStateOf("") }
    val filtered = state.rates.filter {
        it.title.contains(query, true) || it.key.contains(query, true)
    }

    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
        Text("Explore", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("${state.rates.size} rates available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by name or code…") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            items(filtered, key = { it.key }) { item ->
                SelectRow(item, item.key in state.selected) { vm.toggle(item.key) }
            }
        }
    }
}

@Composable
private fun SelectRow(item: RateItem, selected: Boolean, onToggle: () -> Unit) {
    ListItem(
        headlineContent = { Text(item.title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(item.key.uppercase()) },
        trailingContent = {
            IconButton(onClick = onToggle) {
                if (selected) {
                    Icon(Icons.Default.ArrowDownward, "Remove")
                } else {
                    Icon(Icons.Default.Add, "Add")
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    )
}

@Composable
private fun RateCard(item: RateItem) {
    val value = item.rate.value ?: "—"
    val change = item.rate.change
    val positive = (change ?: 0.0) >= 0

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(item.key.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(item.symbol, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(14.dp))
            Text(formatNumber(value), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (positive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    " ${change?.let(::formatDecimal) ?: "0"}",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(item.rate.date ?: "Updated recently", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsScreen(state: AppState, vm: AppViewModel, modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(18.dp))
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Automatic updates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "The dashboard refreshes when the app is active. The home-screen widget uses Android's scheduled widget updates.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Text("Selected rates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${state.selected.size} selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun formatNumber(raw: String): String {
    val n = raw.replace(",", "").toDoubleOrNull() ?: return raw
    return NumberFormat.getNumberInstance(Locale.US).format(n)
}

private fun formatDecimal(n: Double): String =
    NumberFormat.getNumberInstance(Locale.US).format(n)
