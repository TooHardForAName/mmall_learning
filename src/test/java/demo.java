import java.util.Scanner;

public class demo {
    public static void main(String[] args) {
        int temp = 0;
        System.out.println("请输入一个数：");
        Scanner input = new Scanner(System.in);
        int num = input.nextInt();
        for (int i = 1; i < num + 1; i++) {
            if (i % 2 == 1) {
                temp += i;
            } else {
                int j = i / 2;
                while (j % 2 == 0) {
                    j /= 2;
                }
                temp += j;
            }

            System.out.println(temp);

        }
        System.out.println("最大奇约数之和为：" + temp);
    }

}