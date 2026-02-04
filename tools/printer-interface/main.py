import sys
from escpos.printer import Usb

def print_todo(name, title):
    try:
        p = Usb(0x0519, 0x000b)
        p.profile.media_width_pixel = 384
        p.set(align='center', font='a', width=1, height=1)
        p.set(bold=True)
        p.text("\n--- NEUES TODO ---\n")
        p.set(bold=False)
        p.text("--- List: ")
        p.text(f"{name}")
        p.text(" ---\n\n")
        p.text(f"{title}\n\n")
        p.text("================================\n")
        p.set(bold=True)
        p.text("Scan when done:\n")
        p.set(bold=False)
        p.qr("https://dein-kalender-link.de", size=10, center=True)
        p.text("================================\n")
        p.text("Gedruckt am: 03.02.2026\n")
        p.text("================================\n")
        p.cut()
    except Exception as e:
        print(f"Druckfehler: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    # Wenn ein Argument übergeben wurde (von Java), drucken wir es
    if len(sys.argv) > 1:
        task_title = sys.argv[1]
        list_name = sys.argv[2]
        print_todo(list_name, task_title)
    else:
        print("Kein Titel zum Drucken empfangen.")