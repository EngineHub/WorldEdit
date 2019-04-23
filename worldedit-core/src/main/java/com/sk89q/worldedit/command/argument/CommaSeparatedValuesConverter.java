package com.sk89q.worldedit.command.argument;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class CommaSeparatedValuesConverter<T> implements ArgumentConverter<T> {

    public static <T> CommaSeparatedValuesConverter<T> wrap(ArgumentConverter<T> delegate) {
        return wrapAndLimit(delegate, -1);
    }

    public static <T> CommaSeparatedValuesConverter<T> wrapAndLimit(ArgumentConverter<T> delegate, int maximum) {
        return new CommaSeparatedValuesConverter<>(delegate, maximum);
    }

    private static final Splitter COMMA = Splitter.on(',');

    private final ArgumentConverter<T> delegate;
    private final int maximum;

    private CommaSeparatedValuesConverter(ArgumentConverter<T> delegate, int maximum) {
        checkArgument(maximum == -1 || maximum > 1,
            "Maximum must be bigger than 1, or exactly -1");
        this.delegate = delegate;
        this.maximum = maximum;
    }

    @Override
    public String describeAcceptableArguments() {
        StringBuilder result = new StringBuilder();
        if (maximum > -1) {
            result.append("up to ").append(maximum).append(' ');
        }
        result.append("comma separated values of ")
            .append(delegate.describeAcceptableArguments());
        result.setCharAt(0, Character.toUpperCase(result.charAt(0)));
        return result.toString();
    }

    @Override
    public List<String> getSuggestions(String input) {
        String lastInput = Iterables.getLast(COMMA.split(input));
        return delegate.getSuggestions(lastInput);
    }

    @Override
    public ConversionResult<T> convert(String argument, InjectedValueAccess context) {
        ImmutableList.Builder<T> result = ImmutableList.builder();
        for (String input : COMMA.split(argument)) {
            ConversionResult<T> temp = delegate.convert(input, context);
            if (!temp.isSuccessful()) {
                return temp;
            }
            result.addAll(temp.get());
        }
        return SuccessfulConversion.from(result.build());
    }

}
