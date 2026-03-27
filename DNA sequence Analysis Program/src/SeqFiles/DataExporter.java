package SeqFiles;

import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONArray;

public class DataExporter {

    /**
     * Exports alignment results to JSON for Python visualization
     */
    public static void exportAlignmentData(String filename,
                                           String s1, String s2,
                                           NeedlemanWunsch.Result nwResult,
                                           SmithWaterman.Result swResult,
                                           long nwTime, long swTime,
                                           long nwMemory, long swMemory) throws IOException {

        JSONObject data = new JSONObject();

        // Sequence information
        data.put("seq1_length", s1.length());
        data.put("seq2_length", s2.length());

        // Needleman-Wunsch results
        JSONObject nw = new JSONObject();
        nw.put("score", nwResult.score);
        nw.put("time_ns", nwTime);
        nw.put("time_us", nwTime / 1000.0);
        nw.put("memory_bytes", nwMemory);
        nw.put("memory_kb", nwMemory / 1024.0);
        nw.put("aligned_s1", nwResult.alignedS1);
        nw.put("aligned_s2", nwResult.alignedS2);
        data.put("needleman_wunsch", nw);

        // Smith-Waterman results
        JSONObject sw = new JSONObject();
        sw.put("score", swResult.maxScore);
        sw.put("time_ns", swTime);
        sw.put("time_us", swTime / 1000.0);
        sw.put("memory_bytes", swMemory);
        sw.put("memory_kb", swMemory / 1024.0);
        sw.put("aligned_s1", swResult.alignedS1);
        sw.put("aligned_s2", swResult.alignedS2);
        sw.put("start_i", swResult.startI);
        sw.put("start_j", swResult.startJ);
        sw.put("end_i", swResult.endI);
        sw.put("end_j", swResult.endJ);
        data.put("smith_waterman", sw);

        // Write to file
        try (FileWriter file = new FileWriter(filename)) {
            file.write(data.toString(4)); // Pretty print with indent
        }
    }

    /**
     * Exports multiple comparison results (for batch runs)
     */
    public static void exportBatchResults(String filename,
                                          java.util.List<ComparisonResult> results) throws IOException {

        JSONArray dataArray = new JSONArray();

        for (ComparisonResult result : results) {
            JSONObject entry = new JSONObject();
            entry.put("pair_name", result.pairName);
            entry.put("seq1_length", result.seq1Length);
            entry.put("seq2_length", result.seq2Length);
            entry.put("nw_score", result.nwScore);
            entry.put("nw_time_us", result.nwTime / 1000.0);
            entry.put("nw_memory_kb", result.nwMemory / 1024.0);
            entry.put("sw_score", result.swScore);
            entry.put("sw_time_us", result.swTime / 1000.0);
            entry.put("sw_memory_kb", result.swMemory / 1024.0);
            dataArray.put(entry);
        }

        try (FileWriter file = new FileWriter(filename)) {
            file.write(dataArray.toString(4));
        }
    }

    /**
     * Helper class to store comparison results
     */
    public static class ComparisonResult {
        public String pairName;
        public int seq1Length;
        public int seq2Length;
        public int nwScore;
        public long nwTime;
        public long nwMemory;
        public int swScore;
        public long swTime;
        public long swMemory;

        public ComparisonResult(String pairName, int seq1Length, int seq2Length,
                                int nwScore, long nwTime, long nwMemory,
                                int swScore, long swTime, long swMemory) {
            this.pairName = pairName;
            this.seq1Length = seq1Length;
            this.seq2Length = seq2Length;
            this.nwScore = nwScore;
            this.nwTime = nwTime;
            this.nwMemory = nwMemory;
            this.swScore = swScore;
            this.swTime = swTime;
            this.swMemory = swMemory;
        }
    }
}