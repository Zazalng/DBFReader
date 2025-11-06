package worldstandard.group;

import worldstandard.group.contracts.DBFVersion;
import worldstandard.group.entity.DBFField;
import worldstandard.group.entity.DBFHeader;
import worldstandard.group.entity.DBFRow;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;

/**
 * Minimal DBF reader focused on:
 * - read header & field descriptors
 * - read all records as a snapshot at construction and on reload()
 * - support up to 60 charsets possible
 *
 * Limitations / notes:
 * - Memo fields (.dbt/.fpt/.dbt-like memo) are not parsed here (placeholder).
 * - Visual FoxPro-specific binary types, timestamps and some Level 7 features are partially unsupported.
 * - This implementation aims to be conservative and extensible.
 */
public class DBF {
    private final Path path;
    private DBFHeader header;
    private List<DBFRow> records = List.of();

    public DBF(Path path) throws IOException {
        this.path = Objects.requireNonNull(path);
        load();
    }

    public void reload() throws IOException { load(); }

    private void load() throws IOException {
        try (var channel = Files.newByteChannel(path)) {
            header = new DBFHeader(channel);
            records = header.readRecords(channel);
        }
    }

    public DBFHeader getHeader() { return header; }
    public List<DBFField> getFields() { return header.fields(); }
    public List<DBFRow> getRecords() { return records; }
    public DBFVersion getVersion() { return header.version(); }
}