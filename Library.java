import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.stream.Stream;


public class Library {

    private static final int MAX_INPUT_LENGTH = 32;
    private static final int MIN_INPUT_LENGTH = 4;
    private static final String QUOTE_STORAGE_ROOT = System.getProperty("user.dir") + "\\data\\quotes\\";
    private static final String SOURCE_STORAGE_ROOT = System.getProperty("user.dir") + "\\data\\sources\\";
    private static final String DEBUG_ROOT = System.getProperty("user.dir") + "\\Debug\\";
    public static void main(String[] args) {
        addToLibrary(DEBUG_ROOT + "Jack and Jill.txt");
    }

    private static String crop(String input) {
        return input.substring(0, Math.min(input.length(), MAX_INPUT_LENGTH));
    }

    private static String removeCharacters(String input) {
        return input.replaceAll("[^a-zA-Z0-9]","")
                    .replaceAll("[aeiouAEIOU]","")
                    .toLowerCase();
    }

    private static String convertToFilePath(String input) {
        return QUOTE_STORAGE_ROOT + input.replaceAll(".(?!$)", "$0\\\\");
    }

    private static String[] readFileByLines(String filepath) throws IOException {
        if(Files.notExists(Paths.get(filepath))) {
            return new String[0];
        }
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filepath)))) {
            try (Stream<String> stream = Files.lines(Paths.get(filepath), StandardCharsets.UTF_8)) {
                String[] lines = new String[(int) stream.count()];
                for(int i = 0; i < lines.length; lines[i++] = br.readLine());
                return lines;
            }
        }
    }

    private static void appendToFile(String filepath, String message) throws IOException {
        Path p = Paths.get(filepath);
        if(Files.notExists(p)) {
            Files.createDirectories(p.getParent());
            Files.createFile(p);
        }
        for(String line : readFileByLines(filepath)) {
            if(line.equals(message)) {
                return;
            }
        }
        Files.write(p, (message+"\r\n").getBytes(), StandardOpenOption.APPEND);
    }

    public static String[] query(String input) {
        try {
            return readFileByLines(convertToFilePath(crop(removeCharacters(input)))+"\\sources.txt");
        } catch (IOException ioe) {
            System.out.println("Warning: IO Exception detected");
            return new String[0];
        }
    }

    public static void addToLibrary(String quote, String source) {
        try {
            quote = removeCharacters(quote);
            long before = ZonedDateTime.now().toInstant().toEpochMilli();
            for(int start = 0; start + MIN_INPUT_LENGTH <= quote.length(); start++) {
                for(int end = Math.min(start + MAX_INPUT_LENGTH, quote.length()); end - start >= MIN_INPUT_LENGTH; end--) {
                    String filepath = convertToFilePath(quote.substring(start, end))+"\\sources.txt";
                    // System.out.println("Appending to file: " + filepath);
                    appendToFile(filepath, source);
                }
            }
            long after = ZonedDateTime.now().toInstant().toEpochMilli();
            double elapsed = (after - before) / 1000.0;
            System.out.println("Time elapsed: " + elapsed + " seconds");
        } catch (IOException ioe) {
            System.out.println("Warning: IO Exception detected");
        }
    }

    public static void addToLibrary(String filepath) {
        try {
            Path p = Paths.get(filepath);
            String quote = new String(Files.readAllBytes(p));
            String source = p.getFileName().toString();
            addToLibrary(quote, source);
        } catch (IOException ioe) {
            System.out.println("Warning: IO Exception detected");
        }
    }



}