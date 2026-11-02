package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.BosDocumentation
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessDark
import com.example.ui.theme.SuccessGreen
import com.example.util.WitaTimeUtil
import java.io.File
import java.util.Locale

@Composable
fun WatermarkPreviewDialog(
  doc: BosDocumentation,
  onDismiss: () -> Unit,
  onDelete: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val photoFile = File(doc.photoFilePath)
  var showDeleteConfirmDialog by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.90f)
        .testTag("dialog_watermark_preview")
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Top Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(SuccessGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = SuccessDark,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Verifikasi Bukti Dana BOS",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = NavyDark
            )
            Text(
              text = "SMK Negeri 3 Kotamobagu • WITA",
              fontSize = 11.sp,
              color = Color.Gray
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            if (onDelete != null) {
              IconButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.size(32.dp).testTag("btn_delete_photo_header")
              ) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus Foto", tint = Color(0xFFEF4444))
              }
              Spacer(modifier = Modifier.width(4.dp))
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image with Watermark (Full uncropped view matching camera frame)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF0F172A),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(12.dp))
        ) {
          AsyncImage(
            model = photoFile,
            contentDescription = "Foto Bukti Berwatermark",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category & Doc Number
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = NavyPrimary
          ) {
            Text(
              text = doc.category.uppercase(Locale("id", "ID")),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          Text(
            text = doc.nomorDokumentasi,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = NavyDark
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Activity Detail
        Text(
          text = doc.activityName,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = Color(0xFF0F172A)
        )
        Text(
          text = doc.komponenBos,
          fontSize = 12.sp,
          color = Color(0xFF475569)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Divider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(10.dp))

        // WITA Time & GPS
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Schedule, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = doc.witaFormattedDate, fontSize = 12.sp, color = Color(0xFF1E293B))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Top) {
          Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "${WitaTimeUtil.toDmsFormat(doc.latitude, doc.longitude)} (${String.format(Locale.US, "%.5f°, %.5f°", doc.latitude, doc.longitude)})",
              fontFamily = FontFamily.Monospace,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF1E293B)
            )
            Text(text = "📍 ${doc.addressText}", fontSize = 11.sp, color = Color(0xFF475569))
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SHA-256 Checksum Card
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFF1F5F9),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "SHA-256 HASH KEABSAHAN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessDark
              )
              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("SHA-256", doc.sha256Hash)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "Hash disalin ke clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Salin Hash",
                  tint = NavyPrimary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
            Text(
              text = doc.sha256Hash,
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              color = Color(0xFF0F172A),
              lineHeight = 13.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              if (photoFile.exists()) {
                val uri = FileProvider.getUriForFile(
                  context,
                  "${context.packageName}.fileprovider",
                  photoFile
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "image/jpeg"
                  putExtra(Intent.EXTRA_STREAM, uri)
                  putExtra(Intent.EXTRA_SUBJECT, "Bukti Foto BOS ${doc.nomorDokumentasi}")
                  putExtra(Intent.EXTRA_TEXT, "Bukti Foto BOS SMKN 3 Kotamobagu\nKegiatan: ${doc.activityName}\nKategori: ${doc.category}\nSHA-256: ${doc.sha256Hash}")
                  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Bagikan Bukti Foto"))
              }
            },
            modifier = Modifier
              .weight(1f)
              .height(46.dp),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Bagikan", fontSize = 13.sp)
          }

          if (onDelete != null) {
            OutlinedButton(
              onClick = { showDeleteConfirmDialog = true },
              modifier = Modifier
                .weight(1f)
                .height(46.dp),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626))
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Hapus", fontSize = 13.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
            }
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .height(46.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
          ) {
            Text("Tutup", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    if (showDeleteConfirmDialog && onDelete != null) {
      AlertDialog(
        onDismissRequest = { showDeleteConfirmDialog = false },
        title = { Text("Hapus Bukti Foto?", fontWeight = FontWeight.Bold) },
        text = {
          Text("Foto bukti ${doc.nomorDokumentasi} (${doc.category}) akan dihapus secara permanen dari penyimpanan perangkat dan basis data.")
        },
        confirmButton = {
          Button(
            onClick = {
              showDeleteConfirmDialog = false
              onDelete.invoke()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
          ) {
            Text("Hapus Permanen", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showDeleteConfirmDialog = false }) {
            Text("Batal")
          }
        }
      )
    }
  }
}
