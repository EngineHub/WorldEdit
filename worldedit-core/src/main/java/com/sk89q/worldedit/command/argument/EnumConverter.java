package com.sk89q.worldedit.command.argument;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class EnumConverter<E extends Enum<E>> implements ArgumentConverter<E> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(SelectorChoice.class),
            new EnumConverter<>(SelectorChoice.class, SelectorChoice.UNKNOWN));
    }

    private final ImmutableMap<String, E> map;
    @Nullable
    private final E unknownValue;

    private EnumConverter(Class<E> enumClass, @Nullable E unknownValue) {
        ImmutableSortedMap.Builder<String, E> map = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);
        EnumSet<E> validValues = EnumSet.allOf(enumClass);
        if (unknownValue != null) {
            validValues.remove(unknownValue);
        }
        for (E e : validValues) {
            map.put(e.name(), e);
        }
        this.map = map.build();
        this.unknownValue = unknownValue;
    }

    @Override
    public String describeAcceptableArguments() {
        return String.join("|", map.keySet());
    }

    @Override
    public ConversionResult<E> convert(String argument, InjectedValueAccess context) {
        E result = map.getOrDefault(argument, unknownValue);
        return result == null
            ? FailedConversion.from(new IllegalArgumentException("Not a valid choice: " + argument))
            : SuccessfulConversion.fromSingle(result);
    }
}
