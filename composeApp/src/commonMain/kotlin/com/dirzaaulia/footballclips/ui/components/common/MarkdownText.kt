package com.dirzaaulia.footballclips.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> MarkdownHeading(block)
                is MarkdownBlock.BulletItem -> MarkdownBulletItem(block, color)
                is MarkdownBlock.Paragraph -> MarkdownParagraph(block, color)
                is MarkdownBlock.Divider -> Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun MarkdownHeading(block: MarkdownBlock.Heading) {
    val typography = when (block.level) {
        1 -> MaterialTheme.typography.titleLarge
        2 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }
    Text(
        text = block.text,
        style = typography,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun MarkdownBulletItem(
    block: MarkdownBlock.BulletItem,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = parseInlineSpans(block.text),
            style = MaterialTheme.typography.bodySmall,
            color = color,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun MarkdownParagraph(
    block: MarkdownBlock.Paragraph,
    color: Color,
) {
    val isCitation = block.text.startsWith("*Source:") || block.text.startsWith("_Source:")
    Text(
        text = parseInlineSpans(block.text),
        style = if (isCitation) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
        fontStyle = if (isCitation) FontStyle.Italic else FontStyle.Normal,
        color = if (isCitation) color.copy(alpha = 0.8f) else color,
        lineHeight = 19.sp,
    )
}

private sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data object Divider : MarkdownBlock
}

private fun parseMarkdownBlocks(raw: String): List<MarkdownBlock> {
    val lines = raw.lines()
    val blocks = mutableListOf<MarkdownBlock>()

    for (line in lines) {
        val trimmed = line.trim()
        when {
            trimmed.isEmpty() -> {
                if (blocks.lastOrNull() !is MarkdownBlock.Divider) {
                    blocks.add(MarkdownBlock.Divider)
                }
            }
            trimmed.startsWith("### ") -> blocks.add(MarkdownBlock.Heading(3, trimmed.removePrefix("### ").trim()))
            trimmed.startsWith("## ") -> blocks.add(MarkdownBlock.Heading(2, trimmed.removePrefix("## ").trim()))
            trimmed.startsWith("# ") -> blocks.add(MarkdownBlock.Heading(1, trimmed.removePrefix("# ").trim()))
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                val bulletContent = trimmed.substring(2).trim()
                blocks.add(MarkdownBlock.BulletItem(bulletContent))
            }
            else -> blocks.add(MarkdownBlock.Paragraph(trimmed))
        }
    }
    return blocks
}

private fun parseInlineSpans(text: String): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    val regex = Regex("""\*\*(.+?)\*\*|\*(.+?)\*""")
    val matches = regex.findAll(text)

    for (match in matches) {
        if (match.range.first > cursor) {
            append(text.substring(cursor, match.range.first))
        }
        val boldGroup = match.groups[1]
        val italicGroup = match.groups[2]
        if (boldGroup != null) {
            val start = length
            append(boldGroup.value)
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
        } else if (italicGroup != null) {
            val start = length
            append(italicGroup.value)
            addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) {
        append(text.substring(cursor))
    }
}
