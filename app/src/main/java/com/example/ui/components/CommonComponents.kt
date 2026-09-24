package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClinicCyanAccent
import com.example.ui.theme.ClinicDarkCardBorder
import com.example.ui.theme.ClinicDarkSurface
import com.example.ui.theme.ClinicDarkSurfaceVariant
import com.example.ui.theme.ClinicHeaderTeal
import com.example.ui.theme.ClinicTealPrimary
import com.example.ui.theme.ClinicTextMuted
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.ClinicTextSecondary

@Composable
fun ClinicTopBar(
    title: String = "حسابات ديون المرضى لعيادة الرحمن",
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClinicHeaderTeal)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("clinic_top_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(40.dp).testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "القائمة",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(40.dp).testTag("profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "الملف الشخصي",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun ClinicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ClinicDarkSurface,
    borderColor: Color = ClinicDarkCardBorder,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ClinicInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingText: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    testTag: String = "clinic_input"
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = ClinicTextSecondary
            ),
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = ClinicTextMuted
                    )
                }
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = ClinicCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingIcon = trailingText?.let {
                {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ClinicCyanAccent,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            },
            singleLine = singleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ClinicDarkSurfaceVariant,
                unfocusedContainerColor = ClinicDarkSurfaceVariant,
                focusedBorderColor = ClinicTealPrimary,
                unfocusedBorderColor = ClinicDarkCardBorder,
                focusedTextColor = ClinicTextPrimary,
                unfocusedTextColor = ClinicTextPrimary
            )
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ClinicTextPrimary
        ),
        modifier = modifier.padding(vertical = 4.dp)
    )
}
