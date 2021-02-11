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

import com.google.common.base.Strings
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.command.BiomeCommands
import com.sk89q.worldedit.command.ChunkCommands
import com.sk89q.worldedit.command.ClipboardCommands
import com.sk89q.worldedit.command.GeneralCommands
import com.sk89q.worldedit.command.GenerationCommands
import com.sk89q.worldedit.command.HistoryCommands
import com.sk89q.worldedit.command.NavigationCommands
import com.sk89q.worldedit.command.RegionCommands
import com.sk89q.worldedit.command.ScriptingCommands
import com.sk89q.worldedit.command.SelectionCommands
import com.sk89q.worldedit.command.SnapshotUtilCommands
import com.sk89q.worldedit.command.ToolCommands
import com.sk89q.worldedit.command.ToolUtilCommands
import com.sk89q.worldedit.command.UtilityCommands
import com.sk89q.worldedit.command.util.PermissionCondition
import com.sk89q.worldedit.internal.command.CommandUtil
import com.sk89q.worldedit.util.formatting.text.TextComponent
import org.enginehub.piston.Command
import org.enginehub.piston.config.TextConfig
import org.enginehub.piston.part.SubCommandPart
import org.enginehub.piston.util.HelpGenerator
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.streams.toList

class DocumentationPrinter private constructor() {

    private val nameRegex = Regex("name = \"(.+?)\"")
    private val commands = WorldEdit.getInstance().platformManager.platformCommandManager.commandManager.allCommands
            .map { it.name to it }.toList().toMap()
    private val cmdOutput = StringBuilder()
    private val permsOutput = StringBuilder()
    private val matchedCommands = mutableSetOf<String>()

    private inline fun <reified T> findCommandsIn(): Sequence<String> {
        return sequence {
            val sourceFile = Paths.get("worldedit-core/src/main/java/" + T::class.qualifiedName!!.replace('.', '/') + ".java")
            require(Files.exists(sourceFile)) {
                "Source not found for ${T::class.qualifiedName}, looked at ${sourceFile.toAbsolutePath()}"
            }
            Files.newBufferedReader(sourceFile).useLines { lines ->
                var inCommand = false
                for (line in lines) {
                    if (inCommand) {
                        when (val match = nameRegex.find(line)) {
                            null -> if (line.trim() == ")") inCommand = false
                            else -> yield(match.groupValues[1])
                        }
                    } else if (line.contains("@Command(")) {
                        inCommand = true
                    }
                }
            }
        }
    }

    private fun writeAllCommands() {
        writeHeader()

        dumpSection("General Commands") {
            yield("worldedit")
            yieldAll(findCommandsIn<HistoryCommands>())
            yieldAll(findCommandsIn<GeneralCommands>())
        }

        dumpSection("Navigation Commands") {
            yieldAll(findCommandsIn<NavigationCommands>())
        }

        dumpSection("Selection Commands") {
            yieldAll(findCommandsIn<SelectionCommands>())
            yield("/expand")
        }

        dumpSection("Region Commands") {
            yieldAll(findCommandsIn<RegionCommands>())
        }

        dumpSection("Generation Commands") {
            yieldAll(findCommandsIn<GenerationCommands>())
        }

        dumpSection("Schematic and Clipboard Commands") {
            yield("schematic")
            yieldAll(findCommandsIn<ClipboardCommands>())
        }

        dumpSection("Tool Commands") {
            yield("tool")
            yieldAll(findCommandsIn<ToolCommands>().filter { it != "stacker" })
            yieldAll(findCommandsIn<ToolUtilCommands>())
        }

        dumpSection("Super Pickaxe Commands") {
            yield("superpickaxe")
        }

        dumpSection("Brush Commands") {
            yield("brush")
        }

        dumpSection("Biome Commands") {
            yieldAll(findCommandsIn<BiomeCommands>())
        }

        dumpSection("Chunk Commands") {
            yieldAll(findCommandsIn<ChunkCommands>())
        }

        dumpSection("Snapshot Commands") {
            yieldAll(findCommandsIn<SnapshotUtilCommands>())
            yield("snapshot")
        }

        dumpSection("Scripting Commands") {
            yieldAll(findCommandsIn<ScriptingCommands>())
        }

        dumpSection("Utility Commands") {
            yieldAll(findCommandsIn<UtilityCommands>())
        }

        writeFooter()

        val missingCommands = commands.keys.filterNot { it in matchedCommands }
        require(missingCommands.isEmpty()) { "Missing commands: $missingCommands" }
    }

