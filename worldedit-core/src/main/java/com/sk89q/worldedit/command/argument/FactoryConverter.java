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

package com.sk89q.worldedit.command.argument;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.ClipboardMask;
import com.sk89q.worldedit.internal.registry.AbstractFactory;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.request.RequestExtent;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;

public class FactoryConverter<T> implements ArgumentConverter<T> {

    public static void register(WorldEdit worldEdit, CommandManager commandManager) {
        commandManager.registerConverter(Key.of(Pattern.class),
            new FactoryConverter<>(worldEdit, WorldEdit::getPatternFactory, "pattern", null));
        commandManager.registerConverter(Key.of(Mask.class),
            new FactoryConverter<>(worldEdit, WorldEdit::getMaskFactory, "mask", null));
        commandManager.registerConverter(Key.of(BaseItem.class),
            new FactoryConverter<>(worldEdit, WorldEdit::getItemFactory, "item", null));

        commandManager.registerConverter(Key.of(Mask.class, ClipboardMask.class),
            new FactoryConverter<>(worldEdit, WorldEdit::getMaskFactory, "mask",
                context -> {
                    try {
                        ClipboardHolder holder = context.getSession().getClipboard();
                        Transform transform = holder.getTransform();
                        Extent target;
                        if (transform.isIdentity()) {
                            target = holder.getClipboard();
                        } else {
                            target = new BlockTransformExtent(holder.getClipboard(), transform);
                        }
                        context.setExtent(target);
                    } catch (EmptyClipboardException e) {
                        throw new IllegalStateException(e);
                    }
                }));
    }

    private final WorldEdit worldEdit;
    private final Function<WorldEdit, AbstractFactory<T>> factoryExtractor;
    private final String description;
    @Nullable private final Consumer<ParserContext> contextTweaker;

    private FactoryConverter(WorldEdit worldEdit,
                             Function<WorldEdit, AbstractFactory<T>> factoryExtractor,
                             String description,
                             @Nullable Consumer<ParserContext> contextTweaker) {
        this.worldEdit = worldEdit;
        this.factoryExtractor = factoryExtractor;
        this.description = description;
        this.contextTweaker = contextTweaker;
    }

    @Override
    public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
        Actor actor = context.injectedValue(Key.of(Actor.class))
            .orElseThrow(() -> new IllegalStateException("No actor"));
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);

        ParserContext parserContext = new ParserContext();
        parserContext.setActor(actor);
        if (actor instanceof Locatable) {
            Extent extent = ((Locatable) actor).getExtent();
            if (extent instanceof World) {
                parserContext.setWorld((World) extent);
            }
            parserContext.setExtent(new RequestExtent());
        } else if (session.hasWorldOverride()) {
            parserContext.setWorld(session.getWorldOverride());
            parserContext.setExtent(new RequestExtent());
        }
        parserContext.setSession(session);
        parserContext.setRestricted(true);

        if (contextTweaker != null) {
            contextTweaker.accept(parserContext);
        }

        try {
            return SuccessfulConversion.fromSingle(
                factoryExtractor.apply(worldEdit).parseFromInput(argument, parserContext)
            );
        } catch (InputParseException e) {
            return FailedConversion.from(e);
        }
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        return factoryExtractor.apply(worldEdit).getSuggestions(input);
    }

    @Override
    public Component describeAcceptableArguments() {
        return TextComponent.of("any " + description);
    }
}
