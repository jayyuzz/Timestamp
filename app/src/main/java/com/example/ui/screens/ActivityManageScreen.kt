package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BosActivity
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import java.text.NumberFormat
import java.util.Locale

private val BOS_KOMPONEN_LIST = listOf(
  "01. Penerimaan Peserta Didik Baru (PPDB)",
  "02. Pengembangan Perpustakaan dan Pojok Baca",
  "03. Pemeliharaan Sarana dan Prasarana Sekolah",
  "04. Pelaksanaan Kegiatan Pembelajaran & Praktik Kejuruan",
  "05. Pelaksanaan Kegiatan Asesmen & Uji Kompetensi Keahlian (UKK)",
  "06. Langganan Daya dan Jasa Sekolah",
  "07. Pembayaran Honorarium Guru & Tenaga Kependidikan",
  "08. Pengembangan Profesi Pendidik & Tenaga Kependidikan",
  "09. Pembiayaan Langganan Lisensi Perangkat Lunak Praktik"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityManageScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit
) {
  val activities by viewModel.activities.collectAsState()
  val userSession by viewModel.userSession.collectAsState()

  var showAddDialog by remember { mutableStateOf(false) }
  var activityToDelete by remember { mutableStateOf<BosActivity?>(null) }

  Scaffold(
    topBar = {
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
            modifier = Modifier.testTag("btn_back_activities")
          ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
          }

          Spacer(modifier = Modifier.width(4.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Manajemen Kegiatan BOS",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = Color.White
            )
            Text(
              text = "Tahun Anggaran 2026 • SMK Negeri 3 Kotamobagu",
              fontSize = 11.sp,
              color = GoldAccent
            )
          }
        }
      }
    },
    floatingActionButton = {
      if (userSession.role == UserRole.ADMIN) {
        FloatingActionButton(
          onClick = { showAddDialog = true },
          containerColor = GoldAccent,
          contentColor = Color.Black,
          modifier = Modifier.testTag("fab_add_activity")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Tambah Kegiatan")
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF8FAFC))
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      item {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = NavyPrimary.copy(alpha = 0.08f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = null,
              tint = NavyPrimary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Total ${activities.size} Kegiatan Terdaftar",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NavyDark
              )
              Text(
                text = if (userSession.role == UserRole.ADMIN) "Kelola anggaran dan komponen belanja sekolah" else "Hanya Admin yang dapat menambah/menghapus kegiatan",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
            }
          }
        }
      }

      items(activities, key = { it.id }) { act ->
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.White),
          shape = RoundedCornerShape(12.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
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
                  text = act.kodeKegiatan,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF334155),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }

              if (userSession.role == UserRole.ADMIN) {
                IconButton(
                  onClick = { activityToDelete = act },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Kegiatan",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = act.namaKegiatan,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF0F172A)
            )

            Text(
              text = act.komponenBos,
              fontSize = 12.sp,
              color = Color(0xFF475569)
            )

            if (act.deskripsi.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = act.deskripsi,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              val rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(act.alokasiDana)
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = rupiah,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = NavyPrimary
                )
              }

              Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFECFDF5)
              ) {
                Text(
                  text = act.status,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF047857),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }

    // Add Activity Dialog
    if (showAddDialog) {
      AddActivityDialog(
        onDismiss = { showAddDialog = false },
        onSave = { kode, nama, komp, alokasi, desc ->
          viewModel.addNewActivity(kode, nama, komp, alokasi, desc)
          showAddDialog = false
        }
      )
    }

    // Delete Confirmation
    activityToDelete?.let { act ->
      AlertDialog(
        onDismissRequest = { activityToDelete = null },
        title = { Text("Hapus Kegiatan BOS?") },
        text = { Text("Kegiatan '${act.namaKegiatan}' (${act.kodeKegiatan}) akan dihapus dari sistem.") },
        confirmButton = {
          TextButton(
            onClick = {
              viewModel.deleteActivity(act)
              activityToDelete = null
            }
          ) {
            Text("Hapus", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { activityToDelete = null }) {
            Text("Batal")
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddActivityDialog(
  onDismiss: () -> Unit,
  onSave: (kode: String, nama: String, komp: String, alokasi: Long, desc: String) -> Unit
) {
  var kode by remember { mutableStateOf("BOS-SMKN3-2026-00" + (1..9).random()) }
  var nama by remember { mutableStateOf("") }
  var selectedKomponen by remember { mutableStateOf(BOS_KOMPONEN_LIST[2]) }
  var alokasiStr by remember { mutableStateOf("15000000") }
  var deskripsi by remember { mutableStateOf("") }
  var dropdownExpanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = "Tambah Kegiatan BOS",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = NavyDark
        )
        Text(
          text = "Alokasi anggaran & komponen SMK Negeri 3 Kotamobagu",
          fontSize = 11.sp,
          color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = kode,
          onValueChange = { kode = it },
          label = { Text("Kode Kegiatan") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = nama,
          onValueChange = { nama = it },
          label = { Text("Nama Kegiatan / Belanja") },
          placeholder = { Text("cth: Pengadaan Kabel & Switch Jaringan") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Komponen BOS Dropdown
        ExposedDropdownMenuBox(
          expanded = dropdownExpanded,
          onExpandedChange = { dropdownExpanded = it }
        ) {
          OutlinedTextField(
            value = selectedKomponen,
            onValueChange = {},
            readOnly = true,
            label = { Text("Komponen BOS") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )

          ExposedDropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false }
          ) {
            BOS_KOMPONEN_LIST.forEach { item ->
              DropdownMenuItem(
                text = { Text(item, fontSize = 12.sp) },
                onClick = {
                  selectedKomponen = item
                  dropdownExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = alokasiStr,
          onValueChange = { alokasiStr = it.filter { ch -> ch.isDigit() } },
          label = { Text("Alokasi Dana (Rp)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = deskripsi,
          onValueChange = { deskripsi = it },
          label = { Text("Catatan / Sasaran Kegiatan") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(onClick = onDismiss) {
            Text("Batal")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (nama.isNotBlank()) {
                val alokasi = alokasiStr.toLongOrNull() ?: 0L
                onSave(kode, nama, selectedKomponen, alokasi, deskripsi)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Simpan Kegiatan")
          }
        }
      }
    }
  }
}
