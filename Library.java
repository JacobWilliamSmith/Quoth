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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Library {

    private static final int MAX_INPUT_LENGTH = 8;
    private static final int MIN_INPUT_LENGTH = 4;
    private static final String STORAGE_ROOT = System.getProperty("user.dir") + "\\..\\data\\";
    private static final String DEBUG_ROOT = System.getProperty("user.dir") + "\\debug\\";
    public static void main(String[] args) {
        addToLibrary(DEBUG_ROOT + "Jack and Jill.txt");
        query("Jack fell down and broke his crown");
    }

    ///////////////////////////////////////////////////////////////////
    ///// PUBLIC METHODS: These should be called directly by main /////
    ///////////////////////////////////////////////////////////////////

    public static void addToLibrary(String quote, String source) {
        delimit();
        try {
            System.out.println("Adding " + source + " to library...");
            quote = removeCharacters(quote);
            long before = ZonedDateTime.now().toInstant().toEpochMilli();

            int maxIterations = ((MAX_INPUT_LENGTH - MIN_INPUT_LENGTH + 1) 
                              * (quote.length() - MAX_INPUT_LENGTH))
                              + (int) ((((quote.length() - (MIN_INPUT_LENGTH + (quote.length() - ((MAX_INPUT_LENGTH + MIN_INPUT_LENGTH + 1.0) / 2))) + 1)
                              * (MAX_INPUT_LENGTH - MIN_INPUT_LENGTH)) + 0.5))
                              + 1;
            
            for (int start = 0, currentIteration = 0; start + MIN_INPUT_LENGTH <= quote.length(); start++) {
                for (int end = Math.min(start + MAX_INPUT_LENGTH, quote.length()); end - start >= MIN_INPUT_LENGTH; end--) {
                    String filepath = convertToFilePath(quote.substring(start, end))+"\\sources.txt";
                    appendToFile(filepath, source);
                    currentIteration++;
                    trackProgress(currentIteration, maxIterations);
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
            source = source.substring(0, source.lastIndexOf('.'));
            addToLibrary(quote, source);
        } catch (IOException ioe) {
            System.out.println("Warning: IO Exception detected");
        }
    }

    public static void query(String quote) {
        delimit();
        try {
            System.out.println("Looking up quote: " + quote + "\n");

            long before = ZonedDateTime.now().toInstant().toEpochMilli();

            String compressedQuote = removeCharacters(quote);
            createFileIfNotExists(STORAGE_ROOT + "");

            String filepath = convertToFilePath(compressedQuote.substring(0, Math.min(0 + MAX_INPUT_LENGTH, compressedQuote.length())))+"\\sources.txt";
            Set<String> potentialQuotes = new HashSet<String>(Arrays.asList(readFileByLines(filepath)));

            for (int start = 1; start + MAX_INPUT_LENGTH <= compressedQuote.length(); start++) {
                filepath = convertToFilePath(compressedQuote.substring(start, Math.min(start + MAX_INPUT_LENGTH, compressedQuote.length())))+"\\sources.txt";
                potentialQuotes.retainAll(new HashSet<String>(Arrays.asList(readFileByLines(filepath))));
            }
            
            if(potentialQuotes.isEmpty()) {
                System.out.println("This quote was not found");
            } else {
                System.out.println("This quote was found in the following:");
                for (String s : potentialQuotes) {
                    System.out.println(s);
                }
            }

            long after = ZonedDateTime.now().toInstant().toEpochMilli();
            double elapsed = (after - before) / 1000.0;
            System.out.println("\nTime elapsed: " + elapsed + " seconds");

        } catch (IOException ioe) {
            System.out.println("Warning: IO Exception detected");
        }
    }

    public static void deleteLibrary() {
        delimit();
        System.out.println("Clearing all data from library...");
        long before = ZonedDateTime.now().toInstant().toEpochMilli();
        
        deleteDir(new File(STORAGE_ROOT));

        long after = ZonedDateTime.now().toInstant().toEpochMilli();
        double elapsed = (after - before) / 1000.0;
        System.out.println("Time elapsed: " + elapsed + " seconds");
    }

    ////////////////////////////////////////////////////////////////////////
    ///// PRIVATE METHODS: These should not be called directly by main /////
    ////////////////////////////////////////////////////////////////////////
    private static void trackProgress(long current, long total) {
        if (current > total) {
            throw new IllegalArgumentException();
        }

        int barLength = 50;
        int jumpLen = 33;

        if (current % jumpLen != 0 && current != total) {
            return;
        }
        
        int completeLength = (int) ((barLength * current) / total);

        String complete = new String(new char[completeLength]).replace('\0', '█');
        String incomplete = new String(new char[barLength - completeLength]).replace('\0', ' ');

        System.out.print("\r" + "|" + complete + incomplete + "|" + " " + current + " / " + total);
        if(current == total) {
            System.out.println();
        }
    }


    // private static String crop(String input) {
    //     return input.substring(0, Math.min(input.length(), MAX_INPUT_LENGTH));
    // }

    private static String removeCharacters(String input) {
        return input.replaceAll("[^a-zA-Z0-9]","")
                    .replaceAll("[aeiouAEIOU]","")
                    .toLowerCase();
    }

    private static String convertToFilePath(String input) {
        return STORAGE_ROOT + input.replaceAll(".(?!$)", "$0\\\\");
    }

    private static void appendToFile(String filepath, String message) throws IOException {
        createFileIfNotExists(filepath);
        for (String line : readFileByLines(filepath)) {
            if (line.equals(message)) {
                return;
            }
        }
        Files.write(Paths.get(filepath), (message+"\r\n").getBytes(), StandardOpenOption.APPEND);
    }

    private static String[] readFileByLines(String filepath) throws IOException {
        if (Files.notExists(Paths.get(filepath))) {
            return new String[0];
        }
        try (BufferedReader br = new BufferedReader(new FileReader(new File(filepath)))) {
            try (Stream<String> stream = Files.lines(Paths.get(filepath), StandardCharsets.UTF_8)) {
                String[] lines = new String[(int) stream.count()];
                for (int i = 0; i < lines.length; lines[i++] = br.readLine());
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

    private static void delimit() {
        System.out.println("===========================================================================");
    }
}