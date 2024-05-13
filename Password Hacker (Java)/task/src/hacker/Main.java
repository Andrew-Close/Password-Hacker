package hacker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        String ipAddress = args[0]; int port = Integer.parseInt(args[1]);
        try (Socket socket = new Socket(ipAddress, port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            List<Character> passwordAttempt = new ArrayList<>();
            // The initial character is ` because its ascii code is right behind the ascii code of "a". This way, it gets changed to "a" in the first iteration of the algorithm instead of "a" getting skipped over

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
                // Password to be checked
                String sentPassword = joinList(passwordAttempt);
                output.writeUTF(sentPassword);
                if ("Connection success!".equals(input.readUTF())) {
                    System.out.println(sentPassword);
                    break;
                }
            }
        }
    }

    /**
     * Returns the next character to be checked in the next iteration of the cycle.
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
     * Takes a char array and returns a string containing the contents of the array in the same order.
     * @param array the char array to be joined
     * @return the joined array
     */
    private static <T> String joinList(List<T> array) {
        StringBuilder sentPassword = new StringBuilder();
        array.forEach(x -> sentPassword.append(x.toString()));
        return sentPassword.toString();
    }
}
