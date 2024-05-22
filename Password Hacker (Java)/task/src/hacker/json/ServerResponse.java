package hacker.json;

/**
 * An object which holds a server response and can be converted from json.
 */
public class ServerResponse {
    private final String result;

    public ServerResponse(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }
}
