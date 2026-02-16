# 1. Build-Stage: Node.js zum Kompilieren nutzen
FROM node:20-alpine AS build
LABEL authors="PoebelPogge"
WORKDIR /app

# Nur Abhängigkeiten kopieren (für besseres Caching)
COPY package*.json ./
RUN npm install

# Restlichen Code kopieren und bauen
COPY . .
RUN npm run build -- --configuration production

# 2. Runtime-Stage: Schlanker Webserver für die fertigen Dateien
FROM nginx:alpine
LABEL authors="PoebelPogge"

# Die gebauten Dateien aus der ersten Stage kopieren
# WICHTIG: Ersetze 'DEIN_PROJEKTNAME' durch den Namen aus deiner angular.json
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/task2ink/browser /usr/share/nginx/html

# Port 80 freigeben
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
