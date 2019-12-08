/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command;

import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.AsyncCommandBuilder;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.WorldEditAsyncCommandBuilder;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.component.CodeFormat;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.FilenameException;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.enginehub.piston.exception.StopExecutionException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Commands that work with schematic files.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SchematicCommands {

    private static final Logger log = LoggerFactory.getLogger(SchematicCommands.class);
    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public SchematicCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        name = "load",
        desc = "Load a schematic into your clipboard"
    )
    @CommandPermissions({"worldedit.clipboard.load", "worldedit.schematic.load"})
    public void load(Actor actor, LocalSession session,
                     @Arg(desc = "File name.")
                         String filename,
                     @Arg(desc = "Format name.", def = "sponge")
                         String formatName) throws FilenameException {
        LocalConfiguration config = worldEdit.getConfiguration();

        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);
        File f = worldEdit.getSafeOpenFile(actor, dir, filename,
                BuiltInClipboardFormat.SPONGE_SCHEMATIC.getPrimaryFileExtension(),
                ClipboardFormats.getFileExtensionArray());

        if (!f.exists()) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.load.does-not-exist", TextComponent.of(filename)));
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(f);
        if (format == null) {
            format = ClipboardFormats.findByAlias(formatName);
        }
        if (format == null) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)));
            return;
        }

        SchematicLoadTask task = new SchematicLoadTask(actor, f, format);
        AsyncCommandBuilder.wrap(task, actor)
                .registerWithSupervisor(worldEdit.getSupervisor(), "Loading schematic " + filename)
                .sendMessageAfterDelay(TranslatableComponent.of("worldedit.schematic.load.loading"))
                .onSuccess(TextComponent.of(filename, TextColor.GOLD)
                                .append(TextComponent.of(" loaded. Paste it with ", TextColor.LIGHT_PURPLE))
                                .append(CodeFormat.wrap("//paste").clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, "//paste"))),
                        session::setClipboard)
                .onFailure("Failed to load schematic", worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter())
                .buildAndExec(worldEdit.getExecutorService());
    }

    @Command(
        name = "save",
        desc = "Save a schematic into your clipboard"
    )
    @CommandPermissions({"worldedit.clipboard.save", "worldedit.schematic.save"})
    public void save(Actor actor, LocalSession session,
                     @Arg(desc = "File name.")
                         String filename,
                     @Arg(desc = "Format name.", def = "sponge")
                         String formatName,
                     @Switch(name = 'f', desc = "Overwrite an existing file.")
                         boolean allowOverwrite
        ) throws WorldEditException {
        LocalConfiguration config = worldEdit.getConfiguration();

        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);

        ClipboardFormat format = ClipboardFormats.findByAlias(formatName);
        if (format == null) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)));
            return;
        }

        File f = worldEdit.getSafeSaveFile(actor, dir, filename, format.getPrimaryFileExtension());

        boolean overwrite = f.exists();
        if (overwrite) {
            if (!actor.hasPermission("worldedit.schematic.delete")) {
                throw new StopExecutionException(TextComponent.of("That schematic already exists!"));
            }
            if (!allowOverwrite) {
                actor.printError(TranslatableComponent.of("worldedit.schematic.save.already-exists"));
                return;
            }
        }

        // Create parent directories
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new StopExecutionException(TranslatableComponent.of(
                        "worldedit.schematic.save.failed-directory"));
            }
        }

        ClipboardHolder holder = session.getClipboard();

        SchematicSaveTask task = new SchematicSaveTask(actor, f, format, holder, overwrite);
        AsyncCommandBuilder.wrap(task, actor)
                .registerWithSupervisor(worldEdit.getSupervisor(), "Saving schematic " + filename)
                .sendMessageAfterDelay(TranslatableComponent.of("worldedit.schematic.save.saving"))
                .onSuccess(filename + " saved" + (overwrite ? " (overwriting previous file)." : "."), null)
                .onFailure("Failed to load schematic", worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter())
                .buildAndExec(worldEdit.getExecutorService());
    }

    @Command(
        name = "delete",
        aliases = {"d"},
        desc = "Delete a saved schematic"
    )
    @CommandPermissions("worldedit.schematic.delete")
    public void delete(Actor actor,
                       @Arg(desc = "File name.")
                           String filename) throws WorldEditException {
        LocalConfiguration config = worldEdit.getConfiguration();
        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);

        File f = worldEdit.getSafeOpenFile(actor instanceof Player ? ((Player) actor) : null,
                dir, filename, "schematic", ClipboardFormats.getFileExtensionArray());

        if (!f.exists()) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.delete.does-not-exist", TextComponent.of(filename)));
            return;
        }

        if (!f.delete()) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.delete.failed", TextComponent.of(filename)));
            return;
        }

        actor.printInfo(TranslatableComponent.of("worldedit.schematic.delete.deleted", TextComponent.of(filename)));
        try {
            log.info(actor.getName() + " deleted " + f.getCanonicalPath());
        } catch (IOException e) {
            log.info(actor.getName() + " deleted " + f.getAbsolutePath());
        }
    }

    @Command(
        name = "formats",
        aliases = {"listformats", "f"},
        desc = "List available formats"
    )
    @CommandPermissions("worldedit.schematic.formats")
    public void formats(Actor actor) {
        actor.printInfo(TranslatableComponent.of("worldedit.schematic.formats.title"));
        StringBuilder builder;
        boolean first = true;
        for (ClipboardFormat format : ClipboardFormats.getAll()) {
            builder = new StringBuilder();
            builder.append(format.getName()).append(": ");
            for (String lookupName : format.getAliases()) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(lookupName);
                first = false;
            }
            first = true;
            actor.printInfo(TextComponent.of(builder.toString()));
        }
    }

    @Command(
        name = "list",
        aliases = {"all", "ls"},
        desc = "List saved schematics",
        descFooter = "Note: Format is not fully verified until loading."
    )
    @CommandPermissions("worldedit.schematic.list")
    public void list(Actor actor,
                     @ArgFlag(name = 'p', desc = "Page to view.", def = "1")
                         int page,
                     @Switch(name = 'd', desc = "Sort by date, oldest first")
                         boolean oldFirst,
                     @Switch(name = 'n', desc = "Sort by date, newest first")
                         boolean newFirst) {
        if (oldFirst && newFirst) {
            throw new StopExecutionException(TextComponent.of("Cannot sort by oldest and newest."));
        }
        final String saveDir = worldEdit.getConfiguration().saveDir;
        final int sortType = oldFirst ? -1 : newFirst ? 1 : 0;
        final String pageCommand = actor.isPlayer()
                ? "//schem list -p %page%" + (sortType == -1 ? " -d" : sortType == 1 ? " -n" : "") : null;

        WorldEditAsyncCommandBuilder.createAndSendMessage(actor,
                new SchematicListTask(saveDir, sortType, page, pageCommand), "(Please wait... gathering schematic list.)");
    }

    private static class SchematicLoadTask implements Callable<ClipboardHolder> {
        private final Actor actor;
        private final File file;
        private final ClipboardFormat format;

        SchematicLoadTask(Actor actor, File file, ClipboardFormat format) {
            this.actor = actor;
            this.file = file;
            this.format = format;
        }

        @Override
        public ClipboardHolder call() throws Exception {
            try (Closer closer = Closer.create()) {
                FileInputStream fis = closer.register(new FileInputStream(file));
                BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
                ClipboardReader reader = closer.register(format.getReader(bis));

                Clipboard clipboard = reader.read();
                log.info(actor.getName() + " loaded " + file.getCanonicalPath());
                return new ClipboardHolder(clipboard);
            }
        }
    }

    private static class SchematicSaveTask implements Callable<Void> {
        private final Actor actor;
        private final File file;
        private final ClipboardFormat format;
        private final ClipboardHolder holder;
        private final boolean overwrite;

        SchematicSaveTask(Actor actor, File file, ClipboardFormat format, ClipboardHolder holder, boolean overwrite) {
            this.actor = actor;
            this.file = file;
            this.format = format;
            this.holder = holder;
            this.overwrite = overwrite;
        }

        @Override
        public Void call() throws Exception {
            Clipboard clipboard = holder.getClipboard();
            Transform transform = holder.getTransform();
            Clipboard target;

            // If we have a transform, bake it into the copy
            if (transform.isIdentity()) {
                target = clipboard;
            } else {
                FlattenedClipboardTransform result = FlattenedClipboardTransform.transform(clipboard, transform);
                target = new BlockArrayClipboard(result.getTransformedRegion());
                target.setOrigin(clipboard.getOrigin());
                Operations.completeLegacy(result.copyTo(target));
            }

            try (Closer closer = Closer.create()) {
                FileOutputStream fos = closer.register(new FileOutputStream(file));
                BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
                ClipboardWriter writer = closer.register(format.getWriter(bos));
                writer.write(target);

                log.info(actor.getName() + " saved " + file.getCanonicalPath() + (overwrite ? " (overwriting previous file)" : ""));
            }
            return null;
        }
    }

    private static class SchematicListTask implements Callable<Component> {
        private final String prefix;
        private final int sortType;
        private final int page;
        private final File rootDir;
        private final String pageCommand;

        SchematicListTask(String prefix, int sortType, int page, String pageCommand) {
            this.prefix = prefix;
            this.sortType = sortType;
            this.page = page;
            this.rootDir = WorldEdit.getInstance().getWorkingDirectoryFile(prefix);
            this.pageCommand = pageCommand;
        }

        @Override
        public Component call() throws Exception {
            List<File> fileList = allFiles(rootDir);

            if (fileList == null || fileList.isEmpty()) {
                return ErrorFormat.wrap("No schematics found.");
            }

            File[] files = new File[fileList.size()];
            fileList.toArray(files);
            // cleanup file list
            Arrays.sort(files, (f1, f2) -> {
                // http://stackoverflow.com/questions/203030/best-way-to-list-files-in-java-sorted-by-date-modified
                int res;
                if (sortType == 0) { // use name by default
                    int p = f1.getParent().compareTo(f2.getParent());
                    if (p == 0) { // same parent, compare names
                        res = f1.getName().compareTo(f2.getName());
                    } else { // different parent, sort by that
                        res = p;
                    }
                } else {
                    res = Long.compare(f1.lastModified(), f2.lastModified()); // use date if there is a flag
                    if (sortType == 1) res = -res; // flip date for newest first instead of oldest first
                }
                return res;
            });

            PaginationBox paginationBox = new SchematicPaginationBox(prefix, files, pageCommand);
            return paginationBox.create(page);
        }
    }

    private static List<File> allFiles(File root) {
        File[] files = root.listFiles();
        if (files == null) return null;
        List<File> fileList = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory()) {
                List<File> subFiles = allFiles(f);
                if (subFiles == null) continue; // empty subdir
                fileList.addAll(subFiles);
            } else {
                fileList.add(f);
            }
        }
        return fileList;
    }

    private static class SchematicPaginationBox extends PaginationBox {
        private final String prefix;
        private final File[] files;

        SchematicPaginationBox(String rootDir, File[] files, String pageCommand) {
            super("Available schematics", pageCommand);
            this.prefix = rootDir == null ? "" : rootDir;
            this.files = files;
        }

        @Override
        public Component getComponent(int number) {
            checkArgument(number < files.length && number >= 0);
            File file = files[number];
            Multimap<String, ClipboardFormat> exts = ClipboardFormats.getFileExtensionMap();
            String format = exts.get(Files.getFileExtension(file.getName()))
                    .stream().findFirst().map(ClipboardFormat::getName).orElse("Unknown");
            boolean inRoot = file.getParentFile().getName().equals(prefix);

            String path = inRoot ? file.getName() : file.getPath().split(Pattern.quote(prefix + File.separator))[1];

            return TextComponent.builder()
                    .content("")
                    .append(TextComponent.of("[L]")
                            .color(TextColor.GOLD)
                            .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/schem load \"" + path + "\""))
                            .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to load"))))
                    .append(TextComponent.space())
                    .append(TextComponent.of(path)
                            .color(TextColor.DARK_GREEN)
                            .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(format))))
                    .build();
        }

        @Override
        public int getComponentsSize() {
            return files.length;
        }
    }
}
