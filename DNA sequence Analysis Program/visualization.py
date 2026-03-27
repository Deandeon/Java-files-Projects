import json
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import sys
import os

# Set style
sns.set_theme(style="whitegrid")
plt.rcParams['figure.figsize'] = (12, 8)

def load_data(filename):
    """Load JSON data from file"""
    with open(filename, 'r') as f:
        return json.load(f)

def visualize_single_comparison(data, output_dir='visualizations'):
    """Create visualizations for a single alignment comparison"""
    os.makedirs(output_dir, exist_ok=True)

    # Extract data
    nw = data['needleman_wunsch']
    sw = data['smith_waterman']
    seq_len = (data['seq1_length'] + data['seq2_length']) / 2

    # 1. Time Comparison Bar Chart
    fig, axes = plt.subplots(2, 2, figsize=(14, 10))

    # Time comparison
    algorithms = ['Needleman-Wunsch', 'Smith-Waterman']
    times = [nw['time_us'], sw['time_us']]

    axes[0, 0].bar(algorithms, times, color=['#3498db', '#e74c3c'])
    axes[0, 0].set_ylabel('Time (μs)', fontsize=12)
    axes[0, 0].set_title('Execution Time Comparison', fontsize=14, fontweight='bold')
    axes[0, 0].grid(axis='y', alpha=0.3)

    # Add value labels on bars
    for i, v in enumerate(times):
        axes[0, 0].text(i, v + max(times)*0.02, f'{v:.2f}',
                       ha='center', va='bottom', fontsize=10)

    # 2. Memory Comparison Bar Chart
    memory = [nw['memory_kb'], sw['memory_kb']]

    axes[0, 1].bar(algorithms, memory, color=['#3498db', '#e74c3c'])
    axes[0, 1].set_ylabel('Memory (KB)', fontsize=12)
    axes[0, 1].set_title('Memory Usage Comparison', fontsize=14, fontweight='bold')
    axes[0, 1].grid(axis='y', alpha=0.3)

    for i, v in enumerate(memory):
        axes[0, 1].text(i, v + max(memory)*0.02, f'{v:.2f}',
                       ha='center', va='bottom', fontsize=10)

    # 3. Score Comparison
    scores = [nw['score'], sw['score']]

    axes[1, 0].bar(algorithms, scores, color=['#3498db', '#e74c3c'])
    axes[1, 0].set_ylabel('Alignment Score', fontsize=12)
    axes[1, 0].set_title('Alignment Score Comparison', fontsize=14, fontweight='bold')
    axes[1, 0].grid(axis='y', alpha=0.3)

    for i, v in enumerate(scores):
        axes[1, 0].text(i, v + abs(max(scores))*0.02, f'{v}',
                       ha='center', va='bottom', fontsize=10)

    # 4. Combined Metrics (Normalized)
    time_norm = [t/max(times) for t in times]
    mem_norm = [m/max(memory) for m in memory]

    x = range(len(algorithms))
    width = 0.35

    axes[1, 1].bar([i - width/2 for i in x], time_norm, width,
                   label='Time (norm)', color='#3498db', alpha=0.8)
    axes[1, 1].bar([i + width/2 for i in x], mem_norm, width,
                   label='Memory (norm)', color='#e74c3c', alpha=0.8)
    axes[1, 1].set_ylabel('Normalized Value', fontsize=12)
    axes[1, 1].set_title('Normalized Performance Metrics', fontsize=14, fontweight='bold')
    axes[1, 1].set_xticks(x)
    axes[1, 1].set_xticklabels(algorithms)
    axes[1, 1].legend()
    axes[1, 1].grid(axis='y', alpha=0.3)

    plt.tight_layout()
    plt.savefig(f'{output_dir}/single_comparison.png', dpi=300, bbox_inches='tight')
    print(f"Saved: {output_dir}/single_comparison.png")
    plt.close()

