// You can experiment here, it won’t be checked

public class Task {
  public static void main(String[] args) {
    // put your code here
    /*
    int number = 0b01;
    System.out.println(number);
    number += 1;
    System.out.println(number);
    System.out.println(Integer.toBinaryString(number));

    int otherNumber = 0b101100101;
    String binaryString = Integer.toBinaryString(otherNumber);
    for (int i = 1; i < binaryString.length(); i++) {
      System.out.println(binaryString.charAt(i) == '1');
    }

    int thirdNum = 0b111111111;
    System.out.println(Integer.toBinaryString(thirdNum));
    thirdNum += 1;
    System.out.println(Integer.toBinaryString(thirdNum));
     */

    int num = 0b1;
    System.out.println(Integer.toBinaryString(num));
    num *= 2;
    System.out.println(Integer.toBinaryString(num));
    num *= 2;
    System.out.println(Integer.toBinaryString(num));
    num *= 2;
    System.out.println(Integer.toBinaryString(num));
    num *= 2;
    System.out.println(Integer.toBinaryString(num));
  }
}
