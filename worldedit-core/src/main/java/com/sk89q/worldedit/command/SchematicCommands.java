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

package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.google.common.io.MoreFiles;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.AsyncCommandBuilder;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.WorldEditAsyncCommandBuilder;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
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
import com.sk89q.worldedit.util.formatting.component.SubtleFormat;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.util.io.file.MorePaths;
import com.sk89q.worldedit.util.paste.EngineHubPaste;
import com.sk89q.worldedit.util.paste.PasteMetadata;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.StopExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

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

        File dir = worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
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
                .setDelayMessage(TranslatableComponent.of("worldedit.schematic.load.loading"))
                .setWorkingMessage(TranslatableComponent.of("worldedit.schematic.load.still-loading"))
                .onSuccess(TextComponent.of(filename, TextColor.GOLD)
                                .append(TextComponent.of(" loaded. Paste it with ", TextColor.LIGHT_PURPLE))
                                .append(CodeFormat.wrap("//paste").clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, "//paste"))),
                        session::setClipboard)
                .onFailure("Failed to load schematic", worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter())
                .buildAndExec(worldEdit.getExecutorService());
    }

    @Command(
        name = "save",
        desc = "Save your clipboard into a schematic file"
    )
    @CommandPermissions({ "worldedit.clipboard.save", "worldedit.schematic.save" })
    public void save(Actor actor, LocalSession session,
                     @Arg(desc = "File name.")
                         String filename,
                     @Arg(desc = "Format name.", def = "sponge")
                         String formatName,
                     @Switch(name = 'f', desc = "Overwrite an existing file.")
                         boolean allowOverwrite) throws WorldEditException {
        if (worldEdit.getPlatformManager().queryCapability(Capability.GAME_HOOKS).getDataVersion() == -1) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.unsupported-minecraft-version"));
            return;
        }

        LocalConfiguration config = worldEdit.getConfiguration();

        File dir = worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();

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
                .setDelayMessage(TranslatableComponent.of("worldedit.schematic.save.saving"))
                .setWorkingMessage(TranslatableComponent.of("worldedit.schematic.save.still-saving"))
                .onSuccess(filename + " saved" + (overwrite ? " (overwriting previous file)." : "."), null)
                .onFailure("Failed to save schematic", worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter())
                .buildAndExec(worldEdit.getExecutorService());
    }

    @Command(
        name = "share",
        desc = "Share your clipboard as a schematic online"
    )
    @CommandPermissions({ "worldedit.clipboard.share", "worldedit.schematic.share" })
    public void share(Actor actor, LocalSession session,
                      @Arg(desc = "Schematic name. Defaults to name-millis", def = "")
                          String schematicName,
                      @Arg(desc = "Format name.", def = "sponge")
                          String formatName) throws WorldEditException {
        if (worldEdit.getPlatformManager().queryCapability(Capability.GAME_HOOKS).getDataVersion() == -1) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.unsupported-minecraft-version"));
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByAlias(formatName);
        if (format == null) {
            actor.printError(TranslatableComponent.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)));
            return;
        }

        ClipboardHolder holder = session.getClipboard();

        SchematicShareTask task = new SchematicShareTask(actor, format, holder, schematicName);
        AsyncCommandBuilder.wrap(task, actor)
            .registerWithSupervisor(worldEdit.getSupervisor(), "Sharing schematic")
            .setDelayMessage(TranslatableComponent.of("worldedit.schematic.save.saving"))
            .setWorkingMessage(TranslatableComponent.of("worldedit.schematic.save.still-saving"))
            .onSuccess("Shared", (url -> actor.printInfo(TextComponent.of(url.toExternalForm() + ".schem").clickEvent(ClickEvent.openUrl(url.toExternalForm() + ".schem")))))
            .onFailure("Failed to share schematic", worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter())
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
        File dir = worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();

        File f = worldEdit.getSafeOpenFile(actor,
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
        Comparator<Path> pathComparator;
        String flag;
        if (oldFirst) {
            pathComparator = MorePaths.oldestFirst();
            flag = " -d";
        } else if (newFirst) {
            pathComparator = MorePaths.newestFirst();
            flag = " -n";
        } else {
            pathComparator = Comparator.naturalOrder();
            flag = "";
        }
        final String pageCommand = actor.isPlayer()
                ? "//schem list -p %page%" + flag : null;

        WorldEditAsyncCommandBuilder.createAndSendMessage(actor,
                new SchematicListTask(saveDir, pathComparator, page, pageCommand),
                SubtleFormat.wrap("(Please wait... gathering schematic list.)"));
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

    private abstract static class SchematicOutputTask<T> implements Callable<T> {
        protected final Actor actor;
        private final ClipboardFormat format;
        private final ClipboardHolder holder;

        SchematicOutputTask(Actor actor, ClipboardFormat format, ClipboardHolder holder) {
            this.actor = actor;
            this.format = format;
            this.holder = holder;
        }

        protected void writeToOutputStream(OutputStream outputStream) throws Exception {
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
                OutputStream stream = closer.register(outputStream);
                BufferedOutputStream bos = closer.register(new BufferedOutputStream(stream));
                ClipboardWriter writer = closer.register(format.getWriter(bos));
                writer.write(target);
            }
        }
    }

    private static class SchematicSaveTask extends SchematicOutputTask<Void> {
        private final File file;
        private final boolean overwrite;

        SchematicSaveTask(Actor actor, File file, ClipboardFormat format, ClipboardHolder holder, boolean overwrite) {
            super(actor, format, holder);
            this.file = file;
            this.overwrite = overwrite;
        }

        @Override
        public Void call() throws Exception {
            try {
                writeToOutputStream(new FileOutputStream(file));
                log.info(actor.getName() + " saved " + file.getCanonicalPath() + (overwrite ? " (overwriting previous file)" : ""));
            } catch (IOException e) {
                file.delete();
                throw new CommandException(TextComponent.of(e.getMessage()), e, ImmutableList.of());
            }
            return null;
        }
    }

    private static class SchematicShareTask extends SchematicOutputTask<URL> {
        private final String name;

        SchematicShareTask(Actor actor, ClipboardFormat format, ClipboardHolder holder, String name) {
            super(actor, format, holder);
            this.name = name;
        }

        @Override
        public URL call() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                writeToOutputStream(baos);
            } catch (Exception e) {
                throw new CommandException(TextComponent.of(e.getMessage()), e, ImmutableList.of());
            }

            EngineHubPaste pasteService = new EngineHubPaste();
            PasteMetadata metadata = new PasteMetadata();
            metadata.author = this.actor.getName();
            metadata.extension = "schem";
            metadata.name = name == null ? actor.getName() + "-" + System.currentTimeMillis() : name;
            return pasteService.paste(new String(Base64.getEncoder().encode(baos.toByteArray()), StandardCharsets.UTF_8), metadata).call();
        }
    }

    private static class SchematicListTask implements Callable<Component> {
        private final Comparator<Path> pathComparator;
        private final int page;
        private final Path rootDir;
        private final String pageCommand;

        SchematicListTask(String prefix, Comparator<Path> pathComparator, int page, String pageCommand) {
            this.pathComparator = pathComparator;
            this.page = page;
            this.rootDir = WorldEdit.getInstance().getWorkingDirectoryPath(prefix);
            this.pageCommand = pageCommand;
        }

        @Override
        public Component call() throws Exception {
            Path resolvedRoot = rootDir.toRealPath();
            List<Path> fileList = allFiles(resolvedRoot);

            if (fileList.isEmpty()) {
                return ErrorFormat.wrap("No schematics found.");
            }

            fileList.sort(pathComparator);

            PaginationBox paginationBox = new SchematicPaginationBox(resolvedRoot, fileList, pageCommand);
            return paginationBox.create(page);
        }
    }

    private static List<Path> allFiles(Path root) throws IOException {
        List<Path> pathList = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    pathList.addAll(allFiles(path));
                } else {
                    pathList.add(path);
                }
            }
        }
        return pathList;
    }

    private static class SchematicPaginationBox extends PaginationBox {
        private final Path rootDir;
        private final List<Path> files;

        SchematicPaginationBox(Path rootDir, List<Path> files, String pageCommand) {
            super("Available schematics", pageCommand);
            this.rootDir = rootDir;
            this.files = files;
        }

        @Override
        public Component getComponent(int number) {
            checkArgument(number < files.size() && number >= 0);
            Path file = files.get(number);

            String format = ClipboardFormats.getFileExtensionMap()
                .get(MoreFiles.getFileExtension(file))
                .stream()
                .findFirst()
                .map(ClipboardFormat::getName)
                .orElse("Unknown");

            boolean inRoot = file.getParent().equals(rootDir);

            String path = inRoot
                    ? file.getFileName().toString()
                    : file.toString().substring(rootDir.toString().length());

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
            return files.size();
        }
    }
}
