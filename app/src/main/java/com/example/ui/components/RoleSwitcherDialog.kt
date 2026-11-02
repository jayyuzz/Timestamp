package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun RoleSwitcherDialog(
  currentSession: UserSession,
  currentSchoolName: String = "SMK NEGERI 3 KOTAMOBAGU",
  onDismiss: () -> Unit,
  onRoleSelected: (UserRole) -> Unit,
  onNameChanged: (String) -> Unit,
  onSchoolNameChanged: (String) -> Unit = {}
) {
  var tempName by remember { mutableStateOf(currentSession.displayName) }
  var tempSchool by remember { mutableStateOf(currentSchoolName) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("role_switcher_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(NavyPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = NavyPrimary,
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Identitas & Peran Pengguna",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = NavyDark
            )
            Text(
              text = "Hak akses & stempel BOS Cam",
              fontSize = 12.sp,
              color = Color.Gray
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Name input
        OutlinedTextField(
          value = tempName,
          onValueChange = {
            tempName = it
            onNameChanged(it)
          },
          label = { Text("Nama Petugas / Pejabat", fontSize = 12.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_officer_name"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyPrimary,
            focusedLabelColor = NavyPrimary
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // School input
        OutlinedTextField(
          value = tempSchool,
          onValueChange = {
            tempSchool = it
            onSchoolNameChanged(it)
          },
          label = { Text("Nama Sekolah / Instansi", fontSize = 12.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_school_name"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyPrimary,
            focusedLabelColor = NavyPrimary
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Peran Operasional:",
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = NavyDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role List
        UserRole.values().forEach { role ->
          val isSelected = currentSession.role == role
          val roleIcon = when (role) {
            UserRole.ADMIN -> Icons.Default.AdminPanelSettings
            UserRole.PETUGAS -> Icons.Default.CameraAlt
            UserRole.PIMPINAN -> Icons.Default.VerifiedUser
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) NavyPrimary.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, NavyPrimary) else null,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                onRoleSelected(role)
              }
              .testTag("role_option_${role.name.lowercase()}")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) NavyPrimary else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = roleIcon,
                  contentDescription = null,
                  tint = if (isSelected) Color.White else Color.Gray,
                  modifier = Modifier.size(18.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = role.label,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (isSelected) NavyPrimary else Color(0xFF1E293B)
                )
                Text(
                  text = role.deskripsi,
                  fontSize = 11.sp,
                  color = Color(0xFF64748B)
                )
              }

              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Terpilih",
                  tint = NavyPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("btn_confirm_role"),
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Selesai & Gunakan Peran", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
