/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.util

import com.sk89q.worldedit.util.formatting.WorldEditText
import com.sk89q.worldedit.util.formatting.text.Component
import com.sk89q.worldedit.util.formatting.text.TextComponent
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration
import org.enginehub.piston.util.TextHelper
import java.util.Locale

fun reduceToRst(component: Component): String {
    val formatted = WorldEditText.format(component, Locale.US)
    return formatAsRst(formatted).toString()
}

private fun formatAsRst(component: Component, currentDeco: String? = null): CharSequence {
    return when (component) {
        is TextComponent -> {
            val content = StringBuilder(component.content())

            val deco = when {
                // Actions that suggest themselves as commands are marked as code
                component.isSuggestingAsCommand(content.toString()) -> "``"
                component.decorations().any { it == TextDecoration.BOLD } -> "**"
                component.decorations().any { it == TextDecoration.ITALIC } -> "*"
                else -> null
            }

            component.children().joinTo(content, separator = "") {
                formatAsRst(it, deco ?: currentDeco)
            }

            deco?.let {
                require(currentDeco == null) {
                    "Nested decorations are hell in RST. \n" +
                        "Existing: $currentDeco; New: $deco\n" +
                        "Offender: ${TextHelper.reduceToText(component)}"
                }
                content.rstDeco(deco)
            }

            content
        }
        is TranslatableComponent -> {
            val content = StringBuilder(component.key())
            component.children().joinTo(content, separator = "") { formatAsRst(it, currentDeco) }
            content
        }
        else -> component.children().joinToString(separator = "") { formatAsRst(it, currentDeco) }
    }
}

private fun Component.isSuggestingAsCommand(text: String): Boolean {
    val ce = clickEvent()
    return when (ce?.action()) {
        ClickEvent.Action.RUN_COMMAND,
        ClickEvent.Action.SUGGEST_COMMAND ->
            ce.value() == text
        else -> false
    }
}

private fun StringBuilder.rstDeco(deco: String): StringBuilder = apply {
    ensureCapacity(length + deco.length * 2)
    val insertionPoint = indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
    insert(insertionPoint, deco)
    val insertionPointEnd = (indexOfLast { !it.isWhitespace() } + 1).takeUnless { it < 1 } ?: length
    insert(insertionPointEnd, deco)
}
