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
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.command.factory.ClipboardPasteFactory;
import com.sk89q.worldedit.command.factory.ItemUseFactory;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.annotation.ClipboardMask;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.ArgAcceptingCommandFlag;
import org.enginehub.piston.part.SubCommandPart;

import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;
import static org.enginehub.piston.part.CommandParts.flag;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class ApplyCommands {

    private static final ArgAcceptingCommandFlag APPLY_MASK = flag(
            'm', TranslatableComponent.of("worldedit.apply.mask"))
        .withRequiredArg()
        .argNamed(TranslatableComponent.of("mask"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(Key.of(Mask.class)))
        .build();

    public static void register(CommandManagerService service, CommandManager commandManager,
                                CommandRegistrationHandler registration) {
        commandManager.register("/apply", builder -> {
            builder.description(TranslatableComponent.of("worldedit.apply.description"));
            builder.action(org.enginehub.piston.Command.Action.NULL_ACTION);

            CommandManager manager = service.newCommandManager();
            registration.register(
                manager,
                ApplyCommandsRegistration.builder(),
                new ApplyCommands()
            );

            builder.condition(new PermissionCondition(ImmutableSet.of("worldedit.region.apply")));
            builder.addPart(APPLY_MASK);
            builder.addPart(SubCommandPart.builder(
                    TranslatableComponent.of("type"),
                    TranslatableComponent.of("worldedit.apply.type"))
                .withCommands(manager.getAllCommands().toList())
                .required()
                .build());
        });
    }

    private int apply(CommandParameters parameters, Actor actor, EditSession editSession,
                      LocalSession localSession, Region region,
                      Contextual<? extends RegionFunction> functionFactory) throws WorldEditException {
        EditContext context = new EditContext();
        context.setDestination(editSession);
        context.setRegion(region);
        context.setSession(localSession);

        RegionFunction function = functionFactory.createFromContext(context);
        Mask applyMask = APPLY_MASK.value(parameters).asSingle(Mask.class);
        if (applyMask != null) {
            function = new RegionMaskingFilter(applyMask, function);
        }

        RegionVisitor visitor = new RegionVisitor(region, function);
        Operations.completeLegacy(visitor);
        actor.printInfo(TranslatableComponent.of("worldedit.apply.done", TextComponent.of(visitor.getAffected())));
        return visitor.getAffected();
    }

    @Command(
        name = "clipboard",
        desc = "Paste the clipboard at every block"
    )
    @CommandPermissions("worldedit.clipboard.paste")
    @Logging(REGION)
    public int clipboard(CommandParameters parameters, Actor actor, EditSession editSession,
                         LocalSession localSession, @Selection Region region,
                         @Switch(name = 'a', desc = "Don't paste air from the clipboard")
                             boolean ignoreAir,
                         @Switch(name = 'v', desc = "Include structure void blocks")
                             boolean pasteStructureVoid,
                         @Switch(name = 'o', desc = "Paste starting at each block, instead of centering on it")
                             boolean usingOrigin,
                         @Switch(name = 'e', desc = "Paste entities if available")
                             boolean pasteEntities,
                         @Switch(name = 'b', desc = "Paste biomes if available")
                             boolean pasteBiomes,
                         @ArgFlag(name = 'm', desc = "Only paste clipboard blocks matching this mask")
                         @ClipboardMask
                             Mask sourceMask) throws WorldEditException {
        return apply(parameters, actor, editSession, localSession, region, new ClipboardPasteFactory(
            localSession.getClipboard(),
            ignoreAir,
            !pasteStructureVoid,
            usingOrigin,
            pasteEntities,
            pasteBiomes,
            sourceMask
        ));
    }

    @Command(
        name = "item",
        desc = "Use an item"
    )
    @CommandPermissions("worldedit.brush.item")
    @Logging(REGION)
    public int item(CommandParameters parameters, Actor actor, EditSession editSession, LocalSession localSession,
                    @Selection Region region,
                    @Arg(desc = "The type of item to use")
                        BaseItem item,
                    @Arg(desc = "The direction in which the item will be applied", def = "up")
                    @Direction(includeDiagonals = true)
                        com.sk89q.worldedit.util.Direction direction) throws WorldEditException {
        actor.print(TranslatableComponent.of(
            "worldedit.warning",
            TranslatableComponent.of("worldedit.apply.item.warning")
                .color(TextColor.WHITE)
                .decoration(TextDecoration.BOLD, false)
        ).color(TextColor.RED).decoration(TextDecoration.BOLD, true));
        return apply(parameters, actor, editSession, localSession, region, new ItemUseFactory(item, direction));
    }
}
