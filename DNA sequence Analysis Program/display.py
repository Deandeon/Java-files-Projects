import tkinter as tk
from tkinter import ttk
from PIL import Image, ImageTk
import os
import platform
import subprocess

class GraphViewer:
    def __init__(self, root):
        self.root = root
        self.root.title("DNA Sequence Alignment - Visualization Results")
        self.root.geometry("1200x800")

        # Keep image references
        self.images = {}

        # Main layout frame
        main_frame = ttk.Frame(root, padding=10)
        main_frame.grid(row=0, column=0, sticky="nsew")

        root.rowconfigure(0, weight=1)
        root.columnconfigure(0, weight=1)
        main_frame.rowconfigure(1, weight=1)
        main_frame.columnconfigure(0, weight=1)

        title = ttk.Label(main_frame, text="📊 Visualization Results",
                          font=("Arial", 16, "bold"))
        title.grid(row=0, column=0, pady=10)

        # Notebook (tabs)
        self.notebook = ttk.Notebook(main_frame)
        self.notebook.grid(row=1, column=0, sticky="nsew")

        # Load visualization files
        vis_dir = "visualizations"
        image_files = {
            "Batch Analysis": os.path.join(vis_dir, "batch_analysis.png"),
            "Complexity Analysis": os.path.join(vis_dir, "complexity_analysis.png"),
            "Single Comparison": os.path.join(vis_dir, "single_comparison.png")
        }

        # Add image tabs
        for tab_name, path in image_files.items():
            if os.path.exists(path):
                self.add_image_tab(tab_name, path)

        # Add CSV tab
        csv_path = os.path.join(vis_dir, "summary_statistics.csv")
        if os.path.exists(csv_path):
            self.add_statistics_tab(csv_path)

        # Status bar
        self.status = ttk.Label(main_frame, text="Ready", anchor="w", relief=tk.SUNKEN)
        self.status.grid(row=2, column=0, sticky="ew", pady=(5, 0))

        # Buttons
        btn_frame = ttk.Frame(main_frame)
        btn_frame.grid(row=3, column=0, pady=10)

        ttk.Button(btn_frame, text="🔄 Refresh", command=self.refresh).grid(row=0, column=0, padx=5)
        ttk.Button(btn_frame, text="📁 Open Folder", command=self.open_folder).grid(row=0, column=1, padx=5)
        ttk.Button(btn_frame, text="✖ Close", command=self.root.quit).grid(row=0, column=2, padx=5)

        # Update status
        tab_count = self.notebook.index("end")
        if tab_count > 0:
            self.status.config(text=f"Loaded {tab_count} visualization(s)")
        else:
            self.status.config(text="⚠ No visualization images found.")

    # --------------------------------------------------------------------

    def add_image_tab(self, name, image_path):
        """Add a tab containing a scrollable image."""
        frame = ttk.Frame(self.notebook)
        self.notebook.add(frame, text=name)

        canvas = tk.Canvas(frame, bg="white")
        v_scroll = ttk.Scrollbar(frame, orient=tk.VERTICAL, command=canvas.yview)
        h_scroll = ttk.Scrollbar(frame, orient=tk.HORIZONTAL, command=canvas.xview)
        canvas.configure(yscrollcommand=v_scroll.set, xscrollcommand=h_scroll.set)

        canvas.grid(row=0, column=0, sticky="nsew")
        v_scroll.grid(row=0, column=1, sticky="ns")
        h_scroll.grid(row=1, column=0, sticky="ew")

        frame.rowconfigure(0, weight=1)
        frame.columnconfigure(0, weight=1)

        try:
            img = Image.open(image_path)

            # Auto-resize if very large
            max_width = 1400
            if img.width > max_width:
                scale = max_width / img.width
                img = img.resize((max_width, int(img.height * scale)), Image.Resampling.LANCZOS)

            photo = ImageTk.PhotoImage(img)
            self.images[name] = photo

            canvas.create_image(0, 0, anchor="nw", image=photo)
            canvas.config(scrollregion=canvas.bbox("all"))

        except Exception as e:
            ttk.Label(frame, text=f"Error loading {image_path}:\n{e}").grid(padx=10, pady=10)

    # --------------------------------------------------------------------

    def add_statistics_tab(self, csv_path):
        """Add tab containing CSV text."""
        frame = ttk.Frame(self.notebook)
        self.notebook.add(frame, text="📈 Statistics")

        frame.rowconfigure(0, weight=1)
        frame.columnconfigure(0, weight=1)

        text = tk.Text(frame, wrap=tk.NONE, font=("Courier", 10))
        v_scroll = ttk.Scrollbar(frame, orient=tk.VERTICAL, command=text.yview)
        h_scroll = ttk.Scrollbar(frame, orient=tk.HORIZONTAL, command=text.xview)
        text.config(yscrollcommand=v_scroll.set, xscrollcommand=h_scroll.set)

        text.grid(row=0, column=0, sticky="nsew")
        v_scroll.grid(row=0, column=1, sticky="ns")
        h_scroll.grid(row=1, column=0, sticky="ew")

        try:
            with open(csv_path, "r") as f:
                content = f.read()
            text.insert("1.0", content)
            text.config(state="disabled")
        except Exception as e:
            text.insert("1.0", f"Error loading CSV:\n{e}")
            text.config(state="disabled")

    # --------------------------------------------------------------------

    def refresh(self):
        """Reload the GUI completely."""
        self.root.destroy()
        main()

    # --------------------------------------------------------------------

    def open_folder(self):
        """Open the visualizations directory."""
        vis_dir = os.path.abspath("visualizations")

        if not os.path.exists(vis_dir):
            self.status.config(text="Visualization folder not found.")
            return

        try:
            system = platform.system()
            if system == "Windows":
                os.startfile(vis_dir)
            elif system == "Darwin":
                subprocess.run(["open", vis_dir])
            else:  # Linux
                subprocess.run(["xdg-open", vis_dir])

            self.status.config(text=f"Opened folder: {vis_dir}")
        except Exception as e:
            self.status.config(text=f"Error: {e}")

# ------------------------------------------------------------------------

def main():
    if not os.path.exists("visualizations"):
        print("Error: 'visualizations' folder does not exist.")
        print("Run: python visualization.py batch_results.json")
        return

    root = tk.Tk()
    app = GraphViewer(root)
    root.mainloop()

# ------------------------------------------------------------------------

if __name__ == "__main__":
    main()
