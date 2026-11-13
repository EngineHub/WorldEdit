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

package com.sk89q.worldedit.internal.lint;

import com.google.common.base.Splitter;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Project rules from AGENTS.md that can be checked mechanically. Each rule lists its known exceptions
 * (with a count where a place has several); a new violation fails, and so does an exception that no longer
 * applies (remove it from the list).
 */
class ProjectRulesTest {

    private static final String COMMAND = "org.enginehub.piston.annotation.Command";
    private static final String COMMAND_PERMISSIONS = "com.sk89q.worldedit.command.util.CommandPermissions";
    private static final String ARG = "org.enginehub.piston.annotation.param.Arg";

    // Known exceptions; don't add to these without a reason
    private static final Set<String> COMMANDS_WITHOUT_PERMISSION = Set.of(
        "com.sk89q.worldedit.command.ApplyBrushCommands#forest",
        "com.sk89q.worldedit.command.ApplyBrushCommands#set",
        "com.sk89q.worldedit.command.BrushCommands#none",
        "com.sk89q.worldedit.command.ExpandCommands#expand",
        "com.sk89q.worldedit.command.GeneralCommands#togglePlace",
        "com.sk89q.worldedit.command.PaintBrushCommands#forest",
        "com.sk89q.worldedit.command.PaintBrushCommands#set",
        "com.sk89q.worldedit.command.SelectionCommands#select",
        "com.sk89q.worldedit.command.ToolCommands#none",
        "com.sk89q.worldedit.command.WorldEditCommands#cui",
        "com.sk89q.worldedit.command.WorldEditCommands#trace",
        "com.sk89q.worldedit.command.WorldEditCommands#tz",
        "com.sk89q.worldedit.command.WorldEditCommands#version"
    );

    // Known exceptions; don't add to these without a reason
    private static final Set<String> BOOLEAN_ARGUMENTS = Set.of(
        "com.sk89q.worldedit.command.GeneralCommands#drawSelection",
        "com.sk89q.worldedit.command.GeneralCommands#fast",
        "com.sk89q.worldedit.command.ToolUtilCommands#togglePickaxe"
    );

    // Known exceptions; don't add to these without a reason
    private static final Set<String> DIRECT_WORLD_WRITES = Set.of(
        "com.sk89q.worldedit.command.RegionCommands->regenerate",
        "com.sk89q.worldedit.function.block.ApplySideEffect->applySideEffects",
        "com.sk89q.worldedit.function.generator.ForestGenerator->setBlock",
        "com.sk89q.worldedit.function.generator.TreeGenerator->generateTree",
        "com.sk89q.worldedit.util.TreeGenerator->generateTree x2"
    );

    // Known exceptions; don't add to these without a reason
    private static final Set<String> THREAD_CREATORS = Set.of(
        "com.sk89q.worldedit.command.util.FutureProgressListener",
        "com.sk89q.worldedit.internal.util.RecursiveDirectoryWatcher",
        "com.sk89q.worldedit.internal.util.RegionOptimizedVectorSorter",
        "com.sk89q.worldedit.session.SessionManager",
        "com.sk89q.worldedit.util.concurrency.EvenMoreExecutors",
        "com.sk89q.worldedit.util.paste.Pasters"
    );

