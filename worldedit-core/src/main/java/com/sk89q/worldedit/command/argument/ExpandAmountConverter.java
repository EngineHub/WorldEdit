package com.sk89q.worldedit.command.argument;

import com.google.common.reflect.TypeToken;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpandAmountConverter implements ArgumentConverter<ExpandAmount> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(ExpandAmount.class), new ExpandAmountConverter());
    }

    private final ArgumentConverter<Integer> integerConverter =
        ArgumentConverters.get(TypeToken.of(int.class));

    private ExpandAmountConverter() {
    }

    @Override
    public String describeAcceptableArguments() {
        return "`vert` or " + integerConverter.describeAcceptableArguments();
    }

    @Override
    public List<String> getSuggestions(String input) {
        return Stream.concat(Stream.of("vert"), integerConverter.getSuggestions(input).stream())
            .filter(x -> x.startsWith(input))
            .collect(Collectors.toList());
    }

    @Override
    public ConversionResult<ExpandAmount> convert(String argument, InjectedValueAccess context) {
        if (argument.equalsIgnoreCase("vert")
            || argument.equalsIgnoreCase("vertical")) {
            return SuccessfulConversion.fromSingle(ExpandAmount.vert());
        }
        return integerConverter.convert(argument, context).mapSingle(ExpandAmount::from);
    }
}
