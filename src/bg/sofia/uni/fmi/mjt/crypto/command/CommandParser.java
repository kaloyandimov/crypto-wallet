package bg.sofia.uni.fmi.mjt.crypto.command;

import java.util.Arrays;

public class CommandParser {
    private static final String COMMAND_OPTION_SEPARATOR = "\\s+";

    public static Command parseCommand(String input) {
        if (input.isBlank()) {
            return Command.empty();
        }

        String[] tokens = CommandParser.getTokens(input.trim());
        String[] arguments = Arrays.copyOfRange(tokens, 1, tokens.length);

        return new Command(tokens[0], arguments);
    }

    private static String[] getTokens(String input) {
        return input.split(COMMAND_OPTION_SEPARATOR);
    }
}
