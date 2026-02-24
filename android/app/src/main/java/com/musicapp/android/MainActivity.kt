package com.musicapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.musicapp.android.ui.components.MiniPlayer
import com.musicapp.android.ui.navigation.MusicAppNavigation
import com.musicapp.android.ui.navigation.Screen
import com.musicapp.android.ui.screens.PlayerScreen
import com.musicapp.android.ui.theme.MusicAppTheme
import com.musicapp.android.viewmodels.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicAppTheme {
                val navController = rememberNavController()
                val playerViewModel: PlayerViewModel = hiltViewModel()
                val currentTrackTitle by playerViewModel.currentTrackTitle.collectAsState()
                val showPlayerSheet by playerViewModel.showPlayerSheet.collectAsState()
                
                val hasTrack = currentTrackTitle != "No track selected"

                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. Main Content Layer
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { 
                                    Text(
                                        "MusicPro", 
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.headlineMedium
                                    ) 
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        bottomBar = {
                            BottomNavigationBar(navController = navController)
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            MusicAppNavigation(
                                navController = navController,
                                paddingValues = PaddingValues(0.dp),
                                playerViewModel = playerViewModel
                            )

                            // Mini Player - Fixed above the Bottom Bar
                            if (hasTrack && !showPlayerSheet) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { playerViewModel.requestExpand() },
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        tonalElevation = 8.dp,
                                        shadowElevation = 4.dp
                                    ) {
                                        MiniPlayer()
                                    }
                                }
                            }
                        }
                    }

                    // 2. Full Player Overlay Layer (Completely new window feel)
                    AnimatedVisibility(
                        visible = showPlayerSheet,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        PlayerScreen(
                            onMinimize = { playerViewModel.dismissPlayerSheet() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        items.forEach { screen ->
            NavigationBarItem(
                icon = { 
                    val icon = when (screen) {
                        Screen.Home -> Icons.Filled.Home
                        Screen.Search -> Icons.Filled.Search
                        Screen.Library -> Icons.Filled.LibraryMusic
                        else -> Icons.Filled.Home
                    }
                    Icon(imageVector = icon, contentDescription = screen.route) 
                },
                label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
