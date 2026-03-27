# DNA Sequence Alignment Analysis Program

A desktop application for comparing the performance of two classical sequence alignment algorithms, Needleman-Wunsch and Smith-Waterman, across large sets of DNA sequence pairs. Built in Java with a Swing GUI and a Python visualization pipeline.

---

## Overview

This program reads paired DNA sequences from a CSV file, runs both global (Needleman-Wunsch) and local (Smith-Waterman) alignment on each pair, and records execution time, memory usage, and alignment scores. Results can be exported to JSON and rendered as publication-quality charts via a Python visualization script.

The project was developed as a comparative study of two foundational dynamic programming algorithms in bioinformatics, examining how their time and space complexity behave across sequences of varying length.

---

## Algorithms

### Needleman-Wunsch (Global Alignment)

Needleman-Wunsch performs a global alignment, meaning it aligns the full length of both sequences end-to-end. It fills an (m+1) x (n+1) scoring matrix using the recurrence:

```
H[i][j] = max(
    H[i-1][j-1] + subScore,   // diagonal: match or mismatch
    H[i-1][j]   + gap,         // up: gap in sequence 2
    H[i][j-1]   + gap          // left: gap in sequence 1
)
```

Initialization sets H[i][0] = i * gap and H[0][j] = j * gap. Traceback starts from H[m][n] and reconstructs the full alignment.

This algorithm is optimal when you want to align two sequences of similar length or when the full-length alignment is biologically meaningful.

Time complexity: O(m * n)
Space complexity: O(m * n)

### Smith-Waterman (Local Alignment)

Smith-Waterman finds the highest-scoring local subsequence alignment. The key difference from Needleman-Wunsch is that cell values are floored at zero, and traceback begins from the cell with the maximum score rather than the bottom-right corner.

```
H[i][j] = max(
    0,
    H[i-1][j-1] + subScore,
    H[i-1][j]   + gap,
    H[i][j-1]   + gap
)
```

Traceback stops when a zero cell is encountered, yielding only the locally optimal alignment region.

This algorithm is preferred when sequences may share only a conserved domain or motif, and the surrounding regions are unrelated.

Time complexity: O(m * n)
Space complexity: O(m * n)

---

## Project Structure

```
DNA sequence Analysis Program/
├── src/SeqFiles/
│   ├── Main.java                      Entry point for command-line use
│   ├── NeedlemanWunsch.java           Global alignment implementation
│   ├── SmithWaterman.java             Local alignment implementation
│   ├── SequenceAlignmentComparator.java  Swing GUI application
│   ├── DNAReader.java                 CSV parsing and sequence loading
│   └── DataExporter.java              JSON export for batch results
│   └── GSD.csv                        Input sequence dataset
├── visualization.py                   Python charting script
├── display.py                         Tkinter GUI for viewing charts
├── batch_results.json                 Output from a batch run
└── visualizations/
    ├── batch_analysis.png
    ├── complexity_analysis.png
    └── summary_statistics.csv
```

---

## Input Format

Sequences are loaded from a CSV file with the following structure:

```
ID,Sequence
ds1_equal_pair1_seq1,TCCTTTCTCGCAGCGCAAC...
ds1_equal_pair1_seq2,TCCTTTCTCGCAGCACAAC...
```

The ID field is used to group sequences into pairs. The program expects pairs to share a common prefix separated by `_seq1` / `_seq2` suffixes. Only sequences containing valid DNA characters (A, C, G, T) are accepted.

The included dataset (`GSD.csv`) contains 50 sequence pairs drawn from two sub-datasets: equal-length pairs and unequal-length pairs, spanning lengths from 18 to approximately 5,000 bases.

---

## Running the Application

### Requirements

- Java 22 or later
- Python 3.8 or later (for visualization)
- Python packages: `pandas`, `matplotlib`, `seaborn`

```bash
pip install pandas matplotlib seaborn
```

### Compile and Run

Compile from the project root:

```bash
javac -d out src/SeqFiles/*.java
java -cp out SeqFiles.SequenceAlignmentComparator
```

Or run `SequenceAlignmentComparator` directly from your IDE.

---

## Using the GUI

The main window has four sections:

**Pair Selector and Scoring Parameters**
Select a sequence pair from the dropdown. Set match score, mismatch penalty, and gap penalty using the spinners. Defaults are match=2, mismatch=-1, gap=-1.

**Sequence Input Area**
Sequences are loaded automatically when a pair is selected. You can also paste sequences manually.

