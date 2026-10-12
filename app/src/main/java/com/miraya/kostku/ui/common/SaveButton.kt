package com.miraya.kostku.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miraya.kostku.ui.theme.Dimens
import com.miraya.kostku.ui.theme.Spacing

/**
 * Tombol Simpan dengan status proses yang jelas.
 *
 * Saat [saving] bernilai true, tombol:
 *  - menampilkan spinner bulat + label proses (default "Menyimpan…"),
 *  - menampilkan progress bar tak-tentu (indeterminate) di bagian bawah tombol,
 *  - dinonaktifkan agar tidak tersentuh dua kali.
 *
 * Saat tidak menyimpan, hanya label [label] yang tampil. Bila [error] tidak null,
 * pesan error ditampilkan tepat di bawah tombol dengan warna error.
 */
@Composable
fun SaveButton(
    label: String,
    onClick: () -> Unit,
    saving: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    savingLabel: String = "Menyimpan…",
    error: String? = null,
    height: Dp = Dimens.ButtonHeight,
    icon: ImageVector? = null
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Box(Modifier.fillMaxWidth()) {
            Button(
                onClick = onClick,
                enabled = enabled && !saving,
                modifier = Modifier.fillMaxWidth().height(height),
                shape = MaterialTheme.shapes.large
            ) {
                AnimatedContent(
                    targetState = saving,
                    transitionSpec = {
                        (fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.85f)) togetherWith fadeOut(tween(120))
                    },
                    label = "saveButtonState"
                ) { isSaving ->
                    if (isSaving) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = LocalContentColor.current
                            )
                            Text(savingLabel, style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            icon?.let { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            Text(label, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            if (saving) {
                // Progress bar tak-tentu di dasar tombol sebagai isyarat proses berjalan.
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(MaterialTheme.shapes.large),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = androidx.compose.ui.graphics.Color.Transparent
                )
            }
        }
        error?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
