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

package com.sk89q.worldedit.bukkit;

import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Checks that the Bukkit module only uses API that Spigot has too, so that WorldEdit keeps working on Spigot.
 * Paper-only API must be guarded (e.g. with PaperLib or a capability check) and listed below.
 */
class SpigotApiTest {

    private static final List<String> API_PACKAGES = List.of(
        "org.bukkit.", "com.destroystokyo.paper.", "io.papermc.paper.", "net.kyori.adventure."
    );

    // Paper-only API used behind a check (PaperLib.isPaper(), isFolia(), or only registered on Paper/Folia);
    // don't add to this without a guard
    private static final Set<String> GUARDED_PAPER_API = Set.of(
        "com.sk89q.bukkit.util.CommandRegistration -> org.bukkit.Bukkit#getCommandMap()",
        "com.sk89q.worldedit.bukkit.BukkitBlockCommandSender -> io.papermc.paper.threadedregions.scheduler.RegionScheduler#execute(Plugin,Location,Runnable)",
        "com.sk89q.worldedit.bukkit.BukkitBlockCommandSender -> org.bukkit.Bukkit#getRegionScheduler()",
        "com.sk89q.worldedit.bukkit.BukkitBlockCommandSender -> org.bukkit.Bukkit#isOwnedByCurrentRegion(Block)",
        "com.sk89q.worldedit.bukkit.BukkitEntity -> org.bukkit.entity.Entity#teleportAsync(Location)",
        "com.sk89q.worldedit.bukkit.BukkitPlayer -> org.bukkit.entity.Player#teleportAsync(Location) x2",
        "com.sk89q.worldedit.bukkit.BukkitServerInterface -> io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler#runAtFixedRate(Plugin,Consumer,long,long)",
        "com.sk89q.worldedit.bukkit.BukkitServerInterface -> org.bukkit.Bukkit#getCurrentTick()",
        "com.sk89q.worldedit.bukkit.BukkitServerInterface -> org.bukkit.Bukkit#getGlobalRegionScheduler()",
        "com.sk89q.worldedit.bukkit.BukkitWorld -> org.bukkit.block.Block#getState(boolean)",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> com.destroystokyo.paper.event.server.AsyncTabCompleteEvent#getBuffer()",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> com.destroystokyo.paper.event.server.AsyncTabCompleteEvent#getSender()",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> com.destroystokyo.paper.event.server.AsyncTabCompleteEvent#isCommand()",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> com.destroystokyo.paper.event.server.AsyncTabCompleteEvent#setCompletions(List)",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> com.destroystokyo.paper.event.server.AsyncTabCompleteEvent#setHandled(boolean)",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> io.papermc.paper.ServerBuildInfo#buildInfo()",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> io.papermc.paper.ServerBuildInfo#isBrandCompatible(Key)",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> io.papermc.paper.threadedregions.scheduler.AsyncScheduler#cancelTasks(Plugin)",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler#cancelTasks(Plugin)",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> net.kyori.adventure.key.Key#key(String,String)",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> org.bukkit.Server#getAsyncScheduler()",
        "com.sk89q.worldedit.bukkit.WorldEditPlugin -> org.bukkit.Server#getGlobalRegionScheduler()",
        "com.sk89q.worldedit.bukkit.folia.FoliaExtentListener -> org.bukkit.Bukkit#isOwnedByCurrentRegion(Location)"
    );

    // Name and parameter types of the methods every object has
    private static final Set<String> OBJECT_METHODS = Set.of(
        "equals(java.lang.Object)", "hashCode()", "toString()", "getClass()"
    );

    private static boolean isApi(String className) {
        return API_PACKAGES.stream().anyMatch(className::startsWith);
    }

    private static String outer(JavaClass clazz) {
        JavaClass top = clazz;
        while (top.getEnclosingClass().isPresent()) {
            top = top.getEnclosingClass().get();
        }
        return top.getName();
    }

    private static Stream<String> params(List<JavaClass> types) {
        return types.stream().map(JavaClass::getName);
    }

    private static String signature(AccessTarget target) {
        if (target instanceof AccessTarget.CodeUnitAccessTarget codeUnit) {
            return target.getName() + codeUnit.getRawParameterTypes().stream()
                .map(JavaClass::getSimpleName).collect(Collectors.joining(",", "(", ")"));
        }
        return target.getName();
    }

