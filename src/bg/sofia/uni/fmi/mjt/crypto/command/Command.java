package bg.sofia.uni.fmi.mjt.crypto.command;

public record Command(String name, String[] arguments) {
    public static final String SIGN_UP = "signup";
    public static final String LOG_IN = "login";
    public static final String LIST = "list";
    public static final String DEPOSIT = "deposit";
    public static final String BUY = "buy";
    public static final String SELL = "sell";
    public static final String SUMMARY = "summary";
    public static final String TRENDS = "trends";
    public static final String LOG_OUT = "logout";
    public static final String EXIT = "exit";

    private static final Command EMPTY = new Command("", new String[]{});

    public static Command empty() {
        return EMPTY;
    }
}
