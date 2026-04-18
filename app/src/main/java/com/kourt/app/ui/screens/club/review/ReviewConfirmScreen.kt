package com.kourt.app.ui.screens.club.review

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.viewmodel.club.ReviewConfirmViewModel

// NOTE: R.drawable.court must be added to your drawable resources before this screen
// renders the hero image. Place a court image file at res/drawable/court.png (or .webp).


@Composable
fun ReviewConfirmScreen(
    navigation: INavigationRouter,
    viewModel: ReviewConfirmViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) navigation.navigateToHome()
    }

    ReviewConfirmContent(
        uiState = uiState,
        onBack = { navigation.returnBack() },
        onConfirm = { viewModel.onConfirm() },
    )
}

@Composable
private fun ReviewConfirmContent(
    uiState: ReviewConfirmScreenUiState,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    BaseScreen(
        title = stringResource(R.string.review_confirm_title),
        onBack = onBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Scrollable body occupies all available space above the fixed bottom button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                // ── Hero image ────────────────────────────────────────────────
                // NOTE: R.drawable.court must be added by the user (see file-level comment).
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Image(
                        painter = painterResource(R.drawable.court),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp)
                            .clip(shape = RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ── Card 1: Club Identity ─────────────────────────────────
                    ReviewCard {
                        CardSectionHeader(
                            icon = painterResource(R.drawable.shield),
                            title = stringResource(R.string.review_card_club_identity),
                        )
                        CardFieldRow(
                            leadingPainter = painterResource(R.drawable.group),
                            label = stringResource(R.string.review_label_club_name),
                            value = uiState.clubName,
                        )
                        CardDivider()
                        CardFieldRow(
                            leadingPainter = painterResource(R.drawable.short_text),
                            label = stringResource(R.string.review_label_short_name),
                            value = uiState.shortName,
                        )
                    }

                    // ── Card 2: Leadership ────────────────────────────────────
                    ReviewCard {
                        CardSectionHeader(
                            icon = painterResource(R.drawable.group),
                            title = stringResource(R.string.review_card_leadership),
                        )
                        CardFieldRow(
                            leadingIcon = Icons.Default.Person,
                            label = stringResource(R.string.review_label_club_president),
                            value = uiState.president,
                        )
                        CardDivider()
                        CardFieldRow(
                            leadingPainter = painterResource(R.drawable.admin),
                            label = stringResource(R.string.review_label_technical_director),
                            value = uiState.technicalDirector,
                        )
                    }

                    // ── Card 3: Location ──────────────────────────────────────
                    ReviewCard {
                        CardSectionHeader(
                            icon = painterResource(R.drawable.globe),
                            title = stringResource(R.string.review_card_location),
                        )
                        CardFieldRow(
                            leadingIcon = Icons.Default.LocationOn,
                            label = stringResource(R.string.review_label_country),
                            value = uiState.country,
                        )
                        CardDivider()
                        CardFieldRow(
                            leadingIcon = Icons.Default.Home,
                            label = stringResource(R.string.review_label_primary_city),
                            value = uiState.city,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Error message ─────────────────────────────────────────────
                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                    )
                }
            }

            // ── Fixed bottom CTA ──────────────────────────────────────────────
            KourtButton(
                text = stringResource(R.string.review_confirm_button),
                onClick = onConfirm,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun ReviewCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun CardSectionHeader(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val iconColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp),
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Field row variant accepting a vector icon as the leading element.
 */
@Composable
private fun CardFieldRow(
    leadingIcon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val iconColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Field row variant accepting a painter (drawable resource) as the leading element.
 */
@Composable
private fun CardFieldRow(
    leadingPainter: Painter,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val iconColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = leadingPainter,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "ReviewConfirmScreen — dark", showBackground = true)
@Composable
private fun ReviewConfirmScreenDarkPreview() {
    KourtTheme(darkTheme = true) {
        ReviewConfirmContent(
            uiState = ReviewConfirmScreenUiState(
                clubName = "Chicago Bulls",
                shortName = "CHI",
                president = "Jerry Reinsdorf",
                technicalDirector = "Arturas Karnisovas",
                country = "USA",
                city = "Chicago",
            ),
            onBack = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "ReviewConfirmScreen — light", showBackground = true)
@Composable
private fun ReviewConfirmScreenLightPreview() {
    KourtTheme(darkTheme = false) {
        ReviewConfirmContent(
            uiState = ReviewConfirmScreenUiState(
                clubName = "Chicago Bulls",
                shortName = "CHI",
                president = "Jerry Reinsdorf",
                technicalDirector = "Arturas Karnisovas",
                country = "USA",
                city = "Chicago",
            ),
            onBack = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "ReviewConfirmScreen — loading state", showBackground = true)
@Composable
private fun ReviewConfirmScreenLoadingPreview() {
    KourtTheme(darkTheme = true) {
        ReviewConfirmContent(
            uiState = ReviewConfirmScreenUiState(
                clubName = "Chicago Bulls",
                shortName = "CHI",
                president = "Jerry Reinsdorf",
                technicalDirector = "Arturas Karnisovas",
                country = "USA",
                city = "Chicago",
                isLoading = true,
            ),
            onBack = {},
            onConfirm = {},
        )
    }
}
