package hacker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * This object holds a binary number which can be used to control algorithms, depending on the number. If the filter is 010, then the
 * algorithm will do the false case the first and third times, but will do the true case the second time. The filter can also modify strings
 * based on the state of the filter.
 */
public class BinaryFilter {
    // The actual binary filter
    int filter;
    // The length of the filter. Should not go above this length when incrementing
    int length;

    public BinaryFilter(int length) {
        int temp = 0b1;
        for (int i = 0; i < length; i++) {
            // The equivalent of multiplying a decimal number by 10, but in binary
            temp *= 2;
        }
        this.filter = temp;
        this.length = length;
    }

    /**
     * Increments the filter by 1. If the filter has reached its limit (if it is equal to one less than 2 ^ the length minus one), then it
     * will not increment.
     * @return whether or not the filter successfully incremented
     */
    public boolean increment() {
        /*
            Equation explanation:
            You need to check if the number is equal to one less than the smallest binary number with one more places than the length field
            in order to verify if you can increment the number. If it is, don't increment.
            The expression for finding the smallest binary number of a certain amount of places is 2 ^ (# of places - 1). Smallest binary number
            of 2 places would be 2 ^ 1 = 2, which is 10 in binary. 4 places would be 2 ^ 3 = 8, which is 1000 in binary.
            Because of this, the expression you should use is 2 ^ (# of places - 1 + 1) = 2 ^ # places.
            However, you also need to add one to the length to account for the 1 at the beginning of the binary number. So it would be
            2 ^ (# of places + 1).
            Then just subtract that by one, and that's it.
         */
        if (this.filter == Math.pow(2, this.length + 1) - 1) {
            return false;
        } else {
            this.filter += 1;
            return true;
        }
    }

    /**
     * Takes a string input and returns a modification of the string using the passed function for each character of the string which
     * corresponds with a "1" bit in the filter. For example, if you pass the string "cat" and a function which capitalizes the character,
     * and the filter is 010, then the function will only be applied to the second character, and the returned string will be cAt.
     * @param string the string to modify
     * @param function the function to apply to the corresponding characters
     * @return the modified string
     * @throws IllegalArgumentException if the length of the passed string is not equal to the length of the filter
     */
    public String modifyString(String string, UnaryOperator<Character> function) throws IllegalArgumentException {
        if (string.length() != this.length) {
            throw new IllegalArgumentException(String.format("The length of the passed string did not equal the length of the filter. Length of the string = %d, length of the filter = %d", string.length(), this.length));
        } else {
            StringBuilder modifiedString = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                if (this.getFilter().charAt(i) == '1') {
                    modifiedString.append(function.apply(string.charAt(i)));
                } else {
                    modifiedString.append(string.charAt(i));
                }
            }
            return modifiedString.toString();
        }
    }

    /**
     * Does the same thing as the modifyString method, but applies the function to the string for every combination of bits of the filter
     * and returns an array containing all the modifications. The filter cycles from all 0's to all 1's and applies the function at each iteration.
     * This method always starts at all 0's, and the initial state of the filter is saved at the beginning and reverted to at the end.
     * @param string the string to modify
     * @param function the function to apply to the corresponding characters
     * @return the array of modified string
     */
    public String[] modifyStringAllCombinations(String string, UnaryOperator<Character> function) {
        // The state of the filter before it is changed in the method so it can be reverted to at the end
        int initialFilter = this.filter;
        // Starts at all 0's
        this.filter = generateEmptyFilter();
        List<String> allModifications = new ArrayList<>();
        // Modifies the string when the filter has all 0's, will jump right to ...001 in the loop
        allModifications.add(modifyString(string, function));
        while (increment()) {
            allModifications.add(modifyString(string, function));
        }
        this.filter = initialFilter;
        return allModifications.toArray(new String[0]);

    }

    /**
     * Returns a filter of the specified length consisting of all 0's. Same algorithm when instantiating a new filter object.
     * @return the empty filter
     */
    private int generateEmptyFilter() {
        int emptyFilter = 0b1;
        for (int i = 0; i < this.length; i++) {
            // The equivalent of multiplying a decimal number by 10, but in binary
            emptyFilter *= 2;
        }
        return emptyFilter;
    }

    /**
     * Prints the filter. Does not include the first 1.
     */
    @Deprecated
    public void printFilter() {
        System.out.println(this.getFilter());
    }

    public String getFilter() {
        return Integer.toBinaryString(this.filter).substring(1);
    }
}
