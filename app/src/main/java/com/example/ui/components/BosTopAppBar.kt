package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BosTopAppBar(
  userSession: UserSession,
  queuedCount: Int,
  isSyncing: Boolean,
  onRoleClick: () -> Unit,
  onSyncClick: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = NavyDark,
      titleContentColor = Color.White
    ),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        // School Crest
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            painter = painterResource(id = R.drawable.ic_smkn3_logo),
            contentDescription = "Logo SMKN 3 Kotamobagu",
            tint = Color.Unspecified,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "BOS Cam",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
              ),
              color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = GoldAccent
            ) {
              Text(
                text = "WITA",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              )
            }
          }
          Text(
            text = "SMK Negeri 3 Kotamobagu",
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              color = Color(0xFFCBD5E1)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    },
    actions = {
      // Role Switcher Pill
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = when (userSession.role) {
          UserRole.ADMIN -> Color(0xFF1E3A8A)
          UserRole.PETUGAS -> Color(0xFF0369A1)
          UserRole.PIMPINAN -> Color(0xFFB45309)
        },
        modifier = Modifier
          .padding(end = 4.dp)
          .clip(RoundedCornerShape(16.dp))
          .clickable { onRoleClick() }
          .testTag("role_badge_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(GoldAccent)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = userSession.role.label.split(" ").first(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }
      }

      // Sync Cloud Button with badge
      IconButton(
        onClick = onSyncClick,
        modifier = Modifier.testTag("sync_button")
      ) {
        if (queuedCount > 0) {
          BadgedBox(
            badge = {
              Badge(
                containerColor = GoldAccent,
                contentColor = Color.Black
              ) {
                Text(
                  text = "$queuedCount",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          ) {
            Icon(
              imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudQueue,
              contentDescription = "Sinkronisasi Antrean",
              tint = GoldAccent,
              modifier = if (isSyncing) Modifier.rotate(rotation) else Modifier
            )
          }
        } else {
          Icon(
            imageVector = if (isSyncing) Icons.Default.Sync else Icons.Default.CloudDone,
            contentDescription = "Sinkronisasi Selesai",
            tint = SuccessGreen,
            modifier = if (isSyncing) Modifier.rotate(rotation) else Modifier
          )
        }
      }
    }
  )
}
