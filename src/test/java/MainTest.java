import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    /** 一个自定义异常，用于模拟测试中退出
     */
    static class ExitException extends RuntimeException {}

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
        Main.main(new String[]{});
        String output = getOutput();
        assertTrue(output.contains("500"));
    }

    @Test
    void testOutAtThreshold() {
        setInput("exit\n");
        assertDoesNotThrow(() -> Main.main(new String[]{}));
        assertTrue(getOutput().contains("程序已退出"));
    }

    @Test
    void testInvalidNumberFormat() {
        setInput("abc\n");
        Main.main(new String[]{});
        assertTrue(getOutput().contains("输入格式错误"));
    }

    @Test
    void testNegativeThreshold() {
        setInput("-1\n");
        Main.main(new String[]{});
        assertTrue(getOutput().contains("发生错误"));
    }
}