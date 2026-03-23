import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    /** 一个自定义异常，用于模拟测试中退出
     */
    static class ExitException extends Error {}

    @BeforeEach
    void setUp() {
        Main.exitAction = () -> { throw new ExitException(); };
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    private String getOutput() {
        return outContent.toString();
    }

    @Test
    void testNormalFlow() {
        // 起征点5000，1档税率0.1，薪资10000，预期税款500
        setInput("5000\n1\n0.1\n10000\nexit\n");
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        String output = getOutput();
        assertTrue(output.contains("500"));
    }

    @Test
    void testOutAtThreshold() {
        setInput("exit\n");
        // ExitException继承Error，assertDoesNotThrow捕获不到Error，会直接抛出导致测试失败
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        assertTrue(getOutput().contains("程序已退出"));
    }

    @Test
    void testInvalidNumberFormat() {
        // 输入abc后发生错误，循环继续，scanner阻塞
        // 需要在abc后加exit终止
        setInput("abc\nexit\n");
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        assertTrue(getOutput().contains("输入格式错误"));
    }

    @Test
    void testNegativeThreshold() {
        // 同理，-1报错后循环继续，需要exit终止
        setInput("-1\nexit\n");
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        assertTrue(getOutput().contains("发生错误"));
    }

    @Test
    void testResetDuringCalculation() {
        setInput("5000\n1\n0.1\n10000\nreset\n0\n1\n0.2\n1000\nexit\n");
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        String output = getOutput();
        assertTrue(output.contains("500"));
        assertTrue(output.contains("200"));
    }

    @Test
    void testResetBeforeConfig() {
        setInput("reset\n5000\n1\n0.1\nexit\n");
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        assertTrue(getOutput().contains("当前暂无配置信息"));
    }

    @Test
    void testConfigureDuringCalculation() {
        setInput("5000\n1\n0.1\nconfigure\nexit\n");
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        String output = getOutput();
        assertTrue(output.contains("起征点: 5000"));
        assertTrue(output.contains("税级数: 1"));
    }

    @Test
    void testConfigureDuringSetCal() {
        setInput("configure\n5000\n1\n0.1\nexit\n");
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        assertTrue(getOutput().contains("税务配置过程中不允许查看配置"));
    }

    @Test
    void testConfigureBeforeConfig() {
        setInput("configure\n5000\n1\n0.1\nexit\n");
        assertThrows(ExitException.class, () -> Main.main(new String[]{}));
        assertTrue(getOutput().contains("税务配置过程中不允许查看配置"));
    }
}