    private fun writeHeader() {
        cmdOutput.appendLine("""
========
Commands
========

.. contents::
    :local:

.. note::

    Arguments enclosed in ``[ ]`` are optional, those enclosed in ``< >`` are required.

.. tip::

    You can access a command listing in-game via the ``//help`` command.
""".trim())

        permsOutput.appendLine("""
===========
Permissions
===========

By default, no one can use WorldEdit. In order for yourself, moderators, and players to use WorldEdit, you must provide the proper permissions. One way is to provide op to moderators and administrators (unless disabled in the :doc:`configuration <config>`), but providing the permission nodes on this page (through a permissions plugin) is the more flexible.

You can give the ``worldedit.*`` permission to give yourself and other administrators full access to WorldEdit.

Commands
=========

See the :doc:`commands` page for an explanation of some of these commands.

.. csv-table::
  :header: Command, Permission
  :widths: 15, 25
""".trim())
        permsOutput.appendLine()
    }

    private fun writeFooter() {
        permsOutput.appendLine()
        permsOutput.append("""
Other Permissions
==================

.. csv-table::
    :header: Permission, Explanation
    :widths: 15, 25

    ``worldedit.navigation.jumpto.tool``,"Allows usage of the navigation wand's ``/jumpto`` shortcut (left click)."
    ``worldedit.navigation.thru.tool``,"Allows usage of the navigation wand's ``/thru`` shortcut (right click)."
    ``worldedit.anyblock``,"Allows usage of blocks in the :doc:`disallowed-blocks <config>` config option."
    ``worldedit.limit.unrestricted``,"Allows setting the limit via the ``//limit`` :doc:`command <commands>` higher than the maximum in the :doc:`configuration <config>`, as well as other limit bypasses."
    ``worldedit.timeout.unrestricted``,"Allows setting the calculation timeout via the ``//timeout`` :doc:`command <commands>` higher than the maximum in the :doc:`configuration <config>`."
    ``worldedit.inventory.unrestricted``,"Override the ``use-inventory`` option if enabled in the :doc:`configuration <config>`."
    ``worldedit.override.bedrock``,"Allows breaking of bedrock with the super-pickaxe tool."
    ``worldedit.override.data-cycler``,"Allows cycling non-whitelisted blocks with the data cycler tool."
    ``worldedit.setnbt``,"Allows setting `extra data <https://minecraft.gamepedia.com/Block_entity>`_ on blocks (such as signs, chests, etc)."
    ``worldedit.report.pastebin``,"Allows uploading report files to pastebin automatically for the ``/worldedit report`` :doc:`command <commands>`."
    ``worldedit.scripting.execute.<filename>``,"Allows using the CraftScript with the given filename."
""".trim())
    }

    private fun dumpSection(title: String, addCommandNames: suspend SequenceScope<String>.() -> Unit) {
        cmdOutput.append("\n").append(title).append("\n").append(Strings.repeat("~", title.length)).append("\n")

        val prefix = reduceToRst(TextConfig.commandPrefixValue())
        val commands = sequence(addCommandNames).map { this.commands.getValue(it) }.toList()
        matchedCommands.addAll(commands.map { it.name })

        cmdsToPerms(commands, prefix)

        for (command in commands) {
            writeCommandBlock(command, prefix, Stream.empty())
            command.parts.stream().filter { p -> p is SubCommandPart }
                    .flatMap { p -> (p as SubCommandPart).commands.stream() }
                    .forEach { sc ->
                        writeCommandBlock(sc, prefix + command.name + " ", Stream.of(command))
                    }
        }
    }

