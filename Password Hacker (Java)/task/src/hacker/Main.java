package hacker;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static final String PASSWORDS_DIRECTORY = "Password Hacker (Java)\\task\\src\\hacker\\passwords.txt";
    public static void main(String[] args) throws IOException {
        //String ipAddress = args[0]; int port = Integer.parseInt(args[1]);
        //try (Socket socket = new Socket(ipAddress, port);
             //DataInputStream input = new DataInputStream(socket.getInputStream());
             //DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
        System.out.println(bruteForceUsingDictionary());
        //}
    }

    private static String bruteForceUsingDictionary() throws IOException {
        try (Reader reader = new FileReader(PASSWORDS_DIRECTORY)) {
            // Holds each char that is read, used in the while loop
            StringBuilder commonPassword = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                char currentCharacter = (char) i;
                // Checking for whitespace/linebreak
                if (Character.toString(currentCharacter).matches("\\s")) {
                    BinaryFilter filter = new BinaryFilter(commonPassword.length());
                    filter.forEach(System.out::println);
                    commonPassword = new StringBuilder();
                } else {
                    commonPassword.append(currentCharacter);
                }
            }
        }
        // PLACEHOLDER
        return null;
    }

    /**
     * Tries to brute force all possible passwords.
     * @param input the input stream to which to respond if the password was correct or not
     * @param output the output stream to which to send the attempted password
     * @return the correct password once it is found
     * @throws IOException if something goes wrong with the input/output of data
     */
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
            if ("Connection success!".equals(input.readUTF())) {
                return sentPassword;
            }
        }
    }

    /**
     * Returns the next character to be checked in the next iteration of the cycle when brute forcing all combinations.
     * @param previousCharacter the previous character which is being changed
     * @return the next character to be checked
     */
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
    private static <T> String joinList(List<T> array) {
        StringBuilder sentPassword = new StringBuilder();
        array.forEach(x -> sentPassword.append(x.toString()));
        return sentPassword.toString();
    }
}
