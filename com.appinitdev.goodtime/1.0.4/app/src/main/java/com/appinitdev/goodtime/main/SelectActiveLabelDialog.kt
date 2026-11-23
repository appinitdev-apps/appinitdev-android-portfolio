/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@Composable
fun SelectActiveLabelDialog(
    viewModel: LabelsViewModel = koinViewModel(),
    initialSelectedLabel: String,
    onNavigateToLabels: () -> Unit,
    onNavigateToActiveLabel: () -> Unit,
    onNavigateToTimerDurations: () -> Unit,
    onClearLabel: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val labels = uiState.unarchivedLabels.filter { !it.isDefault() }.map { it.getLabelData() }

    val labelsIsEmpty = labels.isEmpty()
    val isDefaultLabelActive = initialSelectedLabel == Label.DEFAULT_LABEL_NAME

    if (uiState.isLoading) return
    SelectLabelDialog(
        title = stringResource(R.string.labels_select_active_label),
        singleSelection = true,
        labels = labels,
        initialSelectedLabels = listOf(initialSelectedLabel),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        extraContent = {
            if (labelsIsEmpty) EmptyState()
        },
        buttons = {
            AlertDialogButtonStack {
                TextButton(onClick = {
                    if (isDefaultLabelActive) {
                        onNavigateToTimerDurations()
                    } else {
                        onNavigateToActiveLabel()
                    }
                }) {
                    Text(
                        stringResource(
                            if (labelsIsEmpty || isDefaultLabelActive) {
                                R.string.settings_timer_durations_title
                            } else {
                                R.string.labels_edit_active_label
                            },
                        ),
                    )
                }
                if (!isDefaultLabelActive) {
                    TextButton(onClick = onClearLabel) { Text(stringResource(R.string.labels_clear_label)) }
                }
                TextButton(onClick = onNavigateToLabels) { Text(stringResource(R.string.labels_edit_labels)) }
            }
        },
    )
}

@Composable
private fun EmptyState() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.stats_no_items),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        )
    }
}
