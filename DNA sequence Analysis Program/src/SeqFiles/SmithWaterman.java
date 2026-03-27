package SeqFiles;

public class SmithWaterman {

    public static class Result {
        int maxScore;
        String alignedS1;
        String alignedS2;
        int[][] scoringMatrix;
        int endI, endJ;
        int startI, startJ;

        public Result(int maxScore, String alignedS1, String alignedS2,
                      int[][] scoringMatrix, int startI, int startJ, int endI, int endJ) {
            this.maxScore = maxScore;
            this.alignedS1 = alignedS1;
            this.alignedS2 = alignedS2;
            this.scoringMatrix = scoringMatrix;
            this.startI = startI;
            this.startJ = startJ;
            this.endI = endI;
            this.endJ = endJ;
        }
    }

    /**
     * Performs Smith-Waterman local sequence alignment.
     *
     * @param s1       First sequence
     * @param s2       Second sequence
     * @param match    Score for matching characters
     * @param mismatch Penalty for mismatching characters (typically negative)
     * @param gap      Penalty for gaps (typically negative)
     * @return Result object containing alignment details
     */
    public static Result smithWaterman(String s1, String s2, int match, int mismatch, int gap) {
        int m = s1.length();
        int n = s2.length();
        int[][] H = new int[m + 1][n + 1];

        int maxScore = 0;
        int maxI = 0, maxJ = 0;


        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int subScore = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? match : mismatch;
                int diag = H[i - 1][j - 1] + subScore;
                int up = H[i - 1][j] + gap;
                int left = H[i][j - 1] + gap;

                // Take maximum of 0 and all three directions
                int best = Math.max(0, Math.max(diag, Math.max(up, left)));
                H[i][j] = best;

                // Track maximum score position
                if (best > maxScore) {
                    maxScore = best;
                    maxI = i;
                    maxJ = j;
                }
            }
        }

        // Handle case where no alignment is found
        if (maxScore == 0) {
            return new Result(0, "", "", H, 0, 0, 0, 0);
        }

        // ------------------------------
        // 3. Traceback (without pointer matrix)
        // ------------------------------
        int i = maxI;
        int j = maxJ;
        StringBuilder align1 = new StringBuilder();
        StringBuilder align2 = new StringBuilder();

        // Traceback until we hit a zero or boundary
        while (i > 0 && j > 0 && H[i][j] > 0) {
            int currentScore = H[i][j];
            int subScore = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? match : mismatch;
            int diagScore = H[i - 1][j - 1] + subScore;
            int upScore = H[i - 1][j] + gap;
            int leftScore = H[i][j - 1] + gap;

            // Reconstruct path by checking which cell we came from
            if (currentScore == diagScore) {
                // Came from diagonal (match or mismatch)
                align1.append(s1.charAt(i - 1));
                align2.append(s2.charAt(j - 1));
                i--;
                j--;
            } else if (currentScore == upScore) {
                // Came from above (gap in s2)
                align1.append(s1.charAt(i - 1));
                align2.append('-');
                i--;
            } else if (currentScore == leftScore) {
                // Came from left (gap in s1)
                align1.append('-');
                align2.append(s2.charAt(j - 1));
                j--;
            } else {
                // Should not happen if algorithm is correct
                break;
            }
        }

        // Reverse strings since we built them backwards
        align1.reverse();
        align2.reverse();

        return new Result(maxScore, align1.toString(), align2.toString(),
                H, i, j, maxI, maxJ);
    }


    public static void printAlignment(Result result) {
        System.out.println("Alignment Score: " + result.maxScore);
        System.out.println();

        String s1 = result.alignedS1;
        String s2 = result.alignedS2;

        if (s1.isEmpty() || s2.isEmpty()) {
            System.out.println("No significant alignment found.");
            return;
        }

        // Build middle line showing matches (|), mismatches (:), and gaps ( )
        StringBuilder middle = new StringBuilder();
        for (int i = 0; i < s1.length(); i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 == c2 && c1 != '-') {
                middle.append('|');
            } else if (c1 == '-' || c2 == '-') {
                middle.append(' ');
            } else {
                middle.append(':');
            }
        }

        // Print in chunks of 60 characters for readability
        int chunkSize = 60;
        for (int i = 0; i < s1.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, s1.length());
            System.out.println(s1.substring(i, end));
            System.out.println(middle.substring(i, end));
            System.out.println(s2.substring(i, end));
            System.out.println();
        }

        System.out.println("Start position: (" + result.startI + ", " + result.startJ + ")");
        System.out.println("End position: (" + result.endI + ", " + result.endJ + ")");
    }


    // Prints the scoring matrix.

    public static void printMatrix(int[][] matrix, String s1, String s2) {
        System.out.println("\nScoring Matrix:");
        System.out.print("      ");
        for (int j = 0; j < s2.length(); j++) {
            System.out.printf("  %c", s2.charAt(j));
        }
        System.out.println();

        for (int i = 0; i <= s1.length(); i++) {
            if (i == 0) {
                System.out.print("  ");
            } else {
                System.out.printf("%c ", s1.charAt(i - 1));
            }

            for (int j = 0; j <= s2.length(); j++) {
                System.out.printf("%3d", matrix[i][j]);
            }
            System.out.println();
        }
    }


    // Example usage
    public static void main(String[] args) {
        String s1 = "ACACACGTACCATTAACTAGTAAC";
        String s2 = "AGCACACAAGTACGTTAACCATAC";

        int match = 2;
        int mismatch = -1;
        int gap = -1;

        System.out.println("Sequence 1: " + s1);
        System.out.println("Sequence 2: " + s2);
        System.out.println("\nScoring: Match=" + match + ", Mismatch=" + mismatch + ", Gap=" + gap);
        System.out.println("=".repeat(70));
        System.out.println();

        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long Before = rt.totalMemory() - rt.freeMemory();

        long start = System.nanoTime();
        Result result = smithWaterman(s1, s2, match, mismatch, gap);
        long end = System.nanoTime();
        long duration = end - start;
        long After = rt.totalMemory() - rt.freeMemory();
        long Used = After - Before;
        System.out.println("Memory used (bytes): " + Used);
        System.out.println("Execution time (nanoseconds): " + duration);

        printAlignment(result);
        printMatrix(result.scoringMatrix, s1, s2);
    }
}