    private static boolean hasMember(JavaClass spigotClass, AccessTarget target) {
        if (target instanceof AccessTarget.MethodCallTarget method && OBJECT_METHODS.contains(
            method.getName() + "(" + String.join(",", params(method.getRawParameterTypes()).toList()) + ")")) {
            // Interfaces don't list the methods every object has
            return true;
        }
        if (target instanceof AccessTarget.FieldAccessTarget field) {
            String type = field.getRawType().getName();
            return spigotClass.getAllFields().stream()
                .anyMatch(f -> f.getName().equals(target.getName()) && f.getRawType().getName().equals(type));
        }
        AccessTarget.CodeUnitAccessTarget codeUnit = (AccessTarget.CodeUnitAccessTarget) target;
        // The JVM links by the full descriptor, so a changed return type breaks on Spigot too
        String returnType = codeUnit.getRawReturnType().getName();
        List<String> wanted = params(codeUnit.getRawParameterTypes()).toList();
        Stream<? extends JavaCodeUnit> candidates = target.getName().equals(JavaConstructor.CONSTRUCTOR_NAME)
            ? spigotClass.getConstructors().stream()
            : spigotClass.getAllMethods().stream();
        return candidates.anyMatch(m -> m.getName().equals(target.getName())
            && m.getRawReturnType().getName().equals(returnType)
            && params(m.getRawParameterTypes()).toList().equals(wanted));
    }

    /**
     * The API class an access goes to, if it goes to API. Calls to inherited methods through our own
     * subclasses (e.g. {@code getServer()} inside {@code WorldEditPlugin}) name our class as the owner, so those
     * are resolved to the class that declares the member.
     */
    private static Optional<String> apiOwner(JavaAccess<?> access) {
        String owner = access.getTargetOwner().getName();
        if (isApi(owner)) {
            return Optional.of(owner);
        }
        return access.getTarget().resolveMember()
            .map(member -> member.getOwner().getName())
            .filter(SpigotApiTest::isApi);
    }

    @Test
    void onlySpigotApiIsUsed() throws IOException {
        JavaClasses spigot;
        String spigotJar = System.getProperty("worldedit.spigotApi");
        assertNotNull(spigotJar, "worldedit.spigotApi is not set; run this test through Gradle");
        try (JarFile jar = new JarFile(spigotJar)) {
            spigot = new ClassFileImporter().importJar(jar);
        }
        JavaClasses ours = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
            .importPackages(
                "com.sk89q.worldedit.bukkit", "com.sk89q.wepif", "com.sk89q.bukkit",
                "com.sk89q.worldedit.util.formatting.text.adapter.bukkit"
            );

        Set<String> found = new TreeSet<>();
        Map<String, Integer> missingMembers = new TreeMap<>();
        Map<String, Set<String>> calledMissingClasses = new TreeMap<>();
        for (JavaClass clazz : ours) {
            for (JavaAccess<?> access : clazz.getAccessesFromSelf()) {
                Optional<String> apiOwner = apiOwner(access);
                if (apiOwner.isEmpty()) {
                    continue;
                }
                String owner = apiOwner.get();
                if (!spigot.contain(owner) || !hasMember(spigot.get(owner), access.getTarget())) {
                    if (!spigot.contain(owner)) {
                        calledMissingClasses.computeIfAbsent(outer(clazz), _ -> new TreeSet<>()).add(owner);
                    }
                    missingMembers.merge(outer(clazz) + " -> " + owner + "#" + signature(access.getTarget()), 1, Integer::sum);
                }
            }
        }
        // Paper-only classes that are only referenced as types (parameters, events, fields), not called
        for (JavaClass clazz : ours) {
            Set<String> called = calledMissingClasses.getOrDefault(outer(clazz), Set.of());
            for (Dependency dependency : clazz.getDirectDependenciesFromSelf()) {
                String target = dependency.getTargetClass().getBaseComponentType().getName();
                if (isApi(target) && !spigot.contain(target) && !called.contains(target)) {
                    found.add(outer(clazz) + " -> " + target);
                }
            }
        }
        // Count each call, so a new unguarded call to an already-listed member still fails
        missingMembers.forEach((key, count) -> found.add(count == 1 ? key : key + " x" + count));
        assertEquals(GUARDED_PAPER_API, found, "Paper-only API (missing from Spigot) used by the Bukkit module");
    }
}
