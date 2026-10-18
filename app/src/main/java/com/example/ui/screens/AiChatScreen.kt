package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AiChatScreen(
  chatMessages: List<ChatMessage>,
  onSendMessage: (String) -> Unit,
  isLoading: Boolean,
  lastUpdatedText: String,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  val sampleQuestions = listOf(
    "What are today's important Indian stock-market developments?",
    "What new RBI and SEBI notifications have been published?",
    "Summarize latest official announcements for RELIANCE and TCS.",
    "Which sectors are mentioned most frequently in today's verified events?",
    "Explain SEBI options contract size rule in simple terms."
  )

  LaunchedEffect(chatMessages.size) {
    if (chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(chatMessages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // Header & Disclaimer Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(SlateNavySurface)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CyanTeal.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = CyanTeal,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "MarketPulse AI Assistant",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              )
              Text(
                text = "Evidence-Grounded Financial Intelligence",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(GainEmerald)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Live RAG",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = GainEmerald
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SEBI Compliance Disclaimer Banner
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = CyanTeal,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Not SEBI-Registered Investment Advice • Grounded in Official Filings • Data Refreshed: $lastUpdatedText",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              color = TextSecondary
            )
          )
        }
      }
    }

    // Chat Conversation List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item { Spacer(modifier = Modifier.height(8.dp)) }

      items(chatMessages) { message ->
        ChatMessageCard(message = message)
      }

      if (isLoading) {
        item {
          Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = CyanTeal
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Searching official filings and generating grounded answer...",
              style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    // Quick Query Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(sampleQuestions) { question ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable {
              onSendMessage(question)
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = question,
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextSecondary,
              fontSize = 11.sp
            )
          )
        }
      }
    }

    // Input Text Field
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(SlateNavySurface)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = {
            Text(
              text = "Ask MarketPulse AI about corporate filings, SEBI rules...",
              style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
          },
          modifier = Modifier.weight(1f),
          singleLine = true,
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardSurface,
            unfocusedContainerColor = CardSurface,
            focusedBorderColor = CyanTeal,
            unfocusedBorderColor = CardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          keyboardActions = KeyboardActions(onSend = {
            if (inputText.isNotBlank()) {
              onSendMessage(inputText.trim())
              inputText = ""
            }
          })
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (inputText.isNotBlank()) CyanTeal else CardSurface)
            .clickable(enabled = inputText.isNotBlank()) {
              onSendMessage(inputText.trim())
              inputText = ""
            },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send",
            tint = if (inputText.isNotBlank()) Color.Black else TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ChatMessageCard(message: ChatMessage) {
  val isUser = message.sender == "USER"
  val context = LocalContext.current

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Card(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
      ),
      colors = CardDefaults.cardColors(
        containerColor = if (isUser) CyanTeal.copy(alpha = 0.15f) else CardSurface
      ),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .border(
          width = 1.dp,
          color = if (isUser) CyanTeal.copy(alpha = 0.4f) else CardBorder,
          shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
          )
        )
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        if (!isUser) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = CyanTeal,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "MarketPulse Grounded AI",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CyanTeal
              )
            )

            Spacer(modifier = Modifier.weight(1f))

            if (message.isGrounded) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(GainEmerald.copy(alpha = 0.15f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GainEmerald,
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = message.verificationType ?: "OFFICIAL_VERIFIED",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = GainEmerald
                    )
                  )
                }
              }
            }
          }
        }

        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimary,
            lineHeight = 20.sp
          )
        )

        // Cited Sources and URLs
        if (!isUser && message.citedSources.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(SlateNavySurface)
              .padding(8.dp)
          ) {
            Text(
              text = "Cited Sources & Evidence:",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontSize = 10.sp
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            message.citedSources.forEachIndexed { idx, source ->
              val url = message.citedUrls.getOrNull(idx)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 2.dp)
                  .clickable(enabled = !url.isNullOrBlank()) {
                    try {
                      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                      context.startActivity(intent)
                    } catch (_: Exception) {}
                  }
              ) {
                Icon(
                  imageVector = Icons.Default.Link,
                  contentDescription = null,
                  tint = CyanTeal,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = source,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = CyanTeal,
                    fontSize = 10.sp
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = message.formattedTime,
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontSize = 9.sp
          ),
          modifier = Modifier.align(Alignment.End)
        )
      }
    }
  }
}
