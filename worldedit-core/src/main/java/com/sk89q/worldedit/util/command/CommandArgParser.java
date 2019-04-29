package com.sk89q.worldedit.util.command;

import java.util.stream.Stream;

public class CommandArgParser {

    private enum State {
        NORMAL,
        QUOTE
    }

    private final Stream.Builder<String> args = Stream.builder();
    private final StringBuilder currentArg = new StringBuilder();
    private final String input;
    private int index = 0;
    private State state = State.NORMAL;

    public CommandArgParser(String input) {
        this.input = input;
    }

    public Stream<String> parseArgs() {
        for (; index < input.length(); index++) {
            char c = input.charAt(index);
            switch (state) {
                case NORMAL:
                    handleNormal(c);
                    break;
                case QUOTE:
                    handleQuote(c);
            }
        }
        finishArg(true);
        return args.build();
    }

    private void handleNormal(char c) {
        switch (c) {
            case '"':
                state = State.QUOTE;
                break;
            case ' ':
                finishArg(true);
                break;
            case '\\':
                if (index + 1 < input.length()) {
                    index++;
                }
                appendChar(input.charAt(index));
                break;
            default:
                appendChar(c);
        }
    }

    private void handleQuote(char c) {
        switch (c) {
            case '"':
                state = State.NORMAL;
                finishArg(false);
                break;
            case '\\':
                if (index + 1 < input.length()) {
                    index++;
                }
                appendChar(input.charAt(index));
                break;
            default:
                appendChar(c);
        }
    }

    private void finishArg(boolean requireText) {
        if (currentArg.length() == 0 && requireText) {
            return;
        }
        args.add(currentArg.toString());
        currentArg.setLength(0);
    }

    private void appendChar(char c) {
        currentArg.append(c);
    }

}
