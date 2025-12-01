
package com.example.feed_project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.feed_project.domain.model.ExposureEvent
import com.example.feed_project.domain.model.ExposureLog
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposureTestTool(
    exposureLogs: List<ExposureLog>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FeedFlow客户端",
                    style = MaterialTheme.typography.titleLarge
                )

                Row {
                    IconButton(onClick = onClearLogs) {
                        Icon(Icons.Default.Clear, contentDescription = "清除日志")
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (expanded) "收起" else "展开"
                        )
                    }
                }
            }

            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))

                Text(
                    text = "事件总数: ${exposureLogs.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (exposureLogs.isEmpty()) {
                    Text(
                        text = "暂无曝光事件记录",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(exposureLogs.reversed()) { log ->
                            ExposureLogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExposureLogItem(log: ExposureLog) {
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Shanghai") // 关键！
    }

    val timeString = formatter.format(Date(log.timestamp))
    val eventText = when (log.event) {
        ExposureEvent.VISIBLE -> "露出"
        ExposureEvent.VISIBLE_50_PERCENT -> "露出>50%"
        ExposureEvent.FULLY_VISIBLE -> "完整露出"
        ExposureEvent.INVISIBLE -> "消失"
    }

    val eventColor = when (log.event) {
        ExposureEvent.VISIBLE -> MaterialTheme.colorScheme.primaryContainer
        ExposureEvent.VISIBLE_50_PERCENT -> MaterialTheme.colorScheme.secondaryContainer
        ExposureEvent.FULLY_VISIBLE -> MaterialTheme.colorScheme.tertiaryContainer
        ExposureEvent.INVISIBLE -> MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = eventColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${log.itemId}")
            Text(text = "$timeString | $eventText")
        }
    }
}
