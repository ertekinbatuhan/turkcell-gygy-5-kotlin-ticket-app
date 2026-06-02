package com.flowbytestudio.core.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A highly reusable and premium Pull-to-Refresh container that wraps the standard
 * Material 3 PullToRefreshBox for consistent styling and behavior across the app.
 *
 * @param isRefreshing Whether the refreshing indicator should be visible.
 * @param onRefresh Callback to trigger when a pull-to-refresh gesture is completed.
 * @param modifier Optional modifier to apply to the container.
 * @param content The scrollable content inside the pull-to-refresh box, provided with BoxScope.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketAppPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        content = content
    )
}
