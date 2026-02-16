# Wir nutzen die slim-Variante für einen kleineren Footprint auf dem Pi
FROM python:3.11-slim
LABEL authors="PoebelPogge"
# 1. System-Abhängigkeiten installieren
# libusb-1.0-0 ist zwingend erforderlich für python-escpos
RUN apt-get update && apt-get install -y \
    libusb-1.0-0 \
    && rm -rf /var/lib/apt/lists/*

# 2. Arbeitsverzeichnis festlegen
WORKDIR /app

# 3. Python-Abhängigkeiten installieren
# Wir kopieren erst nur die requirements.txt, um Docker-Caching zu nutzen
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 4. Den Quellcode kopieren
COPY main.py .

# 5. Den Flask-Port nach außen hin öffnen
EXPOSE 5001

# 6. Den Server starten
CMD ["python", "main.py"]