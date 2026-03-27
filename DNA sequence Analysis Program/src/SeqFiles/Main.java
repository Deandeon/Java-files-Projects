package SeqFiles;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        try {
            ArrayList<String> seqList = DNAReader.loadDNA("src/SeqFiles/GSD.csv");

            String seq1 = seqList.get(0);
            String seq2 = seqList.get(1);

            System.out.println("Seq1 length: " + seq1.length());
            System.out.println("Seq2 length: " + seq2.length());

            int MATCH = 1;
            int MISMATCH = -1;
            int GAP = -2;


            NeedlemanWunsch.Result result =
                    NeedlemanWunsch.needlemanWunsch(seq1, seq2, MATCH, MISMATCH, GAP);


            System.out.println("\n=== Needleman–Wunsch Global Alignment ===");
            System.out.println("Score: " + result.score);
            System.out.println("Aligned Sequence 1:\n" + result.alignedS1);
            System.out.println("Aligned Sequence 2:\n" + result.alignedS2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}