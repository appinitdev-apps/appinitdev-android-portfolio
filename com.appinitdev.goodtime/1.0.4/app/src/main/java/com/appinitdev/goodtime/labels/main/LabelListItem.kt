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
fun LabelListItem(
    label: Label,
    isDragging: Boolean,
    @SuppressLint("ModifierParameter")
    dragModifier: Modifier,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val labelName = label.name

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .clickable { onEdit() }
                .let {
                    if (isDragging) {
                        it.background(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.05f),
                        )
                    } else {
                        it
                    }
                }.padding(4.dp),
    ) {
        Icon(
            modifier =
                dragModifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {},
                ),
            imageVector = Icons.Filled.DragIndicator,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            contentDescription = "Drag indicator for $labelName",
        )
        Icon(
            modifier =
                Modifier
                    .padding(8.dp),
            imageVector = Icons.AutoMirrored.Outlined.Label,
            contentDescription = labelName,
            tint = MaterialTheme.getLabelColor(label.colorIndex),
        )
        Text(
            modifier = Modifier.weight(1f),
            text = labelName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
        )

        var dropDownMenuExpanded by remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { dropDownMenuExpanded = true }) {
                Icon(
                    EvaIcons.Outline.MoreVertical,
                    contentDescription = stringResource(R.string.labels_more_about, labelName),
                )
            }
            BetterDropdownMenu(
                expanded = dropDownMenuExpanded,
                onDismissRequest = { dropDownMenuExpanded = false },
            ) {
                val paddingModifier = Modifier.padding(end = 32.dp)
                DropdownMenuItem(
                    modifier = firstMenuItemModifier,
                    text = {
                        Text(
                            modifier = paddingModifier,
                            text = stringResource(R.string.main_edit),
                        )
                    },
                    onClick = {
                        onEdit()
                        dropDownMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            EvaIcons.Outline.Edit,
                            contentDescription =
                                stringResource(
                                    R.string.labels_edit_label,
                                    labelName,
                                ),
                        )
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            modifier = paddingModifier,
                            text = stringResource(R.string.main_duplicate),
                        )
                    },
                    onClick = {
                        onDuplicate()
                        dropDownMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            EvaIcons.Outline.Copy,
                            contentDescription =
                                stringResource(
                                    R.string.labels_duplicate_label_name,
                                    labelName,
                                ),
                        )
                    },
                )
                SubtleHorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(
                            modifier = paddingModifier,
                            text = stringResource(R.string.labels_archived),
                        )
                    },
                    onClick = {
                        onArchive()
                        dropDownMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            EvaIcons.Outline.Archive,
                            contentDescription =
                                stringResource(
                                    R.string.labels_archive_label_name,
                                    labelName,
                                ),
                        )
                    },
                )
                DropdownMenuItem(
                    modifier = lastMenuItemModifier,
                    text = {
                        Text(
                            modifier = paddingModifier,
                            text = stringResource(R.string.main_delete),
                        )
                    },
                    onClick = {
                        onDelete()
                        dropDownMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            EvaIcons.Outline.Trash,
                            contentDescription =
                                stringResource(
                                    R.string.labels_delete_label_name,
                                    labelName,
                                ),
                        )
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun LabelCardPreview() {
    LabelListItem(
        label =
            Label(
                name = "Default",
                useDefaultTimeProfile = false,
                timerProfile = TimerProfile(sessionsBeforeLongBreak = 4),
            ),
        isDragging = false,
        dragModifier = Modifier,
        onEdit = {},
        onDuplicate = {},
        onArchive = {},
        onDelete = {},
    )
}
