import json
import sys
from escpos.printer import Usb
from flask import Flask, request, jsonify
from datetime import datetime

app = Flask(__name__)

def print_todo(data):
    name = data.get('listName','Unknown')
    summary = data.get('summary','')
    due_date = datetime.fromisoformat(data.get('dueDate',''))

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
        p.text(f"{summary}\n\n")
        p.text("================================\n")
        p.set(bold=True)
        p.text("Scan when done:\n")
        p.set(bold=False)
        p.qr("https://dein-kalender-link.de", size=10, center=True)
        p.text("================================\n")
        p.text("Gedruckt am: " + due_date.strftime("%d.%m.%Y %H:%M") + "\n")
        p.text("================================\n")
        p.cut()
    except Exception as e:
        print(f"Druckfehler: {e}", file=sys.stderr)
        sys.exit(1)

@app.route('/print', methods=['POST'])
def handle_print():
    # 1. Daten vom Request holen
    data = request.get_json()

    if not data:
        return jsonify({"error": "Kein JSON empfangen"}), 400

    # 2. Drucken ausführen
    success, message = print_todo(data)

    # 3. Ergebnis zurückgeben
    if success:
        return jsonify({"status": "success", "message": message}), 200
    else:
        # Wenn der Drucker nicht gefunden wird, schicken wir einen 500er Fehler
        return jsonify({"status": "error", "message": message}), 500

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5001)