**Alignment Results Panel**
Shows the formatted alignment output, score, execution time, and memory usage for both algorithms side by side.

**Performance Comparison Table**
Accumulates results across runs, showing scores, times in microseconds, memory in kilobytes, and the percentage time difference between algorithms.

**Buttons**

- Run Comparison: aligns the currently loaded pair and displays results
- Run All Pairs: processes every pair in the dataset sequentially and fills the table
- Show Matrices: renders the full scoring matrices for both algorithms in a scrollable dialog (practical only for short sequences)
- Export Batch Results: saves the table data to `batch_results.json`
- Generate Visualizations: calls `visualization.py` to produce charts from the JSON file
- Clear Results: resets the table and result areas

---

## Scoring Parameters

All three parameters accept integer values, typically in the range -10 to 10.

| Parameter | Typical Value | Effect |
|-----------|---------------|--------|
| Match | +1 to +2 | Reward for identical bases |
| Mismatch | -1 to -3 | Penalty for non-identical bases |
| Gap | -1 to -4 | Penalty for insertions or deletions |

Higher gap penalties produce alignments with fewer but longer gaps. Higher mismatch penalties favor alignments with more gaps over mismatches.

---

## Visualization Output

After running all pairs and exporting, clicking Generate Visualizations produces three files in the `visualizations/` folder:

`batch_analysis.png` contains four panels: time vs. sequence length scatter plot with quadratic trend lines, memory vs. sequence length, a normalized heatmap across all pairs, and a grouped bar chart comparing per-pair execution times.

`complexity_analysis.png` shows time and memory on log-scale axes, making the O(m*n) quadratic growth easier to see.

`summary_statistics.csv` contains descriptive statistics (count, mean, std, min, quartiles, max) for all numeric columns across the 50 pairs.

The visualization script also launches `display.py`, a Tkinter viewer that loads the PNG files into scrollable tabs.

---

## Batch Results Format

`batch_results.json` is a JSON array. Each entry looks like:

```json
{
    "pair_name": "Pair 1",
    "seq1_length": 1321,
    "seq2_length": 3416,
    "nw_score": 411,
    "nw_time_us": 46696.2,
    "nw_memory_kb": 17957.38,
    "sw_score": 2274,
    "sw_time_us": 54577.2,
    "sw_memory_kb": 17928.12
}
```

Time is in microseconds (nanoseconds / 1000). Memory is the JVM heap delta in kilobytes captured with `Runtime.totalMemory() - Runtime.freeMemory()` before and after each alignment call. Note that JVM garbage collection timing can introduce noise into memory readings; values should be treated as approximate.

---

## Key Observations from some pairs tested

Across 50 sequence pairs with average lengths ranging from 34 to 3,344 bases:

- Smith-Waterman consistently produces higher scores than Needleman-Wunsch. This is expected because local alignment ignores low-scoring regions at the ends of sequences, while global alignment is penalized for them.
- Execution time for both algorithms grows roughly quadratically with sequence length, consistent with the O(m*n) complexity.
- Memory usage tracks closely between the two algorithms on most pairs because both allocate an (m+1) x (n+1) integer matrix. Some anomalous negative memory readings in the dataset reflect JVM GC events that reclaimed more memory than was allocated during measurement.
- Smith-Waterman is generally 5-30% slower than Needleman-Wunsch on the same pair, primarily because its traceback terminates at the first zero cell rather than the boundary, but the inner loop logic is otherwise identical.

---

## Limitations

Memory measurements using the JVM heap delta method are imprecise. JVM garbage collection can fire during alignment, producing readings that are artificially low or negative. For accurate memory profiling, use a JVM profiler such as VisualVM or async-profiler.

The traceback in both implementations reconstructs the path by re-evaluating scores rather than storing a pointer matrix. This is correct but slightly slower than a pointer-based traceback for large sequences, as each cell requires recomputing three candidate scores during traceback.

The CSV loader groups sequences by stripping `_seq1` / `_seq2` suffixes. If your naming convention differs, modify the `loadSequencePairs` method in `DNAReader.java`.

---

## Dependencies

| Dependency | Purpose |
|------------|---------|
| Java Swing | GUI framework |
| org.json | JSON serialization in DataExporter |
| pandas | DataFrame handling in visualization.py |
| matplotlib | Chart rendering |
| seaborn | Statistical chart styling |
| Pillow (PIL) | Image loading in display.py |

The `org.json` library must be on the classpath when compiling and running. If using Maven or Gradle, add the `org.json:json` artifact. If compiling manually, download the jar and include it with `-cp`.