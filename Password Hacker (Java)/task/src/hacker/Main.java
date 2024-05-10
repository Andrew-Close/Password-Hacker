package hacker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String ipAddress = args[0]; int port = Integer.parseInt(args[1]); String message = args[2];
        //try (Socket socket = new Socket(ipAddress, port);
             //DataInputStream input = new DataInputStream(socket.getInputStream());
             //DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            List<Character> passwordAttempt = new ArrayList<>();
            passwordAttempt.add('a');
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
                System.out.println(passwordAttempt.toString());
                Thread.sleep(1L);
            }
        //}
    }

    /**
     * Returns the next character to be checked in the next iteration of the cycle.
     * @param previousCharacter the previous character which is being changed
     * @return the next character to be checked
     */
    public static char nextCharacter(char previousCharacter) {
        if (previousCharacter == 'z') {
            return '0';
        }
        return (char) (previousCharacter + 1);
    }
}
