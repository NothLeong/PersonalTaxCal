import java.math.BigDecimal;
import java.util.Scanner;

/** 用于处理用户交互逻辑的类
 * @author NothLeong
 * @version 1.0
 */
public class Main {
    /** 测试时可被测试替换的退出函数，防止中途终止程序结束测试
     */
    static Runnable exitAction = () -> System.exit(0);

    /**
     * 接收用户的输入，判断是否为退出关键词以及类型是否为BigDecimal
     * @param scanner main中创建的Scanner实例
     * @return 根据用户输入返回的BigDecimal实例
     */
    private static BigDecimal nextBigDecimalOrExit(Scanner scanner) {
        String input = scanner.next();
        if (input.equalsIgnoreCase("exit")) {
            System.out.println("程序已退出");
            exitAction.run();
        }
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            throw new RuntimeException("输入格式错误: " + input);
        }
    }

    /**
     * 接收用户的输入，判断是否为退出关键词以及类型是否为Integer
     * @param scanner main中创建的Scanner实例
     * @return 根据用户输入返回的int值
     */
    private static int nextIntOrExit(Scanner scanner) {
        String input = scanner.next();
        if (input.equalsIgnoreCase("exit")) {
            System.out.println("程序已退出");
            exitAction.run();
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new RuntimeException("输入格式错误: " + input);
        }
    }

    /**
     * 实现与用户交互式的计算器参数设置
     * @param scanner main中创建的Scanner实例
     * @return 配置好的TaxCal实例
     */
    private static TaxCal setCal(Scanner scanner) {
        TaxCal taxCal = new TaxCal();

        System.out.print("请设置起征点: ");
        BigDecimal threshold = nextBigDecimalOrExit(scanner);
        taxCal.setThreshold(threshold);

        System.out.print("税率表总级数: ");
        int numLevel = nextIntOrExit(scanner);
        taxCal.setNumLevel(numLevel);

        /* 设置税表逻辑
         * TaxCal提供的setTaxTable方法也有异常处理内容，但在用户交互过程中进行及时反馈更有利于用户体验
         * TaxCal内的异常处理更多是为了保证日后模块复用的正确性和变化的适应性。
         */
        BigDecimal[][] taxTable = new BigDecimal[numLevel][2];
        BigDecimal prevUpper = BigDecimal.ZERO;
        for (int i = 0; i < numLevel; i++) {
            if (i < numLevel - 1) {
                System.out.print("当前税级: " + (i + 1) + ", 应纳税所得额范围: " + prevUpper + "~: ");
                BigDecimal upper = nextBigDecimalOrExit(scanner);
                if (upper.compareTo(prevUpper) <= 0) {
                    throw new RuntimeException("第" + (i + 1) + "档上界" + upper + "应大于下界" + prevUpper);
                }
                taxTable[i][0] = upper;
                prevUpper = upper;
                System.out.print("税率: ");
            } else {
                taxTable[i][0] = new BigDecimal("-1");
                System.out.print("当前税级: " + (i + 1) + ", 应纳税所得额为" + prevUpper + "以上，税率: ");
            }
            BigDecimal rate = nextBigDecimalOrExit(scanner);
            if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw new RuntimeException("第" + (i + 1) + "档税率" + rate + "必须在(0, 1]之间");
            }
            taxTable[i][1] = rate;
        }
        taxCal.setTaxTable(taxTable);
        System.out.println("税表设置完成！");
        printConfig(taxCal);

        return taxCal;
    }

    /**
     * 循环接收用户输入的薪资并计算应纳税款
     * @param taxCal 配置好的税务计算器类TaxCal实例
     * @param scanner main中创建的Scanner实例
     */
    private static void runCalculation(TaxCal taxCal, Scanner scanner) {
        while (true) {
            System.out.print("请输入您的每月薪资: ");
            BigDecimal salary = nextBigDecimalOrExit(scanner);
            System.out.println("您的应纳税款为: " + taxCal.calTax(salary));
        }
    }

    /**
     * 打印当前税务配置，包括起征点、税级数和税表
     * @param taxCal 当前配置的TaxCal实例
     */
    private static void printConfig(TaxCal taxCal) {
        System.out.println("起征点: " + taxCal.getThreshold() + "  税级数: " + taxCal.getNumLevel());
        System.out.println("当前税表:");
        BigDecimal[][] taxTable = taxCal.getTaxTable();
        BigDecimal prevUpper = BigDecimal.ZERO;
        for (int i = 0; i < taxTable.length; i++) {
            BigDecimal upper = taxTable[i][0];
            BigDecimal rate  = taxTable[i][1];
            if (upper.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("  第" + (i + 1) + "档: " + prevUpper + "以上，税率: " + rate);
            } else {
                System.out.println("  第" + (i + 1) + "档: " + prevUpper + "~" + upper + "，税率: " + rate);
                prevUpper = upper;
            }
        }
    }

    /**
     * 为用户提供交互的main函数
     * @param args 参数列表
     */
    public static void main(String[] args) {
        System.out.println("欢迎使用个人所得税计算器。使用途中输入\"exit\"即可退出程序");
        System.out.println("温馨提示:税率应在(0,1]之间，如0.5；各种金额的单位都为元");
        try (Scanner scanner = new Scanner(System.in)) {
            TaxCal taxCal = setCal(scanner);
            runCalculation(taxCal, scanner);
        } catch (Exception e) {
            System.out.println("发生错误: " + e.getMessage());
        }
    }
}