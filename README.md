# 🗃️ DBF Driver — Reactive Reader/Writer for `.dbf`

![Maven Central](https://img.shields.io/maven-central/v/io.github.zazalng/DBFReader.svg?label=Maven%20Central)
![Java](https://img.shields.io/badge/JDK-8%2B-green)
![License](https://img.shields.io/github/license/Zazalng/DBFReader)
[![Javadoc](https://img.shields.io/badge/javadoc-latest-blue.svg)](https://<user>.github.io/<repo>/latest/)

**DBF Driver** is the 2.x evolution of the original reader: a zero-dependency Java library that handles both reading and writing `.DBF` (dBASE/FoxPro/Clipper) files using modern NIO patterns.  
It’s built for **speed, correctness, and centralised access**, supporting `DBF III–VII` variants, encoding overrides, and concurrent service usage.


---

## ✨ Features

- 🧩 Reads & writes `.dbf` files (dBASE III/IV, Visual FoxPro, Clipper)
- ⚙️ Works on classic LTS Java (8+) and the latest releases
- 💾 Full coverage of core field types, now with mirror encoding support
- 🕓 Accurate date/time + binary column handling for read/write
- 🌐 Automatic LDID detection with optional charset overrides
- 🔁 Long-lived `DBFSession` instances that keep channels open & reload safely
- 🔐 File and in-memory locking helpers for centralised API deployments
- ⚙️ `DBFWriter` facade and `DBFService` orchestrator for high-level usage
- 🚀 Zero external dependencies

---

## 📦 Installation

Add this to your **Maven** project:

```xml
<dependency>
    <groupId>io.github.zazalng</groupId>
    <artifactId>DBFReader</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

Or using **Gradle**:

```groovy
implementation 'io.github.zazalng:DBFReader:2.0.0-SNAPSHOT'
```

---

## 💡 Example Usage

### Snapshot Reader

```java
import io.github.zazalng.DBF;

import java.nio.file.Path;

public class Example {
    public static void main(String[] args) throws Exception {
        Path dbfFile = Path.of("data/sample.dbf");
        try (DBF dbf = new DBF(dbfFile)) {
            System.out.println("Version: " + dbf.getVersion());
            System.out.println("Fields: " + dbf.getFields().size());
            System.out.println("Records: " + dbf.getRecords().size());

            for (var row : dbf.getRecords()) {
                System.out.println(row.asMap());
            }
        }
    }
}
```

Output:

```
Version: dBASE IV
Fields: 5
Records: 120
{ID=1, NAME="Alice", SALARY=53250.00, ACTIVE=true, JOINED=2024-01-03}
```

### Live Session & Writer

```java
import io.github.zazalng.DBFWriter;

import java.nio.file.Path;
import java.util.Map;

public class AppendExample {
    public static void main(String[] args) throws Exception {
        Path dbfFile = Path.of("data/sample.dbf");
        try (DBFWriter writer = DBFWriter.open(dbfFile)) {
            writer.append(Map.of(
                    "ID", 121,
                    "NAME", "Bob",
                    "SALARY", 48000,
                    "ACTIVE", true
            ));
        }
    }
}
```

### Centralised Service (pseudo)

```java
DBFService service = new DBFService();

// read in a web request handler
List<DBFRow> rows = service.readRecords(Path.of("dbf/customers.dbf"));

// append in another handler
service.append(Path.of("dbf/customers.dbf"), Map.of("NAME", "Charlie", "ACTIVE", true));
```

---

## 🧠 Design Highlights

- Uses `DBFSession` to keep channels open, reload on demand, and manage file locks
- Reader pipeline mirrors writer pipeline for lossless round-trip of values
- Clean, modular structure (`DBF`, `DBFSession`, `DBFWriter`, `DBFHeader`, `DBFField`, `DBFRow`, `DBFUtils`)
- Optional `DBFService` orchestrates multi-threaded read/write access with read/write locks
- Full charset support via LDID → `Charset` mapping, with overrides through `DBFEncoding`
- No dependencies and minimal checked exceptions for simple integration

---

## 📚 API Overview

| Class         | Description                                          |
| ------------- | ---------------------------------------------------- |
| `DBF`         | Snapshot façade around a `DBFSession`.                |
| `DBFSession`  | Stateful reader/writer keeping channels open.        |
| `DBFWriter`   | Convenience façade for write operations.            |
| `DBFService`  | Orchestrates sessions with concurrency controls.    |
| `DBFHeader`   | Parses header metadata and field descriptors.        |
| `DBFField`    | Column definition with decode + encode helpers.      |
| `DBFRow`      | Represents one record as an immutable map.          |
| `DBFUtils`    | Utility for parsing/formatting date/time/numeric values. |
| `DBFVersion`  | Enum for known dBASE versions.                       |
| `DBFEncoding` | Optional helper for charset override.                |

---

## 🧩 Supported Field Types

| DBF Type         | Java Type       | Example               |
| ---------------- | --------------- | --------------------- |
| `C` (Character)  | `String`        | `"Hello"`             |
| `N` (Numeric)    | `BigDecimal`    | `12345.67`            |
| `F` (Float)      | `BigDecimal`    | `3.14`                |
| `D` (Date)       | `LocalDate`     | `2024-05-21`          |
| `T` (DateTime)   | `LocalDateTime` | `2024-05-21T14:20:00` |
| `L` (Logical)    | `Boolean`       | `true` / `false`      |
| `I` (Integer)    | `Integer`       | `42`                  |
| `B` (Double)     | `Double`        | `3.1415926`           |
| `M` (Memo/Blob)* | `byte[]`        | *(reserved)*          |

---

## 🧾 License

This project is licensed under the **Apache-2.0 License**

---

## 🤝 Contributing

Pull requests are welcome!
If you encounter a file that doesn’t parse correctly, please open an issue and attach:

* A short description (origin software, e.g. Visual FoxPro / Clipper)
* The first 32 bytes of the header (in hex)
* The full stack trace if applicable

---

## 🔗 Links

* 📦 [Maven Central](https://search.maven.org/artifact/io.github.zazalng/DBFReader)
* 🌐 [GitHub Repository](https://github.com/Zazalng/DBFReader)
* 💬 Author: [@zazalng](https://github.com/Zazalng)

---

### ❤️ Built for clarity, reliability, and open data preservation.