package org.example.servermanager.enums;

public enum TipoFuenteCatalogo {
    ARCHIVO,        // PDF o Excel subido localmente
    DRIVE_PDF,      // PDF desde Google Drive
    GOOGLE_SHEETS   // Google Sheets (se lee como CSV)
}
