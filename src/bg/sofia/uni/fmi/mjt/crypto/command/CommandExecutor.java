package bg.sofia.uni.fmi.mjt.crypto.command;

import bg.sofia.uni.fmi.mjt.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.crypto.exception.AssetStorageException;
import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientResourcesException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeValueException;
import bg.sofia.uni.fmi.mjt.crypto.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.crypto.storage.AssetStorage;
import bg.sofia.uni.fmi.mjt.crypto.storage.CryptoUserStorage;
import bg.sofia.uni.fmi.mjt.crypto.user.CryptoUser;
import bg.sofia.uni.fmi.mjt.crypto.user.DefaultCryptoUser;
import bg.sofia.uni.fmi.mjt.crypto.user.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandExecutor {
    private static final String FORMAT_ASSET = "%s: %f";

    private static final CommandValidator VALIDATOR = new CommandValidator();

    private final CryptoUserStorage userStorage;
    private final AssetStorage assetStorage;

    public CommandExecutor(CryptoUserStorage userStorage, AssetStorage assetStorage) {
        this.userStorage = userStorage;
        this.assetStorage = assetStorage;
    }

    public String execute(String username, Command command) {
        CryptoUser user = userStorage.get(username);
        CommandFunction function = getCommandFunction(command);
        Optional<String> error = VALIDATOR.validate(user, command);

        try {
            return error.isEmpty() ? function.apply(user, command.arguments()) : error.get();
        } catch (NumberFormatException e) {
            return "Invalid argument. Only numbers allowed";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private CommandFunction getCommandFunction(Command command) {
        return switch (command.name()) {
            case Command.SIGN_UP -> this::signUp;
            case Command.LOG_IN -> this::logIn;
            case Command.LIST -> this::list;
            case Command.DEPOSIT -> this::deposit;
            case Command.BUY -> this::buy;
            case Command.SELL -> this::sell;
            case Command.SUMMARY -> this::summary;
            case Command.TRENDS -> this::trends;
            case Command.LOG_OUT -> this::logOut;
            case Command.EXIT -> this::exit;
            default -> this::unknownCommand;
        };
    }

    private String signUp(CryptoUser user, String[] args) throws UserAlreadyExistsException {
        String username = args[0];
        String password = BCrypt.hashpw(args[1], BCrypt.gensalt());
        CryptoUser newUser = new DefaultCryptoUser(username, password);

        userStorage.add(newUser);

        return "New user created";
    }

    private String logIn(CryptoUser user, String[] args) {
        String username = args[0];
        String password = args[1];

        User storageUser = userStorage.get(username);

        if (storageUser == null || !BCrypt.checkpw(password, storageUser.getPassword())) {
            return "Incorrect username or password";
        }

        return username;
    }

    private String list(CryptoUser user, String[] args) throws AssetStorageException {
        return assetStorage.getAssets().stream()
            .map(asset -> FORMAT_ASSET.formatted(asset.getId(), asset.getPrice()))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    private String deposit(CryptoUser user, String[] args) throws NegativeValueException {
        double cash = Double.parseDouble(args[0]);

        user.getWallet().deposit(cash);
        userStorage.update(user);

        return "Money successfully deposited. Current balance: " + user.getWallet().getBalance();
    }

    private String buy(CryptoUser user, String[] args)
        throws AssetStorageException, InsufficientResourcesException, NegativeValueException {
        String assetId = args[0];
        double moneyToInvest = Double.parseDouble(args[1]);
        Asset asset = assetStorage.getAsset(assetId);

        user.getWallet().buy(assetId, moneyToInvest, asset.getPrice());
        userStorage.update(user);

        return asset.getName() + " bought. Balance left: " + user.getWallet().getBalance();
    }

    private String sell(CryptoUser user, String[] args) throws AssetStorageException, AssetNotFoundException {
        String assetId = args[0];
        Asset asset = assetStorage.getAsset(assetId);

        user.getWallet().sell(assetId, asset.getPrice());
        userStorage.update(user);

        return asset.getName() + " sold. Current balance: " + user.getWallet().getBalance();
    }

    private String summary(CryptoUser user, String[] args) throws AssetStorageException {
        Map<String, Double> prices = assetStorage.getAssetPrices(user.getWallet().getInvestments());

        return user.getWallet().getSummary(prices);
    }

    private String trends(CryptoUser user, String[] args) throws AssetStorageException {
        Map<String, Double> prices = assetStorage.getAssetPrices(user.getWallet().getInvestments());

        return user.getWallet().getTrends(prices);
    }

    private String logOut(CryptoUser user, String[] args) {
        return "Logged out successfully";
    }

    private String exit(CryptoUser user, String[] args) {
        return "Goodbye";
    }

    private String unknownCommand(CryptoUser user, String[] args) {
        return "Unknown command";
    }
}
