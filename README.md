# Crypto Wallet :moneybag:

The Crypto Wallet is a command-line tool that allows users to manage their cryptocurrency investments and track their
portfolio. Users can perform various actions such as buying and selling cryptocurrencies, and getting summaries and
trends of their investments.

## Table of Contents

- [Key Features](#key-features-sparkles)
- [Supported Commands](#supported-commands-keyboard)
- [Contributing](#contributing)
- [License](#license)

## Key Features :sparkles:

- Enables concurrent user servicing, allowing the server to handle multiple users simultaneously for efficient and
  responsive interactions.
- Maintains user data persistence by storing registered users in a file on the server, allowing seamless reloading of
  existing users upon server restarts.
- Employs CoinAPI to fetch cryptocurrency information and stores the data in a cache for efficient retrieval.
- Implements comprehensive logging to track user interactions, system events, and error messages for effective debugging
  and monitoring.

## Supported Commands :keyboard:

| Name    | Params                    | Usage                                       |
|---------|---------------------------|---------------------------------------------|
| signup  | \<username\> \<password\> | Sign up for a new account                   |
| login   | \<username\> \<password\> | Log in to an existing account               |
| deposit | \<money\>                 | Deposit funds into your account             |
| list    | —                         | List all available cryptocurrencies         |
| buy     | \<id\> \<money\>          | Buy a specified amount of a cryptocurrency  |
| sell    | \<id\>                    | Sell a cryptocurrency                       |
| summary | —                         | View a summary of your investment portfolio |
| trends  | —                         | View the trends of your investments         |
| logout  | —                         | Log out of the current account              |
| exit    | —                         | Exit the Crypto Wallet                      |

## Contributing

Contributions are welcome! If you find any issues or have suggestions for improvement, feel free to submit a pull
request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
