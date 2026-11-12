# Bidwar Home TV

App de pantalla de bienvenida para teles con Android TV, Google TV y Fire TV.
Abre a pantalla completa la pantalla de cada alojamiento (conectada a Smoobu a través de Google Apps Script),
arranca sola al encender y se recupera sola si se va internet.

## Crear o actualizar la app

1. Pestaña **Actions** → **Crear app Bidwar Home** → **Run workflow**.
2. Pega el enlace de la aplicación web de Google Apps Script (el que termina en `/exec`) y pulsa **Run workflow**.
3. En unos 5 minutos aparece en **Releases** el archivo `BidwarHome.apk`.

Enlace de descarga directa (siempre la última versión):
`https://github.com/USUARIO/REPOSITORIO/releases/latest/download/BidwarHome.apk`

## En la tele

- Primera vez: escribe el código de la tele de ese alojamiento (columna «Clave de la tele» de la hoja).
- Para volver a la configuración: en el mando pulsa **arriba, arriba, abajo, abajo**.
- El botón Atrás no cierra la pantalla, para que los huéspedes no la quiten sin querer.

## Firma

`app/bidwar.keystore` firma todas las versiones con la misma clave, para que las actualizaciones
se instalen encima sin desinstalar. No lo borres ni lo cambies.
