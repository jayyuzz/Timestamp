package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.BosActivity
import com.example.data.model.PhotoCategory
import com.example.ui.MainViewModel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessDark
import com.example.ui.theme.SuccessGreen
import com.example.util.PdfReportGenerator
import com.example.util.WitaTimeUtil
import com.example.util.ZipExportUtil
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportExportScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val activities by viewModel.activities.collectAsState()
  val allDocs by viewModel.documentations.collectAsState()
  val schoolName by viewModel.schoolName.collectAsState()

  var selectedActivity by remember { mutableStateOf<BosActivity?>(activities.firstOrNull()) }
  var dropdownExpanded by remember { mutableStateOf(false) }

  var isExportingPdf by remember { mutableStateOf(false) }
  var isExportingZip by remember { mutableStateOf(false) }

  val activeActivity = selectedActivity ?: activities.firstOrNull()
  val activityDocs = remember(activeActivity, allDocs) {
    if (activeActivity != null) {
      allDocs.filter { it.activityId == activeActivity.id }
    } else emptyList()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAFC))
      .testTag("screen_report")
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
          modifier = Modifier.testTag("btn_back_report")
        ) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Pusat Ekspor Laporan LPJ",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
          )
          Text(
            text = "Kop Resmi & Tanda Tangan • PDF & ZIP",
            fontSize = 11.sp,
            color = GoldAccent
          )
        }
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 2. Select Activity Box
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.White),
          shape = RoundedCornerShape(14.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Pilih Kegiatan Belanja BOS:",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = NavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
              expanded = dropdownExpanded,
              onExpandedChange = { dropdownExpanded = it },
              modifier = Modifier.fillMaxWidth()
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { dropdownExpanded = true }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(12.dp)
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = activeActivity?.namaKegiatan ?: "Pilih Kegiatan...",
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = NavyDark,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "${activeActivity?.kodeKegiatan ?: ""} • ${activityDocs.size} Foto Bukti",
                      fontSize = 11.sp,
                      color = Color(0xFF64748B)
                    )
                  }
                  ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                }
              }

              ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
              ) {
                activities.forEach { act ->
                  val count = allDocs.count { it.activityId == act.id }
                  DropdownMenuItem(
                    text = {
                      Column {
                        Text(text = act.namaKegiatan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(text = "${act.kodeKegiatan} ($count foto bukti)", fontSize = 10.sp, color = Color.Gray)
                      }
                    },
                    onClick = {
                      selectedActivity = act
                      dropdownExpanded = false
                    }
                  )
                }
              }
            }
          }
        }
      }

      // 3. Official Kop Surat Preview Card
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.White),
          shape = RoundedCornerShape(14.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                painter = painterResource(id = R.drawable.ic_smkn3_logo),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(38.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "PEMERINTAH PROVINSI SULAWESI UTARA",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF475569)
                )
                Text(
                  text = "SMK NEGERI 3 KOTAMOBAGU",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = NavyDark
                )
                Text(
                  text = "Format Resmi LPJ BOS Berstandar Inspektorat",
                  fontSize = 10.sp,
                  color = SuccessDark
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFCBD5E1), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Check Status of Categories
            Text(
              text = "Status Kategori Bukti Foto:",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              val categories = listOf("Sebelum", "Proses", "Sesudah", "Barang", "Nota", "Serah Terima")
              categories.forEach { catName ->
                val hasCat = activityDocs.any { it.category.contains(catName, ignoreCase = true) }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (hasCat) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                  ) {
                    Icon(
                      imageVector = if (hasCat) Icons.Default.CheckCircle else Icons.Default.Description,
                      contentDescription = null,
                      tint = if (hasCat) SuccessDark else Color(0xFF94A3B8),
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = catName.split(" ").first(),
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (hasCat) SuccessDark else Color.Gray,
                      maxLines = 1
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 4. Primary Actions (Big Touch Buttons)
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.White),
          shape = RoundedCornerShape(14.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Pilihan Format Ekspor:",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = NavyDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Action 1: Export PDF
            Button(
              onClick = {
                if (activeActivity == null) return@Button
                if (activityDocs.isEmpty()) {
                  Toast.makeText(context, "Belum ada foto bukti untuk kegiatan ini. Silakan foto terlebih dahulu.", Toast.LENGTH_LONG).show()
                  return@Button
                }

                scope.launch {
                  isExportingPdf = true
                  try {
                    val exportDir = File(context.cacheDir, "pdf_exports").apply { if (!exists()) mkdirs() }
                    val pdfFile = PdfReportGenerator.generateActivityReport(
                      context = context,
                      activity = activeActivity,
                      documentations = activityDocs,
                      outputDir = exportDir,
                      schoolName = schoolName
                    )

                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                      type = "application/pdf"
                      putExtra(Intent.EXTRA_STREAM, uri)
                      putExtra(Intent.EXTRA_SUBJECT, "LPJ Dana BOS SMKN 3 Kotamobagu - ${activeActivity.namaKegiatan}")
                      putExtra(Intent.EXTRA_TEXT, "Laporan Resmi Dokumentasi Belanja BOS SMK Negeri 3 Kotamobagu (Tahun 2026)\nKode: ${activeActivity.kodeKegiatan}\nTotal Bukti: ${activityDocs.size} berkas")
                      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Cetak / Bagikan Dokumen PDF"))
                  } catch (e: Exception) {
                    Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                  } finally {
                    isExportingPdf = false
                  }
                }
              },
              enabled = !isExportingPdf && !isExportingZip,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F3A5D)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_export_pdf")
            ) {
              if (isExportingPdf) {
                CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Menyusun Berkas PDF Resmi...", fontSize = 13.sp)
              } else {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text("Cetak Laporan LPJ PDF Resmi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text("Berkop Sekolah & Kolom TTD Bendahara/Kepsek", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action 2: Export ZIP
            OutlinedButton(
              onClick = {
                if (activeActivity == null) return@OutlinedButton
                if (activityDocs.isEmpty()) {
                  Toast.makeText(context, "Belum ada foto bukti untuk kegiatan ini.", Toast.LENGTH_SHORT).show()
                  return@OutlinedButton
                }

                scope.launch {
                  isExportingZip = true
                  try {
                    val exportDir = File(context.cacheDir, "zip_exports").apply { if (!exists()) mkdirs() }
                    val zipFile = ZipExportUtil.exportActivityToZip(
                      activity = activeActivity,
                      documentations = activityDocs,
                      outputDir = exportDir
                    )

                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zipFile)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                      type = "application/zip"
                      putExtra(Intent.EXTRA_STREAM, uri)
                      putExtra(Intent.EXTRA_SUBJECT, "Arsip ZIP Foto BOS - ${activeActivity.kodeKegiatan}")
                      putExtra(Intent.EXTRA_TEXT, "Arsip foto bukti ber-watermark & manifest SHA-256 SMKN 3 Kotamobagu")
                      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Ekspor Arsip ZIP"))
                  } catch (e: Exception) {
                    Toast.makeText(context, "Gagal membuat ZIP: ${e.message}", Toast.LENGTH_SHORT).show()
                  } finally {
                    isExportingZip = false
                  }
                }
              },
              enabled = !isExportingPdf && !isExportingZip,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_export_zip")
            ) {
              if (isExportingZip) {
                CircularProgressIndicator(color = NavyPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mengompres Berkas ZIP...")
              } else {
                Icon(Icons.Default.Archive, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ekspor Arsip ZIP Lengkap (Foto + Manifest)", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}
