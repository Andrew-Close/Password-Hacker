import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        // Is the actual stack which stores the values
        Deque<Integer> storageStack = new ArrayDeque<>();
        // Stack which keeps track of which values have been the max value, so when a max value is popped, the max value is the previous one
        Deque<Integer> maxStack = new ArrayDeque<>();
        // The current max value
        int max = 0;

        Scanner scanner = new Scanner(System.in);
        int numOfCommands = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < numOfCommands; i++) {
            String command = scanner.nextLine();
            // Avoid out of bounds exception
            if (command.length() == 3) {
                if ("pop".equals(command.substring(0, 3))) {
                    int value = storageStack.pop();
                    if (value == max) {
                        maxStack.pop();
                        if (maxStack.peekFirst() == null) {
                            max = 0;
                        } else {
                            max = maxStack.peekFirst();
                        }
                    }
                } else if ("max".equals(command)) {
                    System.out.println(max);
                }
            } else {
                if ("push".equals(command.substring(0, 4))) {
                    int value = Integer.parseInt(command.substring(5));
                    if (value >= max) {
                        max = value;
                        maxStack.push(value);
                    }
                    storageStack.push(value);
                }
            }
        }
    }
}