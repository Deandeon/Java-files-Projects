package SeqFiles;


import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DNAReader {

    /**
     * Loads DNA sequences from a CSV file
     * Expected format: ID,Sequence (one sequence per line)
     *
     * @param filename Path to the CSV file
     * @return ArrayList of DNA sequences
     * @throws IOException If file cannot be read
     */
    public static ArrayList<String> loadDNA(String filename) throws IOException {
        ArrayList<String> sequences = new ArrayList<>();

        List<String> lines = Files.readAllLines(Paths.get(filename));

        // Skip header and read each line
        for (int i = 1; i < lines.size(); i++) { // Start from 1 to skip header
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String sequence = parts[1].trim().toUpperCase();
                    // Validate that it contains only DNA characters
                    if (isValidDNA(sequence)) {
                        sequences.add(sequence);
                    } else {
                        System.out.println("Warning: Skipping invalid DNA sequence: " + sequence);
                    }
                }
            }
        }

        if (sequences.size() < 2) {
            throw new IOException("CSV file must contain at least 2 valid DNA sequences");
        }

        return sequences;
    }

    /**
     * Validates if a string contains only valid DNA characters (A, C, G, T)
     */
    private static boolean isValidDNA(String sequence) {
        return sequence.matches("[ACGT]+");
    }

    /**
     * Alternative method to load specific sequences by their IDs
     */
    public static ArrayList<String> loadDNAByIds(String filename, String... ids) throws IOException {
        ArrayList<String> sequences = new ArrayList<>();
        Set<String> targetIds = new HashSet<>(Arrays.asList(ids));

        List<String> lines = Files.readAllLines(Paths.get(filename));

        // Skip header
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String id = parts[0].trim();
                    String sequence = parts[1].trim().toUpperCase();

                    if (targetIds.contains(id) && isValidDNA(sequence)) {
                        sequences.add(sequence);
                    }
                }
            }
        }

        return sequences;
    }

    /**
     * Loads all sequence pairs from the CSV file
     * Returns a list of sequence pairs for batch processing
     */
    public static List<String[]> loadSequencePairs(String filename) throws IOException {
        List<String[]> pairs = new ArrayList<>();
        Map<String, String> sequenceMap = new HashMap<>();

        List<String> lines = Files.readAllLines(Paths.get(filename));

        // Read all sequences into a map
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String id = parts[0].trim();
                    String sequence = parts[1].trim().toUpperCase();
                    if (isValidDNA(sequence)) {
                        sequenceMap.put(id, sequence);
                    }
                }
            }
        }

        // Group sequences by pair (assuming naming convention: pairX_seq1, pairX_seq2)
        Map<String, List<String>> pairGroups = new HashMap<>();
        for (Map.Entry<String, String> entry : sequenceMap.entrySet()) {
            String id = entry.getKey();
            String sequence = entry.getValue();

            // Extract pair name (e.g., "pair1" from "pair1_seq1")
            String pairName = id.replaceAll("_seq\\d+$", "");

            pairGroups.computeIfAbsent(pairName, k -> new ArrayList<>()).add(sequence);
        }

        // Create pairs
        for (List<String> pairSequences : pairGroups.values()) {
            if (pairSequences.size() >= 2) {
                pairs.add(new String[]{pairSequences.get(0), pairSequences.get(1)});
            }
        }

        return pairs;
    }

    /**
     * Simple test method to verify the CSV file structure
     */
    public static void printFileInfo(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));

        System.out.println("CSV File Information:");
        System.out.println("Total lines: " + lines.size());
        System.out.println("Header: " + (lines.size() > 0 ? lines.get(0) : "None"));

        int sequenceCount = 0;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String sequence = parts[1].trim();
                    System.out.println("Sequence " + (++sequenceCount) + ": " +
                            parts[0] + " (length: " + sequence.length() + ")");
                }
            }
        }
    }
}

