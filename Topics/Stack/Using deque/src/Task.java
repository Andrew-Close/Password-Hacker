// You can experiment here, it won’t be checked

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Task {
  public static void main(String[] args) {
    int[] array = {1, 2, 3 ,4};
    List<Integer> list = Arrays.asList(array);
    System.out.println(list.stream().map(x -> x + 1).collect(Collectors.toList()));
  }
}
