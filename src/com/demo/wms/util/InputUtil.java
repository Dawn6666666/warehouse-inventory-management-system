package com.demo.wms.util;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * 控制台输入工具。
 * 把常见的输入校验逻辑集中收口，避免每个控制器都重复写 Scanner 判断。
 */
public final class InputUtil {
    private InputUtil() {
    }

    public static String readRequiredString(Scanner scanner, String prompt) {
        while (true) {
            String value = readLine(scanner, prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("输入不能为空，请重新输入。");
        }
    }

    public static String readOptionalString(Scanner scanner, String prompt) {
        return readLine(scanner, prompt);
    }

    public static int readMenuChoice(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            String text = readLine(scanner, prompt);
            try {
                int choice = Integer.parseInt(text);
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("菜单编号无效，请输入 " + min + " 到 " + max + " 之间的数字。");
        }
    }

    public static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            String text = readLine(scanner, prompt);
            try {
                int value = Integer.parseInt(text);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("请输入大于 0 的整数。");
        }
    }

    public static int readNonNegativeInt(Scanner scanner, String prompt) {
        while (true) {
            String text = readLine(scanner, prompt);
            try {
                int value = Integer.parseInt(text);
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("请输入大于等于 0 的整数。");
        }
    }

    public static BigDecimal readNonNegativeBigDecimal(Scanner scanner, String prompt) {
        while (true) {
            String text = readLine(scanner, prompt);
            try {
                BigDecimal value = new BigDecimal(text);
                if (value.compareTo(BigDecimal.ZERO) >= 0) {
                    return value;
                }
            } catch (NumberFormatException ex) {
                // 保留这个分支是为了让阅读者看清：这里本质上处理的是“数字格式不合法”。
            } catch (Exception ignored) {
            }
            System.out.println("请输入大于等于 0 的金额。");
        }
    }

    public static boolean readYesNo(Scanner scanner, String prompt) {
        while (true) {
            String text = readLine(scanner, prompt + " (Y/N)：");
            if ("Y".equalsIgnoreCase(text)) {
                return true;
            }
            if ("N".equalsIgnoreCase(text)) {
                return false;
            }
            System.out.println("请输入 Y 或 N。");
        }
    }

    public static void pause(Scanner scanner) {
        System.out.print("按回车键继续...");
        scanner.nextLine();
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