    // Known exceptions; don't add to these without a reason
    private static final Set<String> BROAD_CATCHES = Set.of(
        "com.sk89q.worldedit.EditSession<Exception x2",
        "com.sk89q.worldedit.EditSession<RuntimeException",
        "com.sk89q.worldedit.LocalConfiguration<Throwable",
        "com.sk89q.worldedit.WorldEdit<Throwable",
        "com.sk89q.worldedit.command.argument.AbstractDirectionConverter<Exception",
        "com.sk89q.worldedit.command.argument.EntityRemoverConverter<Exception",
        "com.sk89q.worldedit.command.argument.SideEffectConverter<Exception",
        "com.sk89q.worldedit.command.argument.SideEffectSetConverter<Exception",
        "com.sk89q.worldedit.command.util.AsyncCommandBuilder<Throwable x2",
        "com.sk89q.worldedit.extension.factory.parser.DefaultBlockParser<Exception",
        "com.sk89q.worldedit.extension.factory.parser.mask.BlockStateMaskParser<Exception",
        "com.sk89q.worldedit.extension.platform.PlatformCommandManager<Throwable x3",
        "com.sk89q.worldedit.extent.buffer.internal.BatchingExtent<RuntimeException",
        "com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat<Exception x3",
        "com.sk89q.worldedit.extent.clipboard.io.MCEditSchematicReader<Exception x2",
        "com.sk89q.worldedit.internal.expression.invoke.ExpressionHandles<Throwable",
        "com.sk89q.worldedit.scripting.RhinoCraftScriptEngine<Error",
        "com.sk89q.worldedit.util.asset.AssetLoader<Exception",
        "com.sk89q.worldedit.util.eventbus.EventHandler<Throwable",
        "com.sk89q.worldedit.util.eventbus.MethodHandleEventHandler<Error",
        "com.sk89q.worldedit.util.eventbus.MethodHandleEventHandler<Exception",
        "com.sk89q.worldedit.util.eventbus.MethodHandleEventHandler<Throwable",
        "com.sk89q.worldedit.util.formatting.text.serializer.gson.StyleSerializer<Exception",
        "com.sk89q.worldedit.util.io.Closer<Throwable x5",
        "com.sk89q.worldedit.util.translation.TranslationManager<Exception",
        "com.sk89q.worldedit.world.registry.LegacyMapper<Throwable",
        "com.sk89q.worldedit.world.snapshot.fs.FileSystemSnapshotDatabase<Throwable"
    );

    private static JavaClasses classes;

