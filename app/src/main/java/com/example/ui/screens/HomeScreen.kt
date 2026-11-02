package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessDark
import com.example.ui.theme.SuccessGreen
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  onNavigateToCamera: (BosActivity?) -> Unit,
  onNavigateToGallery: () -> Unit,
  onNavigateToActivities: () -> Unit,
  onNavigateToReport: () -> Unit,
  onInspectPhoto: (BosDocumentation) -> Unit
) {
  val userSession by viewModel.userSession.collectAsState()
  val activities by viewModel.activities.collectAsState()
  val documentations by viewModel.documentations.collectAsState()
  val queuedCount by viewModel.queuedCount.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
      .testTag("screen_home"),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    // 1. Hero School Greeting Card
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(NavyDark, NavyPrimary)
            )
          )
          .padding(horizontal = 16.dp, vertical = 20.dp)
      ) {
        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Selamat Datang,",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
              )
              Text(
                text = userSession.displayName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "${userSession.role.label} • SMK Negeri 3 Kotamobagu",
                fontSize = 11.sp,
                color = GoldAccent
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // 3-Metric Summary
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            MetricCard(
              title = "Kegiatan BOS",
              value = "${activities.size}",
              sub = "Tahun 2026",
              modifier = Modifier.weight(1f)
            )
            MetricCard(
              title = "Foto Terverifikasi",
              value = "${documentations.size}",
              sub = "Kamera Langsung",
              modifier = Modifier.weight(1f)
            )
            MetricCard(
              title = "Antrean Offline",
              value = "$queuedCount",
              sub = if (queuedCount > 0) "Siap Sinkron" else "Sinkron",
              highlight = queuedCount > 0,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 2. Primary Action Big Button (Optimized for 360dp mobile screen)
    item {
      Column(modifier = Modifier.padding(16.dp)) {
        ElevatedCard(
          onClick = { onNavigateToCamera(null) },
          colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF0F3A5D)
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_quick_camera")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
          ) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(GoldAccent),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Ambil Foto Bukti BOS",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
              )
              Text(
                text = "Watermark resmi, WITA, GPS & SHA-256 (Tanpa Galeri)",
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sub Actions Grid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ActionGridItem(
            icon = Icons.Default.Collections,
            label = "Galeri Arsip",
            badge = "${documentations.size}",
            color = Color(0xFF0284C7),
            modifier = Modifier.weight(1f),
            testTag = "btn_nav_gallery",
            onClick = onNavigateToGallery
          )

          ActionGridItem(
            icon = Icons.Default.PictureAsPdf,
            label = "Ekspor LPJ PDF",
            badge = "Resmi",
            color = Color(0xFFD97706),
            modifier = Modifier.weight(1f),
            testTag = "btn_nav_report",
            onClick = onNavigateToReport
          )

          ActionGridItem(
            icon = Icons.Default.Folder,
            label = "Kegiatan BOS",
            badge = "${activities.size}",
            color = Color(0xFF0D9488),
            modifier = Modifier.weight(1f),
            testTag = "btn_nav_activities",
            onClick = onNavigateToActivities
          )
        }
      }
    }

    // 3. Recent Photos Carousel
    if (documentations.isNotEmpty()) {
      item {
        Column(modifier = Modifier.padding(top = 8.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
          ) {
            Text(
              text = "Dokumentasi Terbaru",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = NavyDark
            )
            Text(
              text = "Lihat Semua",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = NavyLight,
              modifier = Modifier
                .clickable { onNavigateToGallery() }
                .padding(4.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(documentations.take(6)) { doc ->
              RecentPhotoCard(doc = doc, onClick = { onInspectPhoto(doc) })
            }
          }
        }
      }
    }

    // 4. Active BOS Activities List
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Kegiatan & Komponen BOS 2026",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = NavyDark
          )
          if (userSession.role == UserRole.ADMIN) {
            Text(
              text = "+ Tambah",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = SuccessDark,
              modifier = Modifier
                .clickable { onNavigateToActivities() }
                .padding(4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        activities.forEach { activity ->
          val activityDocs = documentations.filter { it.activityId == activity.id }
          ActivitySummaryItem(
            activity = activity,
            docCount = activityDocs.size,
            onSnapClick = { onNavigateToCamera(activity) }
          )
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  sub: String,
  highlight: Boolean = false,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (highlight) GoldAccent else Color.White.copy(alpha = 0.12f),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = title,
        fontSize = 10.sp,
        color = if (highlight) Color.Black else Color(0xFFCBD5E1),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = value,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = if (highlight) Color.Black else Color.White
      )
      Text(
        text = sub,
        fontSize = 9.sp,
        color = if (highlight) Color(0xFF451A03) else Color(0xFF94A3B8)
      )
    }
  }
}

@Composable
private fun ActionGridItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  badge: String,
  color: Color,
  modifier: Modifier = Modifier,
  testTag: String,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = Color.White),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.testTag(testTag)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        maxLines = 1
      )
      Text(
        text = badge,
        fontSize = 10.sp,
        color = Color.Gray
      )
    }
  }
}

@Composable
private fun RecentPhotoCard(
  doc: BosDocumentation,
  onClick: () -> Unit
) {
  Card(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .width(150.dp)
      .testTag("recent_photo_${doc.id}")
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(95.dp)
          .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
      ) {
        AsyncImage(
          model = File(doc.photoFilePath),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color(0xE60F3A5D),
          modifier = Modifier
            .padding(4.dp)
            .align(Alignment.TopStart)
        ) {
          Text(
            text = doc.category,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
      }
      Column(modifier = Modifier.padding(8.dp)) {
        Text(
          text = doc.activityName,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = doc.witaFormattedDate.split(",").firstOrNull() ?: "",
          fontSize = 10.sp,
          color = Color.Gray
        )
      }
    }
  }
}

@Composable
private fun ActivitySummaryItem(
  activity: BosActivity,
  docCount: Int,
  onSnapClick: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = Color.White),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color(0xFFE2E8F0)
        ) {
          Text(
            text = activity.kodeKegiatan,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (docCount > 0) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
        ) {
          Text(
            text = "$docCount Foto Bukti",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (docCount > 0) SuccessDark else Color.Gray,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = activity.namaKegiatan,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color(0xFF0F172A)
      )

      Text(
        text = activity.komponenBos,
        fontSize = 11.sp,
        color = Color(0xFF64748B),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        val rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(activity.alokasiDana)
        Text(
          text = rupiah,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = NavyPrimary
        )

        Button(
          onClick = onSnapClick,
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          modifier = Modifier.height(32.dp)
        ) {
          Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
