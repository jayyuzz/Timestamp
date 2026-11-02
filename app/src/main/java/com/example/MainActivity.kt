package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import com.example.ui.MainViewModel
import com.example.ui.components.BosTopAppBar
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.components.WatermarkPreviewDialog
import com.example.ui.screens.ActivityManageScreen
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.GalleryArchiveScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportExportScreen
import com.example.ui.theme.BosCamTheme
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import kotlinx.coroutines.flow.collectLatest

enum class AppScreen {
  HOME,
  CAMERA,
  GALLERY,
  ACTIVITIES,
  REPORT
}

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      BosCamTheme {
        val userSession by viewModel.userSession.collectAsState()
        val schoolName by viewModel.schoolName.collectAsState()
        val queuedCount by viewModel.queuedCount.collectAsState()
        val isSyncing by viewModel.isSyncing.collectAsState()

        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
        var targetActivityForCamera by remember { mutableStateOf<BosActivity?>(null) }
        var showRoleDialog by remember { mutableStateOf(false) }
        var inspectedPhoto by remember { mutableStateOf<BosDocumentation?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }

        // Observe toast events
        LaunchedEffect(Unit) {
          viewModel.toastEvent.collectLatest { message ->
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
          }
        }

        // Custom back stack handling
        if (currentScreen != AppScreen.HOME) {
          BackHandler {
            currentScreen = AppScreen.HOME
          }
        }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          snackbarHost = { SnackbarHost(snackbarHostState) },
          topBar = {
            if (currentScreen != AppScreen.CAMERA) {
              BosTopAppBar(
                userSession = userSession,
                queuedCount = queuedCount,
                isSyncing = isSyncing,
                onRoleClick = { showRoleDialog = true },
                onSyncClick = { viewModel.triggerSync() }
              )
            }
          },
          bottomBar = {
            if (currentScreen != AppScreen.CAMERA) {
              NavigationBar(
                containerColor = NavyDark,
                modifier = Modifier
                  .navigationBarsPadding()
                  .testTag("bottom_nav")
              ) {
                NavigationBarItem(
                  selected = currentScreen == AppScreen.HOME,
                  onClick = { currentScreen = AppScreen.HOME },
                  icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                  label = { Text("Beranda", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyDark,
                    selectedTextColor = GoldAccent,
                    indicatorColor = GoldAccent,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                  ),
                  modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                  selected = currentScreen == AppScreen.CAMERA,
                  onClick = {
                    targetActivityForCamera = null
                    currentScreen = AppScreen.CAMERA
                  },
                  icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Kamera") },
                  label = { Text("Kamera", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyDark,
                    selectedTextColor = GoldAccent,
                    indicatorColor = GoldAccent,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                  ),
                  modifier = Modifier.testTag("nav_item_camera")
                )

                NavigationBarItem(
                  selected = currentScreen == AppScreen.GALLERY,
                  onClick = { currentScreen = AppScreen.GALLERY },
                  icon = {
                    if (queuedCount > 0) {
                      BadgedBox(
                        badge = {
                          Badge(containerColor = GoldAccent, contentColor = Color.Black) {
                            Text("$queuedCount", fontSize = 9.sp)
                          }
                        }
                      ) {
                        Icon(Icons.Default.Collections, contentDescription = "Galeri")
                      }
                    } else {
                      Icon(Icons.Default.Collections, contentDescription = "Galeri")
                    }
                  },
                  label = { Text("Galeri", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyDark,
                    selectedTextColor = GoldAccent,
                    indicatorColor = GoldAccent,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                  ),
                  modifier = Modifier.testTag("nav_item_gallery")
                )

                NavigationBarItem(
                  selected = currentScreen == AppScreen.ACTIVITIES,
                  onClick = { currentScreen = AppScreen.ACTIVITIES },
                  icon = { Icon(Icons.Default.Folder, contentDescription = "Kegiatan") },
                  label = { Text("Kegiatan", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyDark,
                    selectedTextColor = GoldAccent,
                    indicatorColor = GoldAccent,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                  ),
                  modifier = Modifier.testTag("nav_item_activities")
                )

                NavigationBarItem(
                  selected = currentScreen == AppScreen.REPORT,
                  onClick = { currentScreen = AppScreen.REPORT },
                  icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "Laporan") },
                  label = { Text("LPJ PDF", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyDark,
                    selectedTextColor = GoldAccent,
                    indicatorColor = GoldAccent,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                  ),
                  modifier = Modifier.testTag("nav_item_report")
                )
              }
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            AnimatedContent(
              targetState = currentScreen,
              transitionSpec = { fadeIn() togetherWith fadeOut() },
              label = "screen_transition"
            ) { screen ->
              when (screen) {
                AppScreen.HOME -> {
                  HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCamera = { act ->
                      targetActivityForCamera = act
                      currentScreen = AppScreen.CAMERA
                    },
                    onNavigateToGallery = { currentScreen = AppScreen.GALLERY },
                    onNavigateToActivities = { currentScreen = AppScreen.ACTIVITIES },
                    onNavigateToReport = { currentScreen = AppScreen.REPORT },
                    onInspectPhoto = { doc -> inspectedPhoto = doc }
                  )
                }

                AppScreen.CAMERA -> {
                  CameraCaptureScreen(
                    viewModel = viewModel,
                    initialActivity = targetActivityForCamera,
                    onBack = { currentScreen = AppScreen.HOME }
                  )
                }

                AppScreen.GALLERY -> {
                  GalleryArchiveScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                  )
                }

                AppScreen.ACTIVITIES -> {
                  ActivityManageScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                  )
                }

                AppScreen.REPORT -> {
                  ReportExportScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = AppScreen.HOME }
                  )
                }
              }
            }
          }
        }

        // Role Switcher Modal Dialog
        if (showRoleDialog) {
          RoleSwitcherDialog(
            currentSession = userSession,
            currentSchoolName = schoolName,
            onDismiss = { showRoleDialog = false },
            onRoleSelected = { role ->
              viewModel.setRole(role)
            },
            onNameChanged = { name ->
              viewModel.setOfficerName(name)
            },
            onSchoolNameChanged = { name ->
              viewModel.setSchoolName(name)
            }
          )
        }

        // Photo Inspect Modal Dialog
        inspectedPhoto?.let { doc ->
          WatermarkPreviewDialog(
            doc = doc,
            onDismiss = { inspectedPhoto = null }
          )
        }
      }
    }
  }
}
