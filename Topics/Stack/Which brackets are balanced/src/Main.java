import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        // put your code here
        Deque<String> stack = new ArrayDeque<>();
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        boolean isBalanced = true;
        loop:
        for (char bracket : input.toCharArray()) {
            if (bracket == '(' || bracket == '[' || bracket == '{') {
                stack.push(Character.toString(bracket));
            } else {
                switch (bracket) {
                    case ')':
                        if ("(".equals(stack.pollFirst())) {
                            break;
                        } else {
                            isBalanced = false;
                            break loop;
                        }
                    case ']':
                        if ("[".equals(stack.pollFirst())) {
                            break;
                        } else {
                            isBalanced = false;
                            break loop;
                        }
                    case '}':
                        if ("{".equals(stack.pollFirst())) {
                            break;
                        } else {
                            isBalanced = false;
                            break loop;
                        }
                }
            }
        }
        if (!stack.isEmpty()) {
            isBalanced = false;
        }
        System.out.println(isBalanced);
    }
}