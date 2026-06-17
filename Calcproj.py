import tkinter as tk
from tkinter import ttk
import math

# --------------------------
# FORMULA DATABASE (24 TOTAL)
# --------------------------
formulas = {

    "Algebra": {
        "Slope": (["x1","y1","x2","y2"], lambda v: (v["y2"]-v["y1"])/(v["x2"]-v["x1"])),
        "Quadratic Discriminant": (["a","b","c"], lambda v: v["b"]**2 - 4*v["a"]*v["c"]),
        "Simple Interest": (["p","r","t"], lambda v: v["p"]*v["r"]*v["t"]),
        "Distance Formula": (["x1","y1","x2","y2"], lambda v: math.sqrt((v["x2"]-v["x1"])**2 + (v["y2"]-v["y1"])**2)),
        "Average": (["a","b","c"], lambda v: (v["a"]+v["b"]+v["c"])/3),
        "Exponent": (["base","power"], lambda v: v["base"]**v["power"])
    },

    "Geometry": {
        "Circle Area": (["radius"], lambda v: math.pi*v["radius"]**2),
        "Circle Circumference": (["radius"], lambda v: 2*math.pi*v["radius"]),
        "Rectangle Area": (["length","width"], lambda v: v["length"]*v["width"]),
        "Rectangle Perimeter": (["length","width"], lambda v: 2*(v["length"]+v["width"])),
        "Triangle Area": (["base","height"], lambda v: 0.5*v["base"]*v["height"]),
        "Pythagorean": (["a","b"], lambda v: math.sqrt(v["a"]**2 + v["b"]**2))
    },

    "Trigonometry": {
        "sin(x)": (["angle_deg"], lambda v: math.sin(math.radians(v["angle_deg"]))),
        "cos(x)": (["angle_deg"], lambda v: math.cos(math.radians(v["angle_deg"]))),
        "tan(x)": (["angle_deg"], lambda v: math.tan(math.radians(v["angle_deg"]))),
        "SOH (find opp)": (["hyp","angle"], lambda v: v["hyp"]*math.sin(math.radians(v["angle"]))),
        "CAH (find adj)": (["hyp","angle"], lambda v: v["hyp"]*math.cos(math.radians(v["angle"]))),
        "TOA (find opp)": (["adj","angle"], lambda v: v["adj"]*math.tan(math.radians(v["angle"])))
    },

    "Physics": {
        "Force (F=ma)": (["mass","acc"], lambda v: v["mass"]*v["acc"]),
        "Speed": (["distance","time"], lambda v: v["distance"]/v["time"]),
        "Work": (["force","distance"], lambda v: v["force"]*v["distance"]),
        "Kinetic Energy": (["mass","velocity"], lambda v: 0.5*v["mass"]*v["velocity"]**2),
        "Momentum": (["mass","velocity"], lambda v: v["mass"]*v["velocity"]),
        "Density": (["mass","volume"], lambda v: v["mass"]/v["volume"])
    }
}

# --------------------------
# MAIN APP CLASS
# --------------------------
class CalculatorApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Formula Calculator")
        self.root.geometry("500x500")
        self.root.configure(bg="#1e1e2f")

        self.style = ttk.Style()
        self.style.theme_use("clam")

        self.style.configure("TButton", font=("Segoe UI", 10), padding=6)
        self.style.configure("TLabel", background="#1e1e2f", foreground="white", font=("Segoe UI", 11))

        self.frame = tk.Frame(root, bg="#1e1e2f")
        self.frame.pack(fill="both", expand=True)

        self.entries = {}

        self.show_subjects()

    # --------------------------
    def clear(self):
        for widget in self.frame.winfo_children():
            widget.destroy()

    # --------------------------
    def show_subjects(self):
        self.clear()

        tk.Label(self.frame, text="Formula Calculator", font=("Segoe UI", 20, "bold"), bg="#1e1e2f", fg="white").pack(pady=20)

        for subject in formulas:
            ttk.Button(self.frame, text=subject,
                       command=lambda s=subject: self.show_formulas(s)).pack(pady=8)

    # --------------------------
    def show_formulas(self, subject):
        self.clear()

        tk.Label(self.frame, text=subject, font=("Segoe UI", 16, "bold"), bg="#1e1e2f", fg="white").pack(pady=15)

        for f in formulas[subject]:
            ttk.Button(self.frame, text=f,
                       command=lambda f=f: self.show_inputs(subject, f)).pack(pady=6)

        ttk.Button(self.frame, text="Back", command=self.show_subjects).pack(pady=15)

    # --------------------------
    def show_inputs(self, subject, formula_name):
        self.clear()
        self.entries.clear()

        vars_list, func = formulas[subject][formula_name]

        tk.Label(self.frame, text=formula_name, font=("Segoe UI", 14, "bold"), bg="#1e1e2f", fg="white").pack(pady=15)

        for var in vars_list:
            row = tk.Frame(self.frame, bg="#1e1e2f")
            row.pack(pady=5)

            tk.Label(row, text=var+":", width=10, anchor="w", bg="#1e1e2f", fg="white").pack(side="left")
            entry = ttk.Entry(row)
            entry.pack(side="right", padx=10)

            self.entries[var] = entry

        result_label = tk.Label(self.frame, text="Result: ", font=("Segoe UI", 12), bg="#1e1e2f", fg="#00ffcc")
        result_label.pack(pady=20)

        def calculate():
            try:
                values = {var: float(self.entries[var].get()) for var in self.entries}
                result = func(values)
                result_label.config(text=f"Result: {round(result, 4)}")
            except:
                result_label.config(text="Invalid Input")

        ttk.Button(self.frame, text="Calculate", command=calculate).pack(pady=5)
        ttk.Button(self.frame, text="Back", command=lambda: self.show_formulas(subject)).pack(pady=5)

# --------------------------
# RUN PROGRAM
# --------------------------
root = tk.Tk()
app = CalculatorApp(root)
root.mainloop()
