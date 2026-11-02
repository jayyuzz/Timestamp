package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BosDocumentation
import com.example.data.model.PhotoCategory
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.WatermarkPreviewDialog
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessDark
import com.example.ui.theme.SuccessGreen
import com.example.util.HashUtil
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryArchiveScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit
) {
  val activities by viewModel.activities.collectAsState()
  val userSession by viewModel.userSession.collectAsState()
  val filteredDocs by viewModel.filteredDocumentations.collectAsState()

  val filterActivityId by viewModel.filterActivityId.collectAsState()
  val filterCategory by viewModel.filterCategory.collectAsState()
  val filterOfficer by viewModel.filterOfficer.collectAsState()

  var selectedDocForPreview by remember { mutableStateOf<BosDocumentation?>(null) }
  var docToDelete by remember { mutableStateOf<BosDocumentation?>(null) }
  var activityMenuExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
      .testTag("screen_gallery")
  ) {
    // 1. App Bar Header
    Surface(
      color = NavyDark,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 12.dp)
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("btn_back_gallery")
        ) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Arsip Bukti Dana BOS",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
          )
          Text(
            text = "${filteredDocs.size} Bukti Terverifikasi SHA-256",
            fontSize = 11.sp,
            color = GoldAccent
          )
        }
      }
    }

    // 2. Filter & Search Controls
    Surface(
      color = Color.White,
      shadowElevation = 1.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(12.dp)) {
        // Search Officer & Activity Filter
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = filterOfficer ?: "",
            onValueChange = { viewModel.filterOfficer.value = if (it.isBlank()) null else it },
            placeholder = { Text("Cari Petugas...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
              if (!filterOfficer.isNullOrBlank()) {
                IconButton(onClick = { viewModel.filterOfficer.value = null }) {
                  Icon(Icons.Default.Clear, contentDescription = "Hapus", modifier = Modifier.size(14.dp))
                }
              }
            },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("input_search_officer"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NavyPrimary,
              unfocusedBorderColor = Color(0xFFCBD5E1)
            )
          )

          // Activity Selector Dropdown
          ExposedDropdownMenuBox(
            expanded = activityMenuExpanded,
            onExpandedChange = { activityMenuExpanded = it },
            modifier = Modifier.weight(1.1f)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF1F5F9),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { activityMenuExpanded = true }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
              ) {
                val selectedName = activities.find { it.id == filterActivityId }?.namaKegiatan ?: "Semua Kegiatan"
                Text(
                  text = selectedName,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = NavyDark,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityMenuExpanded)
              }
            }

            ExposedDropdownMenu(
              expanded = activityMenuExpanded,
              onDismissRequest = { activityMenuExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("Semua Kegiatan BOS", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                onClick = {
                  viewModel.filterActivityId.value = null
                  activityMenuExpanded = false
                }
              )
              activities.forEach { act ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(text = act.namaKegiatan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                      Text(text = act.komponenBos, fontSize = 10.sp, color = Color.Gray)
                    }
                  },
                  onClick = {
                    viewModel.filterActivityId.value = act.id
                    activityMenuExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val isAllSelected = filterCategory == null
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isAllSelected) NavyPrimary else Color(0xFFF1F5F9),
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .clickable { viewModel.filterCategory.value = null }
          ) {
            Text(
              text = "Semua Kategori",
              fontSize = 11.sp,
              fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isAllSelected) Color.White else Color(0xFF475569),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }

          PhotoCategory.values().forEach { cat ->
            val isSelected = filterCategory == cat.label
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) NavyPrimary else Color(0xFFF1F5F9),
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                  viewModel.filterCategory.value = if (isSelected) null else cat.label
                }
            ) {
              Text(
                text = cat.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFF475569),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }
        }
      }
    }

    // 3. Documentations List
    if (filteredDocs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.ImageSearch,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Belum Ada Bukti Dokumentasi",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = NavyDark
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Gunakan kamera BOS Cam untuk memotret bukti belanja fisik dan nota dengan watermark resmi.",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredDocs, key = { it.id }) { doc ->
          GalleryItemCard(
            doc = doc,
            canDelete = true,
            onClick = { selectedDocForPreview = doc },
            onDelete = { docToDelete = doc }
          )
        }
      }
    }

    // Detail Preview Dialog
    selectedDocForPreview?.let { doc ->
      WatermarkPreviewDialog(
        doc = doc,
        onDismiss = { selectedDocForPreview = null },
        onDelete = {
          selectedDocForPreview = null
          docToDelete = doc
        }
      )
    }

    // Delete Confirmation Dialog
    docToDelete?.let { doc ->
      AlertDialog(
        onDismissRequest = { docToDelete = null },
        title = { Text("Hapus Bukti Dokumentasi?") },
        text = {
          Text("Bukti ${doc.nomorDokumentasi} (${doc.category}) akan dihapus permanen dari basis data dan memori perangkat.")
        },
        confirmButton = {
          TextButton(
            onClick = {
              viewModel.deleteDocumentation(doc)
              docToDelete = null
            }
          ) {
            Text("Hapus", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { docToDelete = null }) {
            Text("Batal")
          }
        }
      )
    }
  }
}

@Composable
private fun GalleryItemCard(
  doc: BosDocumentation,
  canDelete: Boolean,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val photoFile = File(doc.photoFilePath)

  Card(
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = Color.White),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("doc_card_${doc.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Photo Thumbnail
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFFE2E8F0))
      ) {
        AsyncImage(
          model = photoFile,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Metadata Info
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = NavyPrimary
          ) {
            Text(
              text = doc.category,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          // Sync indicator
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (doc.syncStatus == "SYNCED") Icons.Default.CloudDone else Icons.Default.CloudQueue,
              contentDescription = null,
              tint = if (doc.syncStatus == "SYNCED") SuccessGreen else GoldAccent,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (doc.syncStatus == "SYNCED") "Cloud" else "Antrean",
              fontSize = 9.sp,
              color = Color.Gray
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = doc.activityName,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = Color(0xFF0F172A),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = doc.nomorDokumentasi,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Time & Officer
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(11.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text(text = doc.witaFormattedDate, fontSize = 9.5.sp, color = Color(0xFF64748B), maxLines = 1)
        }

        Spacer(modifier = Modifier.height(3.dp))

        // SHA-256 Chip
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = Color(0xFFECFDF5)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = SuccessDark, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "SHA: ${HashUtil.truncateHash(doc.sha256Hash, 14)}",
              fontFamily = FontFamily.Monospace,
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = SuccessDark
            )
          }
        }
      }

      // Delete Button for Admin
      if (canDelete) {
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Hapus Bukti",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
