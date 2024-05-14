import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class testMain {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private Main main;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(output));
        main = new Main();
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setIn(System.in);
    }

    @Test
    public void testSuggestions() {
        String input = "1\nquality\ny\nworkload\nn\n4\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        main.runProgram();

        String expectedOutput = "Welcome to the MCIT Course Helper!\n";
        assertEquals(expectedOutput, output.toString().substring(0, expectedOutput.length()));
    }

    @Test
    public void testSearch() {
        String input = "2\nmath\nn\n4\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        main.runProgram();

        String expectedOutput = "Welcome to the MCIT Course Helper!\n";
        assertEquals(expectedOutput, output.toString().substring(0, expectedOutput.length()));
    }

    @Test
    public void testMain() {
        String input = "4\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        Main.main(new String[] {input});
    }
}