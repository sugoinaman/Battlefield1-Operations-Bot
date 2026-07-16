package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Loads configuration and atomically persists rotated EA cookies.
 */
public final class ConfigurationStore {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final Path path;

    public ConfigurationStore(Path path) {
        this.path = Objects.requireNonNull(path).toAbsolutePath();
    }

    public Configuration load() throws IOException {
        return OBJECT_MAPPER.readValue(path.toFile(), Configuration.class);
    }

    // updating ea credentials when sid expires.
    public synchronized Configuration updateEaCredentials(Configuration.Ea credentials)
            throws IOException {

        Objects.requireNonNull(credentials);
        if (!credentials.hasSid()) {
            throw new IllegalArgumentException("A refreshed EA SID must not be blank");
        }

        Configuration updated = load().withEaCredentials(credentials);
        writeAtomically(updated);
        return updated;
    }

    private void writeAtomically(Configuration configuration) throws IOException {
        Path directory = path.getParent();
        Files.createDirectories(directory);
        Path temporaryFile = Files.createTempFile(directory, "bf1-config-", ".tmp");

        try {
            OBJECT_MAPPER.writeValue(temporaryFile.toFile(), configuration);
            try {
                Files.move(
                        temporaryFile,
                        path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}