    /**
     * Turns occurrence counts into list entries, with {@code " xN"} appended when there are several, so a new
     * occurrence in an already-listed place still fails.
     */
    private static Set<String> counted(Map<String, Integer> counts) {
        Set<String> entries = new TreeSet<>();
        counts.forEach((key, count) -> entries.add(count == 1 ? key : key + " x" + count));
        return entries;
    }

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
            .importPackages("com.sk89q.worldedit");
    }

    private static String name(JavaCodeUnit unit) {
        return unit.getOwner().getName() + "#" + unit.getName();
    }

    private static String outer(JavaClass clazz) {
        JavaClass top = clazz;
        while (top.getEnclosingClass().isPresent()) {
            top = top.getEnclosingClass().get();
        }
        return top.getName();
    }

    @Test
    void commandsHavePermissions() {
        Set<String> found = new TreeSet<>();
        for (JavaClass clazz : classes) {
            for (JavaMethod method : clazz.getMethods()) {
                if (method.isAnnotatedWith(COMMAND) && !method.isAnnotatedWith(COMMAND_PERMISSIONS)) {
                    found.add(name(method));
                }
            }
        }
        assertEquals(COMMANDS_WITHOUT_PERMISSION, found, "@Command methods without @CommandPermissions");
    }

    @Test
    void booleanArgumentsAreSwitches() {
        Set<String> found = new TreeSet<>();
        for (JavaClass clazz : classes) {
            for (JavaMethod method : clazz.getMethods()) {
                for (JavaParameter parameter : method.getParameters()) {
                    if (parameter.isAnnotatedWith(ARG)
                        && (parameter.getRawType().isEquivalentTo(boolean.class)
                            || parameter.getRawType().isEquivalentTo(Boolean.class))) {
                        found.add(name(method));
                    }
                }
            }
        }
        assertEquals(BOOLEAN_ARGUMENTS, found, "boolean @Arg parameters (use @Switch)");
    }

    @Test
    void delegateExtentOverridesEveryExtentMethod() {
        Set<String> found = new TreeSet<>();
        for (Method method : Extent.class.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.isAnnotationPresent(Deprecated.class)) {
                continue;
            }
            try {
                Method impl = AbstractDelegateExtent.class.getMethod(method.getName(), method.getParameterTypes());
                if (impl.getDeclaringClass().isInterface()) {
                    found.add(method.getDeclaringClass().getSimpleName() + "#" + method.getName());
                }
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        }
        assertEquals(Set.of(), found, "Extent methods AbstractDelegateExtent does not forward");
    }

    private static final Set<String> THREAD_TYPES = Set.of(
        "java.lang.Thread", "java.util.Timer",
        "java.util.concurrent.ThreadPoolExecutor", "java.util.concurrent.ScheduledThreadPoolExecutor",
        "java.util.concurrent.ForkJoinPool"
    );

    @Test
    void threadsAreCreatedInKnownPlaces() {
        Map<String, Integer> found = new TreeMap<>();
        for (JavaClass clazz : classes) {
            for (JavaAccess<?> access : clazz.getAccessesFromSelf()) {
                String target = access.getTargetOwner().getName();
                String member = access.getTarget().getName();
                boolean creates = member.equals(JavaConstructor.CONSTRUCTOR_NAME)
                    ? THREAD_TYPES.contains(target)
                    : (target.equals("java.util.concurrent.Executors") && member.startsWith("new"))
                        || (target.equals("java.lang.Thread")
                            && (member.startsWith("of") || member.equals("startVirtualThread")));
                if (creates) {
                    found.merge(outer(clazz), 1, Integer::sum);
                }
            }
        }
        assertEquals(THREAD_CREATORS, counted(found), "classes creating threads/executors");
    }

    private static final Pattern BROAD_CATCH = Pattern.compile(
        "catch\\s*\\(\\s*(?:(?:final|@[\\w.]+(?:\\([^)]*\\))?)\\s+)*((?:[\\w.]+\\s*\\|\\s*)*[\\w.]+)\\s+\\w+\\s*\\)");
    private static final Splitter MULTI_CATCH = Splitter.on('|').trimResults();
    private static final Set<String> BROAD = Set.of("Throwable", "Exception", "RuntimeException", "Error");

    private static final Pattern COMMENTS_AND_STRINGS = Pattern.compile(
        "//[^\\n]*|/\\*.*?\\*/|\"\"\".*?\"\"\"|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])'",
        Pattern.DOTALL
    );

    private static String stripCommentsAndStrings(String source) {
        return COMMENTS_AND_STRINGS.matcher(source).replaceAll(" ");
    }

    @Test
    void noBroadCatches() throws IOException {
        // Checked in source, since the bytecode also has the catch blocks javac generates for try-with-resources
        Path root = Path.of(System.getProperty("worldedit.mainSources", "src/main/java"));
        Map<String, Integer> found = new TreeMap<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                Matcher matcher = BROAD_CATCH.matcher(stripCommentsAndStrings(Files.readString(file)));
                while (matcher.find()) {
                    for (String type : MULTI_CATCH.split(matcher.group(1))) {
                        String simple = type.substring(type.lastIndexOf('.') + 1);
                        if (BROAD.contains(simple)) {
                            String clazz = root.relativize(file).toString().replace('\\', '/').replace(".java", "");
                            found.merge(clazz.replace('/', '.') + "<" + simple, 1, Integer::sum);
                        }
                    }
                }
            }
        }
        assertEquals(BROAD_CATCHES, counted(found), "catch blocks for Throwable/Exception/RuntimeException/Error");
    }

    private static final Set<String> WORLD_WRITES = Set.of(
        "setBlock", "setBiome", "createEntity", "setTile", "regenerate", "fixLighting", "fixAfterFastMode",
        "applySideEffects", "clearContainerBlockContents", "dropItem", "simulateBlockMine", "generateTree"
    );

    @Test
    void noDirectWorldWrites() {
        Map<String, Integer> found = new TreeMap<>();
        for (JavaClass clazz : classes) {
            String pkg = clazz.getPackageName();
            if (pkg.startsWith("com.sk89q.worldedit.extent") || pkg.startsWith("com.sk89q.worldedit.world")) {
                continue;
            }
            for (JavaAccess<?> access : clazz.getAccessesFromSelf()) {
                JavaClass owner = access.getTargetOwner();
                if (owner.isAssignableTo("com.sk89q.worldedit.world.World")
                    && WORLD_WRITES.contains(access.getTarget().getName())) {
                    found.merge(outer(clazz) + "->" + access.getTarget().getName(), 1, Integer::sum);
                }
            }
        }
        assertEquals(DIRECT_WORLD_WRITES, counted(found), "writes straight to a World outside extents/world internals");
    }
}