    private fun cmdsToPerms(cmds: List<Command>, prefix: String) {
        cmds.forEach { c ->
            permsOutput.append("    ").append(cmdToPerm(prefix, c)).append("\n")
            c.parts.filterIsInstance<SubCommandPart>()
                    .forEach { scp ->
                        cmdsToPerms(scp.commands.sortedBy { it.name }, prefix + c.name + " ")
                    }
        }
    }

    private fun cmdToPerm(prefix: String, c: Command): String {
        val cond = c.condition
        val permissions = when {
            cond is PermissionCondition && cond.permissions.isNotEmpty() ->
                cond.permissions.joinToString(", ") { "``$it``" }
            else -> ""
        }
        return "``$prefix${c.name}``,\"$permissions\""
    }

    private fun writeCommandBlock(command: Command, prefix: String, parents: Stream<Command>) {
        val name = prefix + command.name
        val entries = commandTableEntries(command, parents)

        cmdOutput.appendLine(".. raw:: html")
        cmdOutput.appendLine()
        cmdOutput.appendLine("""    <span id="command-${linkSafe(name)}"></span>""")
        cmdOutput.appendLine()
        cmdOutput.append(".. topic:: ``$name``")
        if (!command.aliases.isEmpty()) {
            command.aliases.joinTo(cmdOutput, ", ",
                    prefix = " (or ",
                    postfix = ")",
                    transform = { "``$prefix$it``" })
        }
        cmdOutput.appendLine()
        cmdOutput.appendLine("    :class: command-topic").appendLine()
        CommandUtil.deprecationWarning(command).ifPresent { warning ->
            cmdOutput.appendLine("""
                |    .. WARNING::
                |        ${reduceToRst(warning).makeRstSafe("\n\n")}
            """.trimMargin())
        }
        cmdOutput.appendLine("""
            |    .. csv-table::
            |        :widths: 8, 15
        """.trimMargin())
        cmdOutput.appendLine()
        for ((k, v) in entries) {
            val rstSafe = v.makeRstSafe("\n")
            cmdOutput.append("    ".repeat(2))
                    .append(k)
                    .append(",")
                    .append('"')
                    .append(rstSafe)
                    .append('"').appendLine()
        }
        cmdOutput.appendLine()
    }

    private fun String.makeRstSafe(lineJoiner: String) = trim()
        .replace("\"", "\\\"").replace("\n", "\n" + "    ".repeat(2))
        .lineSequence()
        .map { line -> line.ifBlank { "" } }
        .joinToString(separator = lineJoiner)

    private fun linkSafe(text: String) = text.replace(" ", "-")

    private fun commandTableEntries(command: Command, parents: Stream<Command>): Map<String, String> {
        return sequence {
            val desc = command.description.run {
                val footer = CommandUtil.footerWithoutDeprecation(command)
                when {
                    footer.isPresent -> append(
                            TextComponent.builder("\n\n").append(footer.get())
                    )
                    else -> this
                }
            }
            yield("**Description**" to reduceToRst(desc))
            val cond = command.condition
            if (cond is PermissionCondition && cond.permissions.isNotEmpty()) {
                val perms = cond.permissions.joinToString(", ") { "``$it``" }
                yield("**Permissions**" to perms)
            }
            val usage = reduceToRst(HelpGenerator.create(Stream.concat(parents, Stream.of(command)).toList()).usage)
            yield("**Usage**" to "``$usage``")

            // Part descriptions
            command.parts.filterNot { it is SubCommandPart }
                    .forEach {
                        val title = "\u2001\u2001``" + reduceToRst(it.textRepresentation) + "``"
                        yield(title to reduceToRst(it.description))
                    }
        }.toMap()
    }

    companion object {

        /**
         * Generates documentation.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                WorldEdit.getInstance().platformManager.register(DocumentationPlatform())
                val printer = DocumentationPrinter()

                printer.writeAllCommands()
                writeOutput("commands.rst", printer.cmdOutput.toString())
                writeOutput("permissions.rst", printer.permsOutput.toString())
            } finally {
                WorldEdit.getInstance().sessionManager.unload()
            }
        }

        private fun writeOutput(file: String, output: String) {
            Files.newBufferedWriter(Paths.get(file)).use {
                it.write(output)
            }
        }
    }
}
