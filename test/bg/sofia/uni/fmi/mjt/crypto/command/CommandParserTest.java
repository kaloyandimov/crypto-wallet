package bg.sofia.uni.fmi.mjt.crypto.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandParserTest {
    @Test
    public void testParseCommandWithNoArguments() {
        String input = "list";

        Command command = CommandParser.parseCommand(input);
        String[] arguments = command.arguments();

        assertEquals("list", command.name(), "Command should have name \"list\"");
        assertEquals(0, arguments.length, "Command should have no arguments");
    }

    @Test
    public void testParseCommandWithSingleArgument() {
        String input = "deposit 500";

        Command command = CommandParser.parseCommand(input);
        String[] arguments = command.arguments();

        assertEquals("deposit", command.name(), "Command should have name \"deposit\"");
        assertEquals("500", arguments[0], "First argument should be 500");
    }

    @Test
    public void testParseCommandWithMoreArguments() {
        String input = "buy BTC 100";

        Command command = CommandParser.parseCommand(input);
        String[] arguments = command.arguments();

        assertEquals("buy", command.name(), "Command should have name \"buy\"");
        assertEquals("BTC", arguments[0], "First argument should be BTC");
        assertEquals("100", arguments[1], "Second argument should be 100");
    }

    @Test
    public void testParseCommandWithExtraSpaces() {
        String input = "  sell   BTC   200   ";

        Command command = CommandParser.parseCommand(input);
        String[] arguments = command.arguments();

        assertEquals("sell", command.name(), "Command should have name \"sell\"");
        assertEquals("BTC", arguments[0], "First argument should be BTC");
        assertEquals("200", arguments[1], "Second argument should be 200");
    }

    @Test
    public void testParseCommandWithEmptyInput() {
        String input = "";

        Command command = CommandParser.parseCommand(input);
        String[] arguments = command.arguments();

        assertEquals("", command.name(), "Command should have name \"\"");
        assertEquals(0, arguments.length, "Command should have no arguments");
        assertEquals(Command.empty(), command, "Command should be equal to empty command");
    }

    @Test
    public void testParseCommandWithWhitespaceInput() {
        String input = "   ";

        Command command = CommandParser.parseCommand(input);
        String[] arguments = command.arguments();

        assertEquals("", command.name(), "Command should have name \"\"");
        assertEquals(0, arguments.length, "Command should have no arguments");
        assertEquals(Command.empty(), command, "Command should be equal to empty command");
    }
}
