package com.example.data_collect.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoultryTopBar(
    onSync: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = com.example.data_collect.R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Poultry Data")
                }
            },
            actions = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Menu")
                }
            }
        )

        AnimatedVisibility(
            visible = expanded,
            enter = slideInHorizontally(
                initialOffsetX = {-it},
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec =  tween(durationMillis = 450, easing = FastOutSlowInEasing)
            ),
            exit  = slideOutHorizontally (
                targetOffsetX = {-it},
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec =  tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val shape = RoundedCornerShape(50)
                val green = Color(0xFF1E7A3D)
                ActionChip("Sync", onSync, green, shape)
                ActionChip("Export", onExport, green, shape)
                ActionChip("Import", onImport, green, shape)
            }
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    onClick: () -> Unit,
    container: Color,
    shape: Shape
) {
    TextButton(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = container,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label)
    }
}
