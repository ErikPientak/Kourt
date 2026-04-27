package com.kourt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.kourt.app.R

val AVATAR_OPTIONS = listOf(
    "a1" to R.drawable.a1,
    "a2" to R.drawable.a2,
    "a3" to R.drawable.a3,
    "a4" to R.drawable.a4,
    "a5" to R.drawable.a5,
    "a6" to R.drawable.a6,
    "a7" to R.drawable.a7,
    "a8" to R.drawable.a8,
    "a9" to R.drawable.a9,
    "a10" to R.drawable.a10,
    "a11" to R.drawable.a11,
    "a12" to R.drawable.a12,
    "a13" to R.drawable.a13,
    "a14" to R.drawable.a14,
    "a15" to R.drawable.a15,
    "a16" to R.drawable.a16,
    "a17" to R.drawable.a17,
    "a18" to R.drawable.a18,
    "a19" to R.drawable.a19,
    "a20" to R.drawable.a20,
    "a21" to R.drawable.a21,
)

fun avatarResId(avatarId: String): Int? =
    AVATAR_OPTIONS.firstOrNull { it.first == avatarId }?.second

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerBottomSheet(
    currentAvatarId: String,
    onAvatarSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Choose Avatar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(AVATAR_OPTIONS) { (id, resId) ->
                    val isSelected = id == currentAvatarId
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                ) else Modifier
                            )
                            .clickable { onAvatarSelected(id) },
                    ) {
                        Image(
                            painter = painterResource(resId),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                        )
                    }
                }
            }
        }
    }
}
