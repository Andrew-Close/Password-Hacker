package hacker;

import java.util.*;
import java.util.function.Consumer;

/**
 * This object holds a binary number which can be used to control algorithms, depending on the number. If the filter is 010, then the
 * algorithm will do the false case the first and third times, but will do the true case the second time.
 */
public class BinaryFilter implements Iterable<Character> {
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
    public boolean incrementFilter() {
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




    /*




    Maybe figure out how to implement Iterable.




     */





    public void printFilter() {
        System.out.println(Integer.toBinaryString(this.filter).substring(1));
    }

    @Override
    public Iterator<Character> iterator() {
        List<Character> filterList = new ArrayList<>();
        for (char bit : Integer.toBinaryString(this.filter).substring(1).toCharArray()) {
            filterList.add(bit);
        }
        return filterList.iterator();
    }

    @Override
    public void forEach(Consumer action) {
        Objects.requireNonNull(action);

        for (char temp : this) {
            boolean t = temp == '1';
            action.accept(t);
        }
    }
}
