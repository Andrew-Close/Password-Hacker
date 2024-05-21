package hacker.json;

/**
 * An object which stores a login-password pair and can be converted to or from json.
 */
public class LoginPasswordPair {
    private final String login;
    private final String password;

    public LoginPasswordPair(String login, String password) {
        this.login = login;
        this.password = password;
    }


    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
