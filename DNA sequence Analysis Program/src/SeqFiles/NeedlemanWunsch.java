package SeqFiles;

public class NeedlemanWunsch {
    
    public static class Result {
        int score;
        String alignedS1;
        String alignedS2;
        int[][] scoringMatrix;

        public Result(int score, String a1, String a2, int[][] scoringMatrix) {
            this.score = score;
            this.alignedS1 = a1;
            this.alignedS2 = a2;
            this.scoringMatrix = scoringMatrix;
        }
    }

    /**
     * Performs Needleman-Wunsch global sequence alignment.
     * 
     * @param s1 First sequence
     * @param s2 Second sequence
     * @param match Score for matching characters
     * @param mismatch Penalty for mismatching characters (typically negative)
     * @param gap Penalty for gaps (typically negative)
     * @return Result object containing alignment details
     */
    public static Result needlemanWunsch(String s1, String s2, int match, int mismatch, int gap) {
        int m = s1.length();
        int n = s2.length();

        int[][] H = new int[m + 1][n + 1];

        // ------------------------------
        // 1. Initialization
        // ------------------------------
        for (int i = 0; i <= m; i++) {
            H[i][0] = i * gap;
        }
        for (int j = 0; j <= n; j++) {
            H[0][j] = j * gap;
        }

        // ------------------------------
        // 2. Matrix Filling
        // ------------------------------
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int subScore = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? match : mismatch;
                int diag = H[i - 1][j - 1] + subScore;
                int up = H[i - 1][j] + gap;
                int left = H[i][j - 1] + gap;

                H[i][j] = Math.max(diag, Math.max(up, left));
            }
        }

        // ------------------------------
        // 3. Traceback (without pointer matrix)
        // ------------------------------
        StringBuilder alignedS1 = new StringBuilder();
        StringBuilder alignedS2 = new StringBuilder();

        int i = m, j = n;

        while (i > 0 || j > 0) {
            // At position (i, j), determine where we came from
            if (i > 0 && j > 0) {
                int currentScore = H[i][j];
                int subScore = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? match : mismatch;
                int diagScore = H[i - 1][j - 1] + subScore;
                int upScore = H[i - 1][j] + gap;
                int leftScore = H[i][j - 1] + gap;

                if (currentScore == diagScore) {
                    // Came from diagonal (match or mismatch)
                    alignedS1.append(s1.charAt(i - 1));
                    alignedS2.append(s2.charAt(j - 1));
                    i--;
                    j--;
                } else if (currentScore == upScore) {
                    // Came from above (gap in s2)
                    alignedS1.append(s1.charAt(i - 1));
                    alignedS2.append('-');
                    i--;
                } else {
                    // Came from left (gap in s1)
                    alignedS1.append('-');
                    alignedS2.append(s2.charAt(j - 1));
                    j--;
                }
            } else if (i > 0) {
                // Remaining characters from s1
                alignedS1.append(s1.charAt(i - 1));
                alignedS2.append('-');
                i--;
            } else {
                // Remaining characters from s2
                alignedS1.append('-');
                alignedS2.append(s2.charAt(j - 1));
                j--;
            }
        }

        // Reverse strings since we built them backwards
        alignedS1.reverse();
        alignedS2.reverse();

        return new Result(H[m][n], alignedS1.toString(), alignedS2.toString(), H);
    }

    /**
     * Pretty prints the alignment with markers for matches/mismatches.
     */
    public static void printAlignment(Result result) {
        System.out.println("Alignment Score: " + result.score);
        System.out.println();

        String s1 = result.alignedS1;
        String s2 = result.alignedS2;

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
    }

    /**
     * Prints the scoring matrix.
     */
    public static void printMatrix(int[][] matrix, String s1, String s2) {
        System.out.println("\nScoring Matrix:");
        
        // Only print matrix if sequences are reasonably small
        if (s1.length() > 50 || s2.length() > 50) {
            System.out.println("(Matrix too large to display - " + 
                             (s1.length() + 1) + " x " + (s2.length() + 1) + ")");
            return;
        }
        
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

    // ------------------------------------------------
    // Example usage
    // ------------------------------------------------
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
        Result result = needlemanWunsch(s1, s2, match, mismatch, gap);
        long end = System.nanoTime();
        long duration = (end - start);
        long After = rt.totalMemory() - rt.freeMemory();
        long memoryUsed = After - Before;
        System.out.println("Execution time (nanoseconds): " + duration);
        System.out.println("Memory used (bytes): " + memoryUsed);

        printAlignment(result);
        printMatrix(result.scoringMatrix, s1, s2);
    }
}