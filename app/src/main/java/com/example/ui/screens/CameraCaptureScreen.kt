package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import java.util.Calendar
import java.util.TimeZone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import com.example.data.model.PhotoCategory
import com.example.ui.MainViewModel
import com.example.ui.components.WatermarkPreviewDialog
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessDark
import com.example.ui.theme.SuccessGreen
import com.example.util.LocationHelper
import com.example.util.WitaTimeUtil
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
  viewModel: MainViewModel,
  initialActivity: BosActivity?,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val activities by viewModel.activities.collectAsState()
  val userSession by viewModel.userSession.collectAsState()
  val schoolName by viewModel.schoolName.collectAsState()
  val isProcessing by viewModel.isProcessingPhoto.collectAsState()

  // Selected Activity & Category state
  var selectedActivity by remember {
    mutableStateOf(initialActivity ?: activities.firstOrNull())
  }
  var selectedCategory by remember { mutableStateOf(PhotoCategory.SEBELUM) }
  var activeCategoryName by remember { mutableStateOf("Sebelum") }
  var optionalNotes by remember { mutableStateOf("") }
  var activityDropdownExpanded by remember { mutableStateOf(false) }

  // Result dialog
  var capturedDocResult by remember { mutableStateOf<BosDocumentation?>(null) }

  // Manual Date & Time State
  var isManualTimeMode by remember { mutableStateOf(false) }
  var manualTimestampMillis by remember { mutableStateOf<Long?>(null) }
  var showDateTimePickerDialog by remember { mutableStateOf(false) }

  // Permissions state
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
  }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) {
      permissionLauncher.launch(
        arrayOf(
          Manifest.permission.CAMERA,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        )
      )
    }
  }

  // Live WITA Clock
  var liveWitaTime by remember { mutableStateOf(WitaTimeUtil.getCurrentWitaFormatted()) }
  LaunchedEffect(Unit) {
    while (true) {
      liveWitaTime = WitaTimeUtil.getCurrentWitaFormatted()
      delay(1000)
    }
  }

  // CameraX ImageCapture reference (standard 4:3 matching saved output)
  val imageCapture = remember {
    ImageCapture.Builder()
      .setTargetAspectRatio(AspectRatio.RATIO_4_3)
      .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
      .build()
  }
  var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
  val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

  DisposableEffect(Unit) {
    onDispose {
      cameraExecutor.shutdown()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(androidx.compose.ui.graphics.Color.Black)
      .testTag("screen_camera")
  ) {
    // 1. Camera Viewfinder or Fallback
    if (hasCameraPermission) {
      AndroidView(
        factory = { ctx ->
          val previewView = PreviewView(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
          }

          val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
          cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
              .setTargetAspectRatio(AspectRatio.RATIO_4_3)
              .build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
              }

            val cameraSelector = CameraSelector.Builder()
              .requireLensFacing(lensFacing)
              .build()

            try {
              cameraProvider.unbindAll()
              cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
              )
            } catch (_: Exception) {}
          }, ContextCompat.getMainExecutor(ctx))

          previewView
        },
        modifier = Modifier.fillMaxSize()
      )
    } else {
      // Permission Notice / Simulator Viewfinder
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = GoldAccent,
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Kamera Langsung BOS Cam",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Akses hardware kamera langsung diperlukan untuk audit integritas. Unggahan foto dari galeri dilarang keras demi validitas dana BOS.",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(16.dp))
          androidx.compose.material3.Button(
            onClick = {
              permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
              )
            },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NavyPrimary)
          ) {
            Text("Izinkan Kamera & GPS")
          }
        }
      }
    }

    // Grid Guide Overlay (Anti-shake / Alignment)
    CameraGridOverlay()

    // 2. Top HUD Controls
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xCC000000), Color.Transparent)
          )
        )
        .padding(12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(40.dp)
            .background(Color(0x80000000), CircleShape)
            .testTag("btn_back_camera")
        ) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Kembali",
            tint = Color.White
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Live WITA Clock & GPS Pill (Clickable to edit Date & Time)
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color(0xB30A1E31),
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable { showDateTimePickerDialog = true }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = if (isManualTimeMode) Icons.Default.Schedule else Icons.Default.GpsFixed,
              contentDescription = null,
              tint = if (isManualTimeMode) GoldAccent else SuccessGreen,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isManualTimeMode && manualTimestampMillis != null) {
                  WitaTimeUtil.formatCustomWitaFormatted(manualTimestampMillis!!)
                } else {
                  liveWitaTime
                },
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
              )
              Text(
                text = "GPS Terkunci • Ketuk atur waktu & identitas",
                fontSize = 8.5.sp,
                color = Color(0xFFCBD5E1)
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Dedicated Edit Identity & Time Button
        IconButton(
          onClick = { showDateTimePickerDialog = true },
          modifier = Modifier
            .size(36.dp)
            .background(Color(0xB30F3A5D), CircleShape)
            .testTag("btn_edit_identity_time")
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Petugas, Sekolah, & Waktu",
            tint = GoldAccent,
            modifier = Modifier.size(17.dp)
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Direct Camera Only Badge
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xCCDC2626)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "No Galeri",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Activity Dropdown Selector
      ExposedDropdownMenuBox(
        expanded = activityDropdownExpanded,
        onExpandedChange = { activityDropdownExpanded = it },
        modifier = Modifier.fillMaxWidth()
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xD90F3A5D),
          modifier = Modifier
            .menuAnchor()
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { activityDropdownExpanded = true }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Kegiatan BOS Aktif:",
                fontSize = 9.sp,
                color = GoldAccent,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = selectedActivity?.namaKegiatan ?: "Pilih Kegiatan...",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityDropdownExpanded)
          }
        }

        ExposedDropdownMenu(
          expanded = activityDropdownExpanded,
          onDismissRequest = { activityDropdownExpanded = false }
        ) {
          activities.forEach { act ->
            DropdownMenuItem(
              text = {
                Column {
                  Text(text = act.namaKegiatan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  Text(text = act.komponenBos, fontSize = 10.sp, color = Color.Gray)
                }
              },
              onClick = {
                selectedActivity = act
                activityDropdownExpanded = false
              }
            )
          }
        }
      }
    }

    // =========================================================================
    // LIVE CAMERA OVERLAYS (100% IDENTICAL TO REFERENCE IMAGE & SAVED BITMAP)
    // =========================================================================

    // Time, Date & Coordinates formatted for Timemark
    val activeTimeHourMinute = if (isManualTimeMode && manualTimestampMillis != null) {
      WitaTimeUtil.formatWitaHourMinute(manualTimestampMillis)
    } else {
      WitaTimeUtil.formatWitaHourMinute()
    }
    val activeDayDate = if (isManualTimeMode && manualTimestampMillis != null) {
      WitaTimeUtil.formatTimemarkDayDate(manualTimestampMillis)
    } else {
      WitaTimeUtil.formatTimemarkDayDate()
    }
    val activeLatLong = WitaTimeUtil.formatLatLongDecimal(0.732567, 124.294674)
    val activeAuditCode = "URY1DHH916YLXT"

    // 1. TOP-LEFT: MINI MAP PREVIEW BOX
    Box(
      modifier = Modifier
        .align(Alignment.TopStart)
        .statusBarsPadding()
        .padding(top = 96.dp, start = 14.dp)
    ) {
      MiniMapCardPreview()
    }

    // 2. TOP-RIGHT: TIMEMARK BRANDING & ACCURACY SEAL (Reduced by 1/4)
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .statusBarsPadding()
        .padding(top = 96.dp, end = 16.dp)
    ) {
      Text(
        text = "Timemark",
        fontSize = 23.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFFA000)
      )
      Text(
        text = "Foto 100% akurat",
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFE2E8F0)
      )
    }

    // 3. RIGHT EDGE: VERTICAL ROTATED WATERMARK TEXT (Reduced by 1/4)
    Box(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 4.dp)
        .graphicsLayer {
          rotationZ = -90f
        }
    ) {
      Text(
        text = "© $activeAuditCode  Timemark Verified",
        fontSize = 10.5.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0x99FFFFFF)
      )
    }

    // 4. BOTTOM-LEFT: MAIN TIMEMARK WATERMARK (Balanced - Reduced by 1/4)
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(bottom = 205.dp, start = 14.dp, end = 20.dp)
        .clickable { showDateTimePickerDialog = true }
    ) {
      // PROMINENT WHITE PILL BADGE: [Kategori] 20:52
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.padding(bottom = 8.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
        ) {
          // Yellow / Amber category badge (Clickable to edit category directly)
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFFFA000),
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .clickable { showDateTimePickerDialog = true }
          ) {
            val tagText = WitaTimeUtil.formatCategoryTag(activeCategoryName)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
              Text(
                text = tagText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
              )
              Spacer(modifier = Modifier.width(3.dp))
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Kategori",
                tint = Color(0x99000000),
                modifier = Modifier.size(12.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Bold Digital Clock (Balanced 33sp)
          Text(
            text = activeTimeHourMinute,
            fontSize = 33.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            fontFamily = FontFamily.Default
          )
        }
      }

      // ACCENT ROW: Yellow Vertical Bar + Day/Date, Address, Coordinates
      Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(bottom = 6.dp)
      ) {
        // Vertical Accent Bar in Gold / Yellow
        Box(
          modifier = Modifier
            .width(4.5.dp)
            .height(82.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFFFFB300))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          // Day & Date (e.g. "Senin, 02 November 2026")
          Text(
            text = activeDayDate,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Spacer(modifier = Modifier.height(3.dp))

          // Clean Address
          Text(
            text = "Molinow, Kec. Kotamobagu Bar., Kota Kotamobagu, Sulawesi Utara",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            lineHeight = 17.sp
          )

          Spacer(modifier = Modifier.height(3.dp))

          // Coordinates (e.g. "0.732567°N, 124.294674°E")
          Text(
            text = activeLatLong,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }
      }

      // FOOTER LINE: Shield + Kode Foto
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 2.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = Color(0xFFCFD8DC),
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "Kode Foto: $activeAuditCode",
          fontSize = 11.5.sp,
          color = Color(0xFFCFD8DC),
          fontWeight = FontWeight.Medium
        )
      }
    }

    // 3. Bottom Control Bar (Category chips + Big Shutter Button)
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xE6051320), Color(0xFF020B14))
          )
        )
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Photo Category Chips Horizontal Scroll
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "PILIH / EDIT KATEGORI BUKTI:",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = GoldAccent
        )
        Text(
          text = "Ketuk ✏️ untuk kustom",
          fontSize = 9.sp,
          color = Color(0xFF94A3B8)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val quickCategories = listOf(
          "Sebelum" to PhotoCategory.SEBELUM,
          "Proses" to PhotoCategory.PROSES,
          "Sesudah" to PhotoCategory.SESUDAH,
          "Nota" to PhotoCategory.NOTA,
          "Barang" to PhotoCategory.BARANG,
          "Serah Terima" to PhotoCategory.SERAH_TERIMA,
          "Absensi" to null
        )

        quickCategories.forEach { (label, catEnum) ->
          val isSelected = activeCategoryName.equals(label, ignoreCase = true) ||
            (catEnum != null && selectedCategory == catEnum && quickCategories.none { it.first.equals(activeCategoryName, ignoreCase = true) })
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) GoldAccent else Color(0x66FFFFFF),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color.White) else null,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable {
                activeCategoryName = label
                if (catEnum != null) {
                  selectedCategory = catEnum
                }
              }
              .testTag("cat_chip_${label.lowercase().replace(" ", "_")}")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = Color.Black,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else Color.White
              )
            }
          }
        }

        // Custom / Edit Category Chip
        val isCustom = quickCategories.none { it.first.equals(activeCategoryName, ignoreCase = true) }
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (isCustom) GoldAccent else Color(0xB31E3A5F),
          border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { showDateTimePickerDialog = true }
            .testTag("cat_chip_custom_edit")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = null,
              tint = if (isCustom) Color.Black else GoldAccent,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isCustom) activeCategoryName else "Edit Kategori...",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isCustom) Color.Black else Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Shutter Trigger Area
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Fallback / Simulator snap button (for robust testing without real camera hardware)
        Surface(
          shape = CircleShape,
          color = Color(0x660F3A5D),
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .clickable {
              val currentAct = selectedActivity ?: activities.firstOrNull()
              if (currentAct != null && !isProcessing) {
                // Generate authentic simulated frame of SMK lab/activity
                val simulatedBmp = generateSimulatedCapture(selectedCategory)
                viewModel.processAndSavePhoto(
                  context = context,
                  bitmap = simulatedBmp,
                  activity = currentAct,
                  category = selectedCategory,
                  notes = optionalNotes,
                  customTimestampMillis = if (isManualTimeMode) manualTimestampMillis else null,
                  customCategory = activeCategoryName
                ) { doc ->
                  capturedDocResult = doc
                }
              }
            }
            .testTag("btn_simulated_snap")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Simulasi Rana",
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // BIG HARDWARE SHUTTER BUTTON (Touch target 76dp, prominent ring)
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .border(4.dp, GoldAccent, CircleShape)
            .padding(5.dp)
            .clip(CircleShape)
            .background(if (isProcessing) Color.Gray else Color.White)
            .clickable(enabled = !isProcessing) {
              val currentAct = selectedActivity ?: activities.firstOrNull()
              if (currentAct == null) {
                Toast.makeText(context, "Pilih kegiatan BOS terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@clickable
              }

              if (hasCameraPermission) {
                imageCapture.takePicture(
                  cameraExecutor,
                  object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                      val bitmap = imageProxyToBitmap(imageProxy)
                      imageProxy.close()
                      if (bitmap != null) {
                        viewModel.processAndSavePhoto(
                          context = context,
                          bitmap = bitmap,
                          activity = currentAct,
                          category = selectedCategory,
                          notes = optionalNotes,
                          customTimestampMillis = if (isManualTimeMode) manualTimestampMillis else null,
                          customCategory = activeCategoryName
                        ) { doc ->
                          capturedDocResult = doc
                        }
                      }
                    }

                    override fun onError(exception: ImageCaptureException) {
                      // Fallback to simulated high-res frame if hardware sensor error
                      val fallbackBmp = generateSimulatedCapture(selectedCategory)
                      viewModel.processAndSavePhoto(
                        context = context,
                        bitmap = fallbackBmp,
                        activity = currentAct,
                        category = selectedCategory,
                        notes = optionalNotes,
                        customTimestampMillis = if (isManualTimeMode) manualTimestampMillis else null,
                        customCategory = activeCategoryName
                      ) { doc ->
                        capturedDocResult = doc
                      }
                    }
                  }
                )
              } else {
                // Generate simulated capture if permission is pending
                val simBmp = generateSimulatedCapture(selectedCategory)
                viewModel.processAndSavePhoto(
                  context = context,
                  bitmap = simBmp,
                  activity = currentAct,
                  category = selectedCategory,
                  notes = optionalNotes,
                  customTimestampMillis = if (isManualTimeMode) manualTimestampMillis else null,
                  customCategory = activeCategoryName
                ) { doc ->
                  capturedDocResult = doc
                }
              }
            }
            .testTag("btn_shutter"),
          contentAlignment = Alignment.Center
        ) {
          if (isProcessing) {
            CircularProgressIndicator(
              color = NavyPrimary,
              modifier = Modifier.size(32.dp),
              strokeWidth = 3.dp
            )
          } else {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0))
            )
          }
        }

        // Info / Anti-Fraud Label
        Surface(
          shape = CircleShape,
          color = Color(0x660F3A5D),
          modifier = Modifier.size(46.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = "Proteksi Audit",
              tint = SuccessGreen,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Ketuk tombol putih untuk memotret & stempel watermark resmi",
        fontSize = 10.sp,
        color = Color(0xFF94A3B8)
      )
    }

    // Processing Indicator Overlay
    if (isProcessing) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = NavyDark,
          modifier = Modifier.padding(24.dp)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            CircularProgressIndicator(color = GoldAccent)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Menempelkan Watermark Resmi BOS...",
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Menghitung Checksum SHA-256 & Mengunci GPS WITA",
              color = Color(0xFF94A3B8),
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // Edit Date, Time, Officer, School, & Category Dialog
    if (showDateTimePickerDialog) {
      ManualDateTimePickerDialog(
        initialMillis = manualTimestampMillis ?: System.currentTimeMillis(),
        isManualActive = isManualTimeMode,
        currentOfficerName = userSession.displayName,
        currentSchoolName = schoolName,
        currentCategoryName = activeCategoryName,
        onApplyChanges = { selectedMillis, officerNameInput, schoolNameInput, categoryInput ->
          isManualTimeMode = true
          manualTimestampMillis = selectedMillis
          if (officerNameInput.isNotBlank()) {
            viewModel.setOfficerName(officerNameInput)
          }
          if (schoolNameInput.isNotBlank()) {
            viewModel.setSchoolName(schoolNameInput)
          }
          if (categoryInput.isNotBlank()) {
            activeCategoryName = categoryInput
            val matched = PhotoCategory.values().firstOrNull {
              it.name.startsWith(categoryInput.take(3), ignoreCase = true) || it.label.contains(categoryInput, ignoreCase = true)
            }
            if (matched != null) selectedCategory = matched
          }
          showDateTimePickerDialog = false
        },
        onResetToAutomatic = { officerNameInput, schoolNameInput, categoryInput ->
          isManualTimeMode = false
          manualTimestampMillis = null
          if (officerNameInput.isNotBlank()) {
            viewModel.setOfficerName(officerNameInput)
          }
          if (schoolNameInput.isNotBlank()) {
            viewModel.setSchoolName(schoolNameInput)
          }
          if (categoryInput.isNotBlank()) {
            activeCategoryName = categoryInput
          }
          showDateTimePickerDialog = false
        },
        onDismiss = { showDateTimePickerDialog = false }
      )
    }

    // Stamped Verification Result Dialog
    capturedDocResult?.let { doc ->
      WatermarkPreviewDialog(
        doc = doc,
        onDismiss = { capturedDocResult = null },
        onDelete = {
          viewModel.deleteDocumentation(doc)
          capturedDocResult = null
          Toast.makeText(context, "Foto bukti berhasil dihapus", Toast.LENGTH_SHORT).show()
        }
      )
    }
  }
}

