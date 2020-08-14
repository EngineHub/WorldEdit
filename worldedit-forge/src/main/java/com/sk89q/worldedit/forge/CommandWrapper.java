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

package com.sk89q.worldedit.forge;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.util.Substring;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MapBackedValueStore;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public final class CommandWrapper {
    private CommandWrapper() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher, org.enginehub.piston.Command command) {
        ImmutableList.Builder<String> aliases = ImmutableList.builder();
        aliases.add(command.getName()).addAll(command.getAliases());
        for (String alias : aliases.build()) {
            LiteralArgumentBuilder<CommandSource> base = literal(alias).executes(FAKE_COMMAND)
                .then(argument("args", StringArgumentType.greedyString())
                    .suggests(CommandWrapper::suggest)
                    .executes(FAKE_COMMAND));
            if (command.getCondition() != org.enginehub.piston.Command.Condition.TRUE) {
                base.requires(requirementsFor(command));
            }
            dispatcher.register(base);
        }
    }

    public static final Command<CommandSource> FAKE_COMMAND = ctx -> {
        if (ctx.getSource().getWorld().isRemote) {
            return 0;
        }
        return 1;
    };

    private static Predicate<CommandSource> requirementsFor(org.enginehub.piston.Command mapping) {
        return ctx -> {
            final Entity entity = ctx.getEntity();
            if (!(entity instanceof ServerPlayerEntity)) {
                return true;
            }
            final Actor actor = ForgeAdapter.adaptPlayer(((ServerPlayerEntity) entity));
            InjectedValueStore store = MapBackedValueStore.create();
            store.injectValue(Key.of(Actor.class), context -> Optional.of(actor));
            return mapping.getCondition().satisfied(store);
        };
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<CommandSource> context,
                                                          SuggestionsBuilder builder) throws CommandSyntaxException {
        CommandSuggestionEvent event = new CommandSuggestionEvent(
            ForgeAdapter.adaptPlayer(context.getSource().asPlayer()),
            builder.getInput()
        );
        WorldEdit.getInstance().getEventBus().post(event);
        List<Substring> suggestions = event.getSuggestions();

        ImmutableList.Builder<Suggestion> result = ImmutableList.builder();

        for (Substring suggestion : suggestions) {
            String suggestionText = suggestion.getSubstring();
            // If at end, we are actually suggesting the next argument
            // Ensure there is a space!
            if (suggestion.getStart() == suggestion.getEnd()
                && suggestion.getEnd() == builder.getInput().length()
                && !builder.getInput().endsWith(" ")
                && !builder.getInput().endsWith("\"")) {
                suggestionText = " " + suggestionText;
            }
            result.add(new Suggestion(
                StringRange.between(suggestion.getStart(), suggestion.getEnd()),
                suggestionText
            ));
        }

        return CompletableFuture.completedFuture(
            Suggestions.create(builder.getInput(), result.build())
        );
    }

}
