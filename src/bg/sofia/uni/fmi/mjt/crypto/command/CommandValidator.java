package bg.sofia.uni.fmi.mjt.crypto.command;

import bg.sofia.uni.fmi.mjt.crypto.user.User;

import java.util.Optional;
import java.util.function.BiFunction;

public class CommandValidator {
    private static final String MESSAGE_FORMAT_N_ARGUMENTS_EXPECTED = "%d arguments expected";
    private static final String MESSAGE_ONE_ARGUMENT_EXPECTED = "1 argument expected";
    private static final String MESSAGE_LOG_OUT_FIRST = "Log out first";
    private static final String MESSAGE_LOG_IN_FIRST = "Log in first";

    public Optional<String> validate(User user, Command command) {
        BiFunction<User, String[], Optional<String>> function = switch (command.name()) {
            case Command.SIGN_UP -> this::validateSignUp;
            case Command.LOG_IN -> this::validateLogIn;
            case Command.LIST -> this::validateList;
            case Command.DEPOSIT -> this::validateDeposit;
            case Command.BUY -> this::validateBuy;
            case Command.SELL -> this::validateSell;
            case Command.SUMMARY -> this::validateSummary;
            case Command.TRENDS -> this::validateTrends;
            case Command.LOG_OUT -> this::validateLogOut;
            case Command.EXIT -> this::validateExit;
            default -> this::validateUnknownCommand;
        };

        return function.apply(user, command.arguments());
    }

    private Optional<String> validateSignUp(User user, String[] arguments) {
        if (user != null) {
            return Optional.of(MESSAGE_LOG_OUT_FIRST);
        }

        return checkArgumentCount(2, arguments.length);
    }

    private Optional<String> validateLogIn(User user, String[] arguments) {
        if (user != null) {
            return Optional.of(MESSAGE_LOG_OUT_FIRST);
        }

        return checkArgumentCount(2, arguments.length);
    }

    private Optional<String> validateList(User user, String[] arguments) {
        return checkArgumentCount(0, arguments.length);
    }

    private Optional<String> validateDeposit(User user, String[] arguments) {
        if (user == null) {
            return Optional.of(MESSAGE_LOG_IN_FIRST);
        }

        return checkArgumentCount(1, arguments.length);
    }

    private Optional<String> validateBuy(User user, String[] arguments) {
        if (user == null) {
            return Optional.of(MESSAGE_LOG_IN_FIRST);
        }

        return checkArgumentCount(2, arguments.length);
    }

    private Optional<String> validateSell(User user, String[] arguments) {
        if (user == null) {
            return Optional.of(MESSAGE_LOG_IN_FIRST);
        }

        return checkArgumentCount(1, arguments.length);
    }

    private Optional<String> validateSummary(User user, String[] arguments) {
        if (user == null) {
            return Optional.of(MESSAGE_LOG_IN_FIRST);
        }

        return checkArgumentCount(0, arguments.length);
    }

    private Optional<String> validateTrends(User user, String[] arguments) {
        if (user == null) {
            return Optional.of(MESSAGE_LOG_IN_FIRST);
        }

        return checkArgumentCount(0, arguments.length);
    }

    private Optional<String> validateLogOut(User user, String[] arguments) {
        if (user == null) {
            return Optional.of(MESSAGE_LOG_IN_FIRST);
        }

        return checkArgumentCount(0, arguments.length);
    }

    private Optional<String> validateExit(User user, String[] arguments) {
        return checkArgumentCount(0, arguments.length);
    }

    private Optional<String> validateUnknownCommand(User user, String[] arguments) {
        return Optional.empty();
    }

    private Optional<String> checkArgumentCount(int expected, int actual) {
        if (actual == expected) {
            return Optional.empty();
        }

        if (expected == 1) {
            return Optional.of(MESSAGE_ONE_ARGUMENT_EXPECTED);
        }

        return Optional.of(MESSAGE_FORMAT_N_ARGUMENTS_EXPECTED.formatted(expected));
    }
}