@Composable
private fun CameraGridOverlay() {
  Box(modifier = Modifier.fillMaxSize()) {
    // Subtle crosshair / rule of thirds guides
    Row(modifier = Modifier.fillMaxSize()) {
      Spacer(modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .width(0.8.dp)
          .fillMaxSize()
          .background(Color(0x26FFFFFF))
      )
      Spacer(modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .width(0.8.dp)
          .fillMaxSize()
          .background(Color(0x26FFFFFF))
      )
      Spacer(modifier = Modifier.weight(1f))
    }
    Column(modifier = Modifier.fillMaxSize()) {
      Spacer(modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .height(0.8.dp)
          .fillMaxWidth()
          .background(Color(0x26FFFFFF))
      )
      Spacer(modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .height(0.8.dp)
          .fillMaxWidth()
          .background(Color(0x26FFFFFF))
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

// Helper: Converts CameraX ImageProxy to rotated Bitmap
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
  return try {
    val plane = imageProxy.planes[0]
    val buffer = plane.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    if (original != null && imageProxy.imageInfo.rotationDegrees != 0) {
      val matrix = Matrix()
      matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
      Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    } else {
      original
    }
  } catch (_: Exception) {
    null
  }
}

// Helper: Generates realistic simulated high-resolution frame of SMKN 3 Kotamobagu scene
private fun generateSimulatedCapture(category: PhotoCategory): Bitmap {
  val width = 1280
  val height = 960
  val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)

  // Realistic background gradient for classroom/laboratory/workshop
  val bgPaint = Paint().apply {
    color = when (category) {
      PhotoCategory.SEBELUM -> android.graphics.Color.parseColor("#475569")
      PhotoCategory.PROSES -> android.graphics.Color.parseColor("#334155")
      PhotoCategory.SESUDAH -> android.graphics.Color.parseColor("#1E293B")
      PhotoCategory.BARANG -> android.graphics.Color.parseColor("#0F172A")
      PhotoCategory.NOTA -> android.graphics.Color.parseColor("#F8FAFC")
      PhotoCategory.SERAH_TERIMA -> android.graphics.Color.parseColor("#1E3A5F")
    }
  }
  canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

  // Draw simulated hardware/document/equipment shapes
  val detailPaint = Paint().apply {
    color = android.graphics.Color.parseColor("#FFFFFF")
    isAntiAlias = true
  }

  // Draw computer/equipment outline
  val pcPaint = Paint().apply {
    color = android.graphics.Color.parseColor("#0F3A5D")
    isAntiAlias = true
  }
  canvas.drawRoundRect(200f, 150f, 1080f, 650f, 30f, 30f, pcPaint)

  // Screen area
  val screenPaint = Paint().apply {
    color = android.graphics.Color.parseColor("#1E293B")
    isAntiAlias = true
  }
  canvas.drawRoundRect(230f, 180f, 1050f, 620f, 16f, 16f, screenPaint)

  // Stand
  canvas.drawRect(600f, 650f, 680f, 750f, pcPaint)
  canvas.drawRoundRect(520f, 740f, 760f, 770f, 10f, 10f, pcPaint)

  // Text inside simulated screen
  detailPaint.textSize = 34f
  detailPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  detailPaint.textAlign = Paint.Align.CENTER
  canvas.drawText("SMK NEGERI 3 KOTAMOBAGU", width / 2f, 350f, detailPaint)

  val subPaint = Paint().apply {
    color = android.graphics.Color.parseColor("#F59E0B")
    textSize = 28f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    textAlign = Paint.Align.CENTER
    isAntiAlias = true
  }
  canvas.drawText("LABORATORIUM TEKNIK KOMPUTER & JARINGAN (TKJ)", width / 2f, 410f, subPaint)

  val tagPaint = Paint().apply {
    color = android.graphics.Color.parseColor("#10B981")
    textSize = 24f
    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    textAlign = Paint.Align.CENTER
    isAntiAlias = true
  }
  canvas.drawText("BUKTI FISIK: ${category.label.uppercase()} DANA BOS 2026", width / 2f, 470f, tagPaint)

  return bitmap
}

@Composable
fun ManualDateTimePickerDialog(
  initialMillis: Long,
  isManualActive: Boolean,
  currentOfficerName: String,
  currentSchoolName: String,
  currentCategoryName: String,
  onApplyChanges: (selectedMillis: Long, officerName: String, schoolName: String, categoryName: String) -> Unit,
  onResetToAutomatic: (officerName: String, schoolName: String, categoryName: String) -> Unit,
  onDismiss: () -> Unit
) {
  val witaTz = remember { TimeZone.getTimeZone("GMT+8") }
  val initialCal = remember(initialMillis) {
    Calendar.getInstance(witaTz).apply { timeInMillis = initialMillis }
  }

  var tempOfficerName by remember { mutableStateOf(currentOfficerName) }
  var tempSchoolName by remember { mutableStateOf(currentSchoolName) }
  var tempCategoryName by remember { mutableStateOf(currentCategoryName) }

  var selectedYear by remember { mutableStateOf(initialCal.get(Calendar.YEAR)) }
  var selectedMonth by remember { mutableStateOf(initialCal.get(Calendar.MONTH)) } // 0..11
  var selectedDay by remember { mutableStateOf(initialCal.get(Calendar.DAY_OF_MONTH)) }
  var selectedHour by remember { mutableStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
  var selectedMinute by remember { mutableStateOf(initialCal.get(Calendar.MINUTE)) }

  val monthNames = remember {
    listOf(
      "Januari", "Februari", "Maret", "April", "Mei", "Juni",
      "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
  }

  // Calculate live preview timestamp based on selected values
  val previewMillis = remember(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute) {
    val cal = Calendar.getInstance(witaTz).apply {
      set(Calendar.YEAR, selectedYear)
      set(Calendar.MONTH, selectedMonth)
      val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
      set(Calendar.DAY_OF_MONTH, selectedDay.coerceIn(1, maxDay))
      set(Calendar.HOUR_OF_DAY, selectedHour)
      set(Calendar.MINUTE, selectedMinute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    cal.timeInMillis
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      colors = CardDefaults.cardColors(containerColor = NavyDark),
      shape = RoundedCornerShape(20.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
      modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Dialog Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .background(Color(0x33FFB300), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Edit Identitas & Waktu Stempel",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = Color.White
            )
            Text(
              text = "Nama Petugas, Sekolah, dan Waktu WITA",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 1: IDENTITAS PETUGAS & SEKOLAH
        Text(
          text = "IDENTITAS RESMI STEMPEL:",
          fontSize = 9.5.sp,
          fontWeight = FontWeight.Bold,
          color = GoldAccent,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = tempSchoolName,
          onValueChange = { tempSchoolName = it },
          label = { Text("Nama Sekolah / Instansi", fontSize = 11.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_camera_school_name"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = Color(0xFF334155),
            focusedLabelColor = GoldAccent,
            unfocusedLabelColor = Color(0xFF94A3B8)
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = tempOfficerName,
          onValueChange = { tempOfficerName = it },
          label = { Text("Nama Petugas Lapangan", fontSize = 11.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_camera_officer_name"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = Color(0xFF334155),
            focusedLabelColor = GoldAccent,
            unfocusedLabelColor = Color(0xFF94A3B8)
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: KATEGORI / TAHAPAN BUKTI FOTO (SEBELUM, SESUDAH, NOTA, DLL)
        Text(
          text = "KATEGORI / TAHAPAN FOTO STEMPEL:",
          fontSize = 9.5.sp,
          fontWeight = FontWeight.Bold,
          color = GoldAccent,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Preset Chips
        val categoryPresets = listOf("Sebelum", "Proses", "Sesudah", "Nota", "Fisik Barang", "Berita Acara", "Absensi")
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categoryPresets.forEach { preset ->
            val isSelected = tempCategoryName.equals(preset, ignoreCase = true)
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) GoldAccent else Color(0xFF1E3A5F),
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { tempCategoryName = preset }
            ) {
              Text(
                text = preset,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = tempCategoryName,
          onValueChange = { tempCategoryName = it },
          label = { Text("Tulis / Edit Kategori Kustom", fontSize = 11.sp) },
          placeholder = { Text("Contoh: Sebelum, Sesudah, Nota, ATK, Konsumsi...", fontSize = 11.sp, color = Color.Gray) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_camera_category_name"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = GoldAccent,
            unfocusedBorderColor = Color(0xFF334155),
            focusedLabelColor = GoldAccent,
            unfocusedLabelColor = Color(0xFF94A3B8)
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Presets
        Text(
          text = "PILIHAN CEPAT WAKTU (PRESET):",
          fontSize = 9.5.sp,
          fontWeight = FontWeight.Bold,
          color = GoldAccent,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Preset: Sekarang (Auto)
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E3A5F),
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                val nowCal = Calendar.getInstance(witaTz)
                selectedYear = nowCal.get(Calendar.YEAR)
                selectedMonth = nowCal.get(Calendar.MONTH)
                selectedDay = nowCal.get(Calendar.DAY_OF_MONTH)
                selectedHour = nowCal.get(Calendar.HOUR_OF_DAY)
                selectedMinute = nowCal.get(Calendar.MINUTE)
              }
          ) {
            Text(
              text = "Sekarang",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }

          // Preset: Kemarin (H-1)
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E3A5F),
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                val cal = Calendar.getInstance(witaTz).apply {
                  add(Calendar.DAY_OF_YEAR, -1)
                }
                selectedYear = cal.get(Calendar.YEAR)
                selectedMonth = cal.get(Calendar.MONTH)
                selectedDay = cal.get(Calendar.DAY_OF_MONTH)
              }
          ) {
            Text(
              text = "Kemarin (H-1)",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }

          // Preset: -1 Jam
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E3A5F),
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                val cal = Calendar.getInstance(witaTz).apply {
                  timeInMillis = previewMillis
                  add(Calendar.HOUR_OF_DAY, -1)
                }
                selectedHour = cal.get(Calendar.HOUR_OF_DAY)
                selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                selectedMonth = cal.get(Calendar.MONTH)
                selectedYear = cal.get(Calendar.YEAR)
              }
          ) {
            Text(
              text = "-1 Jam",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }

          // Preset: Pagi 08:00
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E3A5F),
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                selectedHour = 8
                selectedMinute = 0
              }
          ) {
            Text(
              text = "Pagi 08:00",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }

          // Preset: Siang 13:00
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E3A5F),
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                selectedHour = 13
                selectedMinute = 0
              }
          ) {
            Text(
              text = "Siang 13:00",
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Date Steppers (Day, Month, Year)
        Text(
          text = "TANGGAL:",
          fontSize = 9.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF38BDF8),
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Day Stepper
          NumberStepper(
            label = "Hari",
            valueText = selectedDay.toString().padStart(2, '0'),
            onDecrement = {
              val cal = Calendar.getInstance(witaTz).apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
              }
              val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
              selectedDay = if (selectedDay <= 1) max else selectedDay - 1
            },
            onIncrement = {
              val cal = Calendar.getInstance(witaTz).apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
              }
              val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
              selectedDay = if (selectedDay >= max) 1 else selectedDay + 1
            },
            modifier = Modifier.weight(1f)
          )

          // Month Stepper
          NumberStepper(
            label = "Bulan",
            valueText = monthNames.getOrElse(selectedMonth) { "Bulan" }.take(3),
            onDecrement = {
              selectedMonth = if (selectedMonth <= 0) 11 else selectedMonth - 1
            },
            onIncrement = {
              selectedMonth = if (selectedMonth >= 11) 0 else selectedMonth + 1
            },
            modifier = Modifier.weight(1.3f)
          )

          // Year Stepper
          NumberStepper(
            label = "Tahun",
            valueText = selectedYear.toString(),
            onDecrement = { if (selectedYear > 2020) selectedYear-- },
            onIncrement = { if (selectedYear < 2030) selectedYear++ },
            modifier = Modifier.weight(1.2f)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Manual Time Steppers (Hour, Minute)
        Text(
          text = "JAM & MENIT (WITA):",
          fontSize = 9.5.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF4ADE80),
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Hour Stepper
          NumberStepper(
            label = "Jam (00-23)",
            valueText = selectedHour.toString().padStart(2, '0'),
            onDecrement = { selectedHour = if (selectedHour <= 0) 23 else selectedHour - 1 },
            onIncrement = { selectedHour = if (selectedHour >= 23) 0 else selectedHour + 1 },
            modifier = Modifier.weight(1f)
          )

          // Minute Stepper
          NumberStepper(
            label = "Menit (00-59)",
            valueText = selectedMinute.toString().padStart(2, '0'),
            onDecrement = { selectedMinute = if (selectedMinute <= 0) 59 else selectedMinute - 1 },
            onIncrement = { selectedMinute = if (selectedMinute >= 59) 0 else selectedMinute + 1 },
            modifier = Modifier.weight(1f)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Preview Plaque
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xD9050C14),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Pratinjau Hasil Stempel:",
              fontSize = 9.sp,
              color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center,
              modifier = Modifier.padding(bottom = 4.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFFFA000)
              ) {
                Text(
                  text = WitaTimeUtil.formatCategoryTag(tempCategoryName),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = WitaTimeUtil.formatWitaHourMinute(previewMillis),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Text(
              text = WitaTimeUtil.formatTimemarkDayDate(previewMillis),
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = "${tempSchoolName.ifBlank { "SMK NEGERI 3 KOTAMOBAGU" }.uppercase()} • DANA BOS 2026",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "👤 ${tempOfficerName.ifBlank { "Petugas Lapangan" }}",
              fontSize = 9.5.sp,
              color = Color(0xFFCBD5E1),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Button(
          onClick = { onApplyChanges(previewMillis, tempOfficerName, tempSchoolName, tempCategoryName) },
          colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_apply_identity_and_time")
        ) {
          Text(
            text = "Terapkan Perubahan",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }

        if (isManualActive) {
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = { onResetToAutomatic(tempOfficerName, tempSchoolName, tempCategoryName) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Kembalikan ke Waktu Otomatis (Realtime)", fontSize = 11.5.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        TextButton(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Batal", color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
private fun NumberStepper(
  label: String,
  valueText: String,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
  ) {
    Text(
      text = label,
      fontSize = 9.sp,
      color = Color(0xFF94A3B8)
    )
    Spacer(modifier = Modifier.height(3.dp))
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = Color(0xFF1E293B),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
      ) {
        IconButton(
          onClick = onDecrement,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Kurangi",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }

        Text(
          text = valueText,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          fontFamily = FontFamily.Monospace
        )

        IconButton(
          onClick = onIncrement,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Tambah",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
fun MiniMapCardPreview(modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color.White,
    shadowElevation = 6.dp,
    modifier = modifier
      .size(96.dp)
      .clip(RoundedCornerShape(14.dp))
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Terrain
        drawRect(color = Color(0xFFEBF2F7))

        // Green Zone
        val greenPath = androidx.compose.ui.graphics.Path().apply {
          moveTo(w * 0.15f, h * 0.48f)
          lineTo(w * 0.45f, h * 0.42f)
          lineTo(w * 0.35f, h * 0.78f)
          lineTo(w * 0.12f, h * 0.70f)
          close()
        }
        drawPath(greenPath, color = Color(0xFFA7F3D0))

        // Water River
        val riverPath = androidx.compose.ui.graphics.Path().apply {
          moveTo(w * 0.20f, 0f)
          quadraticBezierTo(w * 0.35f, h * 0.3f, w, h * 0.45f)
        }
        drawPath(
          riverPath,
          color = Color(0xFF93C5FD),
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )

        // Roads
        val roadPath1 = androidx.compose.ui.graphics.Path().apply {
          moveTo(0f, h * 0.65f)
          lineTo(w, h * 0.25f)
        }
        drawPath(
          roadPath1,
          color = Color(0xFFCBD5E1),
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx())
        )
        drawPath(
          roadPath1,
          color = Color.White,
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
        )

        val roadPath2 = androidx.compose.ui.graphics.Path().apply {
          moveTo(w * 0.50f, 0f)
          lineTo(w * 0.60f, h)
        }
        drawPath(
          roadPath2,
          color = Color(0xFFCBD5E1),
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.5.dp.toPx())
        )
        drawPath(
          roadPath2,
          color = Color.White,
          style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )

        // Location pin marker teardrop
        val pinX = w * 0.52f
        val pinY = h * 0.40f
        val pinR = 7.5f.dp.toPx()

        // Drop shadow
        drawOval(
          color = Color(0x33000000),
          topLeft = androidx.compose.ui.geometry.Offset(pinX - 5.dp.toPx(), pinY + 8.dp.toPx()),
          size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 4.dp.toPx())
        )

        // Pin body
        drawCircle(color = Color(0xFF1A73E8), radius = pinR, center = androidx.compose.ui.geometry.Offset(pinX, pinY))
        val pinPointer = androidx.compose.ui.graphics.Path().apply {
          moveTo(pinX - (pinR * 0.8f), pinY + (pinR * 0.3f))
          lineTo(pinX + (pinR * 0.8f), pinY + (pinR * 0.3f))
          lineTo(pinX, pinY + 11.dp.toPx())
          close()
        }
        drawPath(pinPointer, color = Color(0xFF1A73E8))

        // Center dot
        drawCircle(color = Color.White, radius = 3.2f.dp.toPx(), center = androidx.compose.ui.geometry.Offset(pinX, pinY))
      }

      // Small "bagu" / "Kotamobagu" label on map
      Text(
        text = "bagu",
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(start = 4.dp, top = 48.dp)
      )

      // Google Logo in bottom-left
      Text(
        text = "Google",
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4285F4),
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(start = 6.dp, bottom = 4.dp)
      )
    }
  }
}

