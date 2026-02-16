import logging
from escpos.printer import Usb
from flask import Flask, request, jsonify
from datetime import datetime

app = Flask(__name__)

logging.basicConfig(
    level=logging.DEBUG,  # Das kleinste Level, das noch angezeigt wird
    format='%(asctime)s - %(levelname)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)

app.logger.setLevel(logging.DEBUG)

def print_todo(data):
    name = data.get('listName','Unknown')
    summary = data.get('summary','')

    raw_date = data.get('dueDateTime')
    due_date = None
    display_date = ""

    if raw_date is not None:
        due_date = datetime.fromisoformat(raw_date)
        display_date = due_date.strftime("%d.%m.%Y %H:%M")

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
        if due_date is not None:
            p.text("================================\n")
            p.text("Gedruckt am: " + display_date + "\n")
            p.text("================================\n")
        p.cut()
        logging.info("Todo was printed successfully!")
        app.logger.info("Todo was printed successfully!")
        return True, "Todo was printed successfully."
    except Exception as e:
        logging.error(f"Unable to print todo, see details: {str(e)}")
        app.logger.error(f"Unable to print todo, see details: {str(e)}")
        return False, f"An error occurred: {str(e)}"

@app.route('/print', methods=['POST'])
def handle_print():
    # 1. Daten vom Request holen
    data = request.get_json()

    if not data:
        return jsonify({"status": "error", "message": "Kein JSON empfangen"}), 400

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