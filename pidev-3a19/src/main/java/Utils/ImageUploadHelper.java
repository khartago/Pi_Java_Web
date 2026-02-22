package Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilitaire pour l'upload d'images : dossier de base, création, copie avec nom unique,
 * validation extension et taille.
 */
public class ImageUploadHelper {

    private static final String UPLOADS_BASE = "uploads";
    private static final String PROBLEMES_SUBDIR = "problemes";
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 Mo
    private static final int MAX_IMAGES = 5;
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));

    /**
     * Retourne le chemin du dossier de base des uploads (à la racine du projet ou à côté du JAR).
     */
    public static Path getBaseUploadDir() {
        String userDir = System.getProperty("user.dir");
        return Paths.get(userDir).resolve(UPLOADS_BASE).resolve(PROBLEMES_SUBDIR);
    }

    /**
     * Crée le dossier uploads/problemes s'il n'existe pas.
     */
    public static void ensureUploadDirExists() throws IOException {
        Path dir = getBaseUploadDir();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    /**
     * Vérifie que l'extension du fichier est autorisée (jpg, jpeg, png).
     */
    public static boolean isValidExtension(String filename) {
        if (filename == null || filename.isEmpty()) return false;
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return false;
        String ext = filename.substring(dot + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    /**
     * Vérifie que la taille du fichier ne dépasse pas 5 Mo.
     */
    public static boolean isValidSize(long sizeBytes) {
        return sizeBytes > 0 && sizeBytes <= MAX_FILE_SIZE_BYTES;
    }

    public static long getMaxFileSizeBytes() {
        return MAX_FILE_SIZE_BYTES;
    }

    public static int getMaxImages() {
        return MAX_IMAGES;
    }

    /**
     * Copie un fichier vers le dossier uploads/problemes avec le nom {idProbleme}_{timestamp}_{index}.{ext}.
     * Crée le dossier si nécessaire.
     *
     * @param sourceFile chemin du fichier source
     * @param idProbleme ID du problème
     * @param index      index de l'image (0, 1, ...)
     * @return le chemin relatif (ex. "problemes/1_1739123456_0.jpg") pour stockage en BDD
     */
    public static String copyWithUniqueName(Path sourceFile, int idProbleme, int index) throws IOException {
        ensureUploadDirExists();
        String originalName = sourceFile.getFileName().toString();
        int dot = originalName.lastIndexOf('.');
        String ext = dot >= 0 ? originalName.substring(dot + 1).toLowerCase() : "jpg";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            ext = "jpg";
        }
        long timestamp = System.currentTimeMillis() / 1000;
        String fileName = idProbleme + "_" + timestamp + "_" + index + "." + ext;
        Path targetDir = getBaseUploadDir();
        Path targetFile = targetDir.resolve(fileName);
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        return PROBLEMES_SUBDIR + "/" + fileName;
    }
}
