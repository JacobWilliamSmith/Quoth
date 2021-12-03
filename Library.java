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
    private static final String STORAGE_ROOT = System.getProperty("user.dir") + "\\data\\";
    private static final String DEBUG_ROOT = System.getProperty("user.dir") + "\\debug\\";
    public static void main(String[] args) {
        
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

    public static String[] query(String input) {
        try {
            createFileIfNotExists(STORAGE_ROOT + "quotes\\");
            createFileIfNotExists(STORAGE_ROOT + "sources\\");
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

    public static void deleteLibrary() {
        deleteDir(new File(STORAGE_ROOT));
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
        return STORAGE_ROOT + "quotes\\" + input.replaceAll(".(?!$)", "$0\\\\");
    }

    private static void appendToFile(String filepath, String message) throws IOException {
        createFileIfNotExists(filepath);
        for(String line : readFileByLines(filepath)) {
            if(line.equals(message)) {
                return;
            }
        }
        Files.write(Paths.get(filepath), (message+"\r\n").getBytes(), StandardOpenOption.APPEND);
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

    private static void createFileIfNotExists(String filepath) throws IOException {
        Path p = Paths.get(filepath);
        if(Files.notExists(p)) {
            Files.createDirectories(p.getParent());
            Files.createFile(p);
        }
    }
    
    private static void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
}