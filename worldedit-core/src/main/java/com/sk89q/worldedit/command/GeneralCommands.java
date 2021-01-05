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
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.HookMode;
import com.sk89q.worldedit.command.util.WorldEditAsyncCommandBuilder;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.block.ApplySideEffect;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.util.formatting.component.SideEffectBox;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.item.ItemType;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * General WorldEdit commands.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class GeneralCommands {

    public static void register(CommandRegistrationHandler registration,
                                CommandManager commandManager,
                                CommandManagerService commandManagerService,
                                WorldEdit worldEdit) {
        // Collect the commands
        CommandManager collect = commandManagerService.newCommandManager();

        registration.register(
            collect,
            GeneralCommandsRegistration.builder(),
            new GeneralCommands(worldEdit)
        );


        Set<org.enginehub.piston.Command> commands = collect.getAllCommands()
            .collect(Collectors.toSet());
        for (org.enginehub.piston.Command command : commands) {
            if (command.getName().equals("/fast")) {
                // deprecate to `//perf`
                commandManager.register(CommandUtil.deprecate(
                    command, "//fast duplicates //perf "
                        + "and will be removed in WorldEdit 8",
                    GeneralCommands::replaceFastForPerf
                ));
                continue;
            }

            commandManager.register(command);
        }
    }

    private static Component replaceFastForPerf(org.enginehub.piston.Command oldCmd,
                                                CommandParameters oldParams) {
        if (oldParams.getMetadata() == null) {
            return CommandUtil.createNewCommandReplacementText("//perf");
        }
        ImmutableList<String> args = oldParams.getMetadata().getArguments();
        if (args.isEmpty()) {
            return TextComponent.of("There is not yet a replacement for //fast"
                + " with no arguments");
        }
        String arg0 = args.get(0).toLowerCase(Locale.ENGLISH);
        String flipped;
        switch (arg0) {
            case "on":
                flipped = "off";
                break;
            case "off":
                flipped = "on";
                break;
            default:
                return TextComponent.of("There is no replacement for //fast " + arg0);
        }
        return CommandUtil.createNewCommandReplacementText("//perf " + flipped);
    }

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public GeneralCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        name = "/limit",
        desc = "Modify block change limit"
    )
    @CommandPermissions("worldedit.limit")
    public void limit(Actor actor, LocalSession session,
                      @Arg(desc = "The limit to set", def = "")
                          Integer limit) {

        LocalConfiguration config = worldEdit.getConfiguration();
        boolean mayDisable = actor.hasPermission("worldedit.limit.unrestricted");

        limit = limit == null ? config.defaultChangeLimit : Math.max(-1, limit);
        if (!mayDisable && config.maxChangeLimit > -1) {
            if (limit > config.maxChangeLimit) {
                actor.printError(TranslatableComponent.of("worldedit.limit.too-high", TextComponent.of(config.maxChangeLimit)));
                return;
            }
        }

        session.setBlockChangeLimit(limit);
        Component component = TextComponent.empty().append(TranslatableComponent.of("worldedit.limit.set", TextComponent.of(limit)));
        if (limit != config.defaultChangeLimit) {
            component.append(TextComponent.space()).append(TranslatableComponent.of("worldedit.limit.return-to-default", TextColor.GRAY));
        }
        actor.printInfo(component);
    }

    @Command(
        name = "/timeout",
        desc = "Modify evaluation timeout time."
    )
    @CommandPermissions("worldedit.timeout")
    public void timeout(Actor actor, LocalSession session,
                        @Arg(desc = "The timeout time to set", def = "")
                            Integer limit) {
        LocalConfiguration config = worldEdit.getConfiguration();
        boolean mayDisable = actor.hasPermission("worldedit.timeout.unrestricted");

        limit = limit == null ? config.calculationTimeout : Math.max(-1, limit);
        if (!mayDisable && config.maxCalculationTimeout > -1) {
            if (limit > config.maxCalculationTimeout) {
                actor.printError(TranslatableComponent.of("worldedit.timeout.too-high", TextComponent.of(config.maxCalculationTimeout)));
                return;
            }
        }

        session.setTimeout(limit);
        Component component = TextComponent.empty().append(TranslatableComponent.of("worldedit.timeout.set", TextComponent.of(limit)));
        if (limit != config.calculationTimeout) {
            component.append(TranslatableComponent.of("worldedit.timeout.return-to-default", TextColor.GRAY));
        }
        actor.printInfo(component);
    }

    @Command(
        name = "/fast",
        desc = "Toggle fast mode"
    )
    @CommandPermissions("worldedit.fast")
    @SuppressWarnings("deprecation")
    void fast(Actor actor, LocalSession session,
              @Arg(desc = "The new fast mode state", def = "")
                  Boolean fastMode) {
        boolean hasFastMode = session.hasFastMode();
        if (fastMode != null && fastMode == hasFastMode) {
            actor.printError(TranslatableComponent.of(fastMode ? "worldedit.fast.enabled.already" : "worldedit.fast.disabled.already"));
            return;
        }

        if (hasFastMode) {
            session.setFastMode(false);
            actor.printInfo(TranslatableComponent.of("worldedit.fast.disabled"));
        } else {
            session.setFastMode(true);
            actor.printInfo(TranslatableComponent.of("worldedit.fast.enabled"));
        }
    }

    @Command(
        name = "/perf",
        desc = "Toggle side effects for performance",
        descFooter = "Note that this command is GOING to change in the future."
            + " Do not depend on the exact format of this command yet."
    )
    @CommandPermissions("worldedit.perf")
    void perf(Actor actor, LocalSession session,
              @Arg(desc = "The side effect", def = "")
                  SideEffect sideEffect,
              @Arg(desc = "The new side effect state", def = "")
                  SideEffect.State newState,
              @Switch(name = 'h', desc = "Show the info box")
                  boolean showInfoBox) throws WorldEditException {
        if (sideEffect != null) {
            SideEffect.State currentState = session.getSideEffectSet().getState(sideEffect);
            if (newState != null && newState == currentState) {
                if (!showInfoBox) {
                    actor.printError(TranslatableComponent.of(
                            "worldedit.perf.sideeffect.already-set",
                            TranslatableComponent.of(sideEffect.getDisplayName()),
                            TranslatableComponent.of(newState.getDisplayName())
                    ));
                }
                return;
            }

            if (newState != null) {
                session.setSideEffectSet(session.getSideEffectSet().with(sideEffect, newState));
                if (!showInfoBox) {
                    actor.printInfo(TranslatableComponent.of(
                            "worldedit.perf.sideeffect.set",
                            TranslatableComponent.of(sideEffect.getDisplayName()),
                            TranslatableComponent.of(newState.getDisplayName())
                    ));
                }
            } else {
                actor.printInfo(TranslatableComponent.of(
                        "worldedit.perf.sideeffect.get",
                        TranslatableComponent.of(sideEffect.getDisplayName()),
                        TranslatableComponent.of(currentState.getDisplayName())
                ));
            }
        } else if (newState != null) {
            SideEffectSet applier = session.getSideEffectSet();
            for (SideEffect sideEffectEntry : SideEffect.values()) {
                applier = applier.with(sideEffectEntry, newState);
            }
            session.setSideEffectSet(applier);
            if (!showInfoBox) {
                actor.printInfo(TranslatableComponent.of(
                        "worldedit.perf.sideeffect.set-all",
                        TranslatableComponent.of(newState.getDisplayName())
                ));
            }
        }

        if (sideEffect == null || showInfoBox) {
            SideEffectBox sideEffectBox = new SideEffectBox(session.getSideEffectSet());
            actor.print(sideEffectBox.create(1));
        }
    }

    @Command(
        name = "/update",
        desc = "Apply side effects to your selection"
    )
    @CommandPermissions("worldedit.update")
    void update(Actor actor, LocalSession session, World injectedWorld,
                @Arg(desc = "The side effects", def = "")
                    SideEffectSet sideEffectSet) throws WorldEditException {
        if (sideEffectSet == null) {
            // Use defaults if none supplied.
            sideEffectSet = SideEffectSet.defaults();
        }
        RegionFunction apply = new ApplySideEffect(injectedWorld, sideEffectSet);
        if (session.getMask() != null) {
            apply = new RegionMaskingFilter(session.getMask(), apply);
        }

        RegionVisitor visitor = new RegionVisitor(session.getSelection(injectedWorld), apply);
        Operations.complete(visitor);

        actor.printInfo(TranslatableComponent.of("worldedit.update"));
    }

    @Command(
        name = "/reorder",
        desc = "Sets the reorder mode of WorldEdit"
    )
    @CommandPermissions("worldedit.reorder")
    public void reorderMode(Actor actor, LocalSession session,
                            @Arg(desc = "The reorder mode", def = "")
                                EditSession.ReorderMode reorderMode) {
        if (reorderMode == null) {
            actor.printInfo(TranslatableComponent.of("worldedit.reorder.current", TextComponent.of(session.getReorderMode().getDisplayName())));
        } else {
            session.setReorderMode(reorderMode);
            actor.printInfo(TranslatableComponent.of("worldedit.reorder.set", TextComponent.of(session.getReorderMode().getDisplayName())));
        }
    }

    @Command(
        name = "/drawsel",
        desc = "Toggle drawing the current selection"
    )
    @CommandPermissions("worldedit.drawsel")
    public void drawSelection(Player player, LocalSession session,
                              @Arg(desc = "The new draw selection state", def = "")
                                  Boolean drawSelection) throws WorldEditException {
        if (!WorldEdit.getInstance().getConfiguration().serverSideCUI) {
            throw new AuthorizationException(TranslatableComponent.of("worldedit.error.disabled"));
        }
        boolean useServerCui = session.shouldUseServerCUI();
        if (drawSelection != null && drawSelection == useServerCui) {
            player.printError(TranslatableComponent.of("worldedit.drawsel." + (useServerCui ? "enabled" : "disabled") + ".already"));

            return;
        }
        if (useServerCui) {
            session.setUseServerCUI(false);
            session.updateServerCUI(player);
            player.printInfo(TranslatableComponent.of("worldedit.drawsel.disabled"));
        } else {
            session.setUseServerCUI(true);
            session.updateServerCUI(player);
            player.printInfo(TranslatableComponent.of("worldedit.drawsel.enabled"));
        }
    }

    @Command(
        name = "/world",
        desc = "Sets the world override"
    )
    @CommandPermissions("worldedit.world")
    public void world(Actor actor, LocalSession session,
            @Arg(desc = "The world override", def = "") World world) {
        session.setWorldOverride(world);
        if (world == null) {
            actor.printInfo(TranslatableComponent.of("worldedit.world.remove"));
        } else {
            actor.printInfo(TranslatableComponent.of("worldedit.world.set", TextComponent.of(world.getId())));
        }
    }

    @Command(
        name = "/watchdog",
        desc = "Changes watchdog hook state.",
        descFooter = "This is dependent on platform implementation. "
            + "Not all platforms support watchdog hooks, or contain a watchdog."
    )
    @CommandPermissions("worldedit.watchdog")
    public void watchdog(Actor actor, LocalSession session,
                         @Arg(desc = "The mode to set the watchdog hook to", def = "")
                             HookMode hookMode) {
        if (WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getWatchdog() == null) {
            actor.printError(TranslatableComponent.of("worldedit.watchdog.no-hook"));
            return;
        }
        boolean previousMode = session.isTickingWatchdog();
        if (hookMode != null && (hookMode == HookMode.ACTIVE) == previousMode) {
            actor.printError(TranslatableComponent.of(previousMode ? "worldedit.watchdog.active.already" : "worldedit.watchdog.inactive.already"));
            return;
        }
        session.setTickingWatchdog(!previousMode);
        actor.printInfo(TranslatableComponent.of(previousMode ? "worldedit.watchdog.inactive" : "worldedit.watchdog.active"));
    }

    @Command(
        name = "gmask",
        aliases = {"/gmask"},
        desc = "Set the global mask"
    )
    @CommandPermissions("worldedit.global-mask")
    public void gmask(Actor actor, LocalSession session,
                      @Arg(desc = "The mask to set", def = "")
                          Mask mask) {
        if (mask == null) {
            session.setMask(null);
            actor.printInfo(TranslatableComponent.of("worldedit.gmask.disabled"));
        } else {
            session.setMask(mask);
            actor.printInfo(TranslatableComponent.of("worldedit.gmask.set"));
        }
    }

    @Command(
        name = "toggleplace",
        aliases = {"/toggleplace"},
        desc = "Switch between your position and pos1 for placement"
    )
    public void togglePlace(Player player, LocalSession session) {
        if (session.togglePlacementPosition()) {
            player.printInfo(TranslatableComponent.of("worldedit.toggleplace.pos1"));
        } else {
            player.printInfo(TranslatableComponent.of("worldedit.toggleplace.player"));
        }
    }

    @Command(
        name = "searchitem",
        aliases = {"/searchitem", "/l", "/search"},
        desc = "Search for an item"
    )
    @CommandPermissions("worldedit.searchitem")
    public void searchItem(Actor actor,
                           @Switch(name = 'b', desc = "Only search for blocks")
                               boolean blocksOnly,
                           @Switch(name = 'i', desc = "Only search for items")
                               boolean itemsOnly,
                           @ArgFlag(name = 'p', desc = "Page of results to return", def = "1")
                               int page,
                           @Arg(desc = "Search query", variable = true)
                               List<String> query) {
        String search = String.join(" ", query);
        if (search.length() <= 2) {
            actor.printError(TranslatableComponent.of("worldedit.searchitem.too-short"));
            return;
        }
        if (blocksOnly && itemsOnly) {
            actor.printError(TranslatableComponent.of("worldedit.searchitem.either-b-or-i"));
            return;
        }

        WorldEditAsyncCommandBuilder.createAndSendMessage(actor, new ItemSearcher(search, blocksOnly, itemsOnly, page),
                TranslatableComponent.of("worldedit.searchitem.searching"));
    }

    private static class ItemSearcher implements Callable<Component> {
        private final boolean blocksOnly;
        private final boolean itemsOnly;
        private final String search;
        private final int page;

        ItemSearcher(String search, boolean blocksOnly, boolean itemsOnly, int page) {
            this.blocksOnly = blocksOnly;
            this.itemsOnly = itemsOnly;
            this.search = search;
            this.page = page;
        }

        @Override
        public Component call() throws Exception {
            String command = "/searchitem " + (blocksOnly ? "-b " : "") + (itemsOnly ? "-i " : "") + "-p %page% " + search;
            Map<String, Component> results = new TreeMap<>();
            String idMatch = search.replace(' ', '_');
            String nameMatch = search.toLowerCase(Locale.ROOT);
            for (ItemType searchType : ItemType.REGISTRY) {
                if (blocksOnly && !searchType.hasBlockType()) {
                    continue;
                }

                if (itemsOnly && searchType.hasBlockType()) {
                    continue;
                }
                final String id = searchType.getId();
                if (id.contains(idMatch)) {
                    Component name = searchType.getRichName();
                    results.put(id, TextComponent.builder()
                        .append(name)
                        .append(" (" + id + ")")
                        .build());
                }
            }
            List<Component> list = new ArrayList<>(results.values());
            return PaginationBox.fromComponents("Search results for '" + search + "'", command, list)
                .create(page);
        }
    }
}