def visualize_batch_results(data, output_dir='visualizations'):
    """Create visualizations for batch comparison results"""
    os.makedirs(output_dir, exist_ok=True)

    # Convert to DataFrame
    df = pd.DataFrame(data)

    # Calculate derived metrics
    df['avg_seq_length'] = (df['seq1_length'] + df['seq2_length']) / 2
    df['time_diff_pct'] = ((df['sw_time_us'] - df['nw_time_us']) / df['nw_time_us']) * 100
    df['memory_diff_pct'] = ((df['sw_memory_kb'] - df['nw_memory_kb']) / df['nw_memory_kb']) * 100
    df['score_diff'] = df['nw_score'] - df['sw_score']

    # 1. Time vs Sequence Length
    fig, axes = plt.subplots(2, 2, figsize=(16, 12))

    axes[0, 0].scatter(df['avg_seq_length'], df['nw_time_us'],
                       s=100, alpha=0.6, label='Needleman-Wunsch', color='#3498db')
    axes[0, 0].scatter(df['avg_seq_length'], df['sw_time_us'],
                       s=100, alpha=0.6, label='Smith-Waterman', color='#e74c3c')
    axes[0, 0].set_xlabel('Average Sequence Length', fontsize=12)
    axes[0, 0].set_ylabel('Execution Time (μs)', fontsize=12)
    axes[0, 0].set_title('Time Complexity: Execution Time vs Sequence Length',
                        fontsize=14, fontweight='bold')
    axes[0, 0].legend(fontsize=10)
    axes[0, 0].grid(alpha=0.3)

    # Add trend lines
    z1 = np.polyfit(df['avg_seq_length'], df['nw_time_us'], 2)
    p1 = np.poly1d(z1)
    z2 = np.polyfit(df['avg_seq_length'], df['sw_time_us'], 2)
    p2 = np.poly1d(z2)
    x_smooth = np.linspace(df['avg_seq_length'].min(), df['avg_seq_length'].max(), 100)
    axes[0, 0].plot(x_smooth, p1(x_smooth), '--', color='#3498db', alpha=0.5, linewidth=2)
    axes[0, 0].plot(x_smooth, p2(x_smooth), '--', color='#e74c3c', alpha=0.5, linewidth=2)

    # 2. Memory vs Sequence Length
    axes[0, 1].scatter(df['avg_seq_length'], df['nw_memory_kb'],
                       s=100, alpha=0.6, label='Needleman-Wunsch', color='#3498db')
    axes[0, 1].scatter(df['avg_seq_length'], df['sw_memory_kb'],
                       s=100, alpha=0.6, label='Smith-Waterman', color='#e74c3c')
    axes[0, 1].set_xlabel('Average Sequence Length', fontsize=12)
    axes[0, 1].set_ylabel('Memory Usage (KB)', fontsize=12)
    axes[0, 1].set_title('Space Complexity: Memory Usage vs Sequence Length',
                        fontsize=14, fontweight='bold')
    axes[0, 1].legend(fontsize=10)
    axes[0, 1].grid(alpha=0.3)

    # 3. Performance Comparison Heatmap
    perf_data = df[['pair_name', 'nw_time_us', 'sw_time_us', 'nw_memory_kb', 'sw_memory_kb']].set_index('pair_name')
    perf_data.columns = ['NW Time', 'SW Time', 'NW Memory', 'SW Memory']

    # Normalize for better visualization
    perf_normalized = (perf_data - perf_data.min()) / (perf_data.max() - perf_data.min())

    sns.heatmap(perf_normalized.T, annot=False, cmap='RdYlGn_r',
                cbar_kws={'label': 'Normalized Value'}, ax=axes[1, 0])
    axes[1, 0].set_title('Performance Heatmap (Normalized)', fontsize=14, fontweight='bold')
    axes[1, 0].set_xlabel('Sequence Pairs', fontsize=12)
    axes[1, 0].set_ylabel('Metrics', fontsize=12)

    # 4. Time Efficiency Comparison
    df_sorted = df.sort_values('avg_seq_length')
    x = range(len(df_sorted))
    width = 0.35

    axes[1, 1].bar([i - width/2 for i in x], df_sorted['nw_time_us'], width,
                   label='Needleman-Wunsch', color='#3498db', alpha=0.8)
    axes[1, 1].bar([i + width/2 for i in x], df_sorted['sw_time_us'], width,
                   label='Smith-Waterman', color='#e74c3c', alpha=0.8)
    axes[1, 1].set_xlabel('Sequence Pairs (sorted by length)', fontsize=12)
    axes[1, 1].set_ylabel('Execution Time (μs)', fontsize=12)
    axes[1, 1].set_title('Time Comparison Across All Pairs', fontsize=14, fontweight='bold')
    axes[1, 1].set_xticks(x)
    axes[1, 1].set_xticklabels(df_sorted['pair_name'], rotation=45, ha='right')
    axes[1, 1].legend(fontsize=10)
    axes[1, 1].grid(axis='y', alpha=0.3)

    plt.tight_layout()
    plt.savefig(f'{output_dir}/batch_analysis.png', dpi=300, bbox_inches='tight')
    print(f"Saved: {output_dir}/batch_analysis.png")
    plt.close()

    # 5. Detailed Complexity Analysis
    fig, axes = plt.subplots(1, 2, figsize=(16, 6))

    # Log scale for better complexity visualization
    axes[0].scatter(df['avg_seq_length'], df['nw_time_us'],
                   s=100, alpha=0.6, label='Needleman-Wunsch', color='#3498db')
    axes[0].scatter(df['avg_seq_length'], df['sw_time_us'],
                   s=100, alpha=0.6, label='Smith-Waterman', color='#e74c3c')
    axes[0].set_xlabel('Average Sequence Length', fontsize=12)
    axes[0].set_ylabel('Execution Time (μs) - Log Scale', fontsize=12)
    axes[0].set_title('Time Complexity Analysis (Log Scale)', fontsize=14, fontweight='bold')
    axes[0].set_yscale('log')
    axes[0].legend(fontsize=10)
    axes[0].grid(alpha=0.3, which='both')

    # Space complexity
    axes[1].scatter(df['avg_seq_length'], df['nw_memory_kb'],
                   s=100, alpha=0.6, label='Needleman-Wunsch', color='#3498db')
    axes[1].scatter(df['avg_seq_length'], df['sw_memory_kb'],
                   s=100, alpha=0.6, label='Smith-Waterman', color='#e74c3c')
    axes[1].set_xlabel('Average Sequence Length', fontsize=12)
    axes[1].set_ylabel('Memory Usage (KB) - Log Scale', fontsize=12)
    axes[1].set_title('Space Complexity Analysis (Log Scale)', fontsize=14, fontweight='bold')
    axes[1].set_yscale('log')
    axes[1].legend(fontsize=10)
    axes[1].grid(alpha=0.3, which='both')

    plt.tight_layout()
    plt.savefig(f'{output_dir}/complexity_analysis.png', dpi=300, bbox_inches='tight')
    print(f"Saved: {output_dir}/complexity_analysis.png")
    plt.close()

    # Save summary statistics
    summary = df[['pair_name', 'avg_seq_length', 'nw_time_us', 'sw_time_us',
                  'nw_memory_kb', 'sw_memory_kb', 'nw_score', 'sw_score']].describe()
    summary.to_csv(f'{output_dir}/summary_statistics.csv')
    print(f"Saved: {output_dir}/summary_statistics.csv")

def main():
    if len(sys.argv) < 2:
        print("Usage: python visualization.py <json_file>")
        print("Example: python visualization.py batch_results.json")
        sys.exit(1)

    filename = sys.argv[1]

    if not os.path.exists(filename):
        print(f"Error: File '{filename}' not found!")
        sys.exit(1)

    data = load_data(filename)

    # Check if it's single comparison or batch results
    if isinstance(data, dict):
        print("Visualizing single comparison...")
        visualize_single_comparison(data)
    elif isinstance(data, list):
        print("Visualizing batch results...")
        visualize_batch_results(data)
    else:
        print("Error: Unrecognized data format!")
        sys.exit(1)

    print("\nVisualization complete! Launching graph viewer...")

    # AUTO-OPEN THE GUI
    try:
        import subprocess
        subprocess.Popen([sys.executable, "display.py"])
    except Exception as e:
        print(f"Failed to launch GUI viewer automatically: {e}")
        print("You can manually run: python display.py")

if __name__ == "__main__":
    main()
