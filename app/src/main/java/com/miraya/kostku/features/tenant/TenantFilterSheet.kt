package com.miraya.kostku.features.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.ui.common.FilterChipsRow
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TenantFilterSheet(
    locations: List<RemoteLocation>,
    types: List<RemoteRoomType>,
    locationFilter: String?,
    typeFilter: String?,
    sort: String,
    onLocation: (String?) -> Unit,
    onType: (String?) -> Unit,
    onSort: (String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val sorts = listOf(
        "name_az" to "Nama A–Z",
        "name_za" to "Nama Z–A",
        "days_asc" to "Sisa hari (sedikit dulu)",
        "days_desc" to "Sisa hari (banyak dulu)"
    )
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = Screen.edge).padding(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Filter & Urutkan", style = MaterialTheme.typography.titleLarge)

            Text("Lokasi kos", style = MaterialTheme.typography.labelLarge)
            FilterChipsRow(listOf("Semua") + locations.map { it.name }, locations.firstOrNull { it.id == locationFilter }?.name ?: "Semua") { label ->
                onLocation(locations.firstOrNull { it.name == label }?.id)
            }

            Text("Tipe kamar", style = MaterialTheme.typography.labelLarge)
            FilterChipsRow(listOf("Semua") + types.map { it.name }, types.firstOrNull { it.id == typeFilter }?.name ?: "Semua") { label ->
                onType(types.firstOrNull { it.name == label }?.id)
            }

            Text("Urutkan", style = MaterialTheme.typography.labelLarge)
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                sorts.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        row.forEach { (id, label) ->
                            Box(Modifier.weight(1f)) {
                                FilterChip(selected = sort == id, onClick = { onSort(id) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Reset") }
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Terapkan") }
            }
        }
    }
}
