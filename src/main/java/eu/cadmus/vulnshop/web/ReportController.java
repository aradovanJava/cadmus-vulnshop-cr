package eu.cadmus.vulnshop.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * ============================================================================
 *  A05:2025 - INJECTION (OS command)   +   A01 (path traversal)
 *  A10:2025 - MISHANDLING OF EXCEPTIONAL CONDITIONS
 * ============================================================================
 *
 * The "export a report" feature. Three weaknesses, all of them the result of
 * reaching for a shell because it was faster than writing Java.
 *
 * MITRE ATT&CK: T1059 - Command and Scripting Interpreter.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final Path REPORT_DIR = Path.of(System.getProperty("java.io.tmpdir"), "vulnshop-reports");

    /**
     * A05 - OS COMMAND INJECTION. The caller's value is concatenated into a
     * shell command line, so shell metacharacters are interpreted as syntax.
     *
     * The correct fix is not escaping. It is not using a shell: build the file
     * listing in Java, or pass an argument vector to ProcessBuilder with no
     * shell in the path. See docs/FIXES.md.
     */
    @GetMapping("/list")
    public Map<String, Object> listReports(@RequestParam String pattern) {
        StringBuilder output = new StringBuilder();
        try {
            Files.createDirectories(REPORT_DIR);
            boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
            String command = windows
                    ? "dir /b " + REPORT_DIR + "\\" + pattern
                    : "ls -1 " + REPORT_DIR + "/" + pattern;

            ProcessBuilder builder = windows
                    ? new ProcessBuilder("cmd.exe", "/c", command)
                    : new ProcessBuilder("sh", "-c", command);
            builder.redirectErrorStream(true);

            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }
            process.waitFor();
        } catch (Exception e) {
            // A10 - the full stack trace is handed to the caller: internal paths,
            //       class names and framework versions, free of charge.
            return Map.of("ok", false, "trace", stackTraceOf(e));
        }
        return Map.of("ok", true, "output", output.toString());
    }

    /**
     * A01 - PATH TRAVERSAL. The filename is concatenated onto a base directory
     * with no canonicalisation and no containment check, so ../ escapes it.
     *
     * A10 - the FileInputStream is opened outside the try-with-resources scope
     *       used below, so an exception thrown while reading leaks the handle.
     *       Repeated failures exhaust the descriptor table: a denial of service
     *       reached through the failure path, which is exactly the kind of
     *       scenario the new A10 category was created to cover.
     */
    @GetMapping("/download")
    public Map<String, Object> download(@RequestParam String name) throws Exception {
        FileInputStream in = new FileInputStream(REPORT_DIR.resolve(name).toString());
        byte[] content = in.readAllBytes();
        in.close();   // never reached if readAllBytes throws
        return Map.of("name", name, "content", new String(content, StandardCharsets.UTF_8));
    }

    private static String stackTraceOf(Exception e) {
        StringBuilder sb = new StringBuilder(e.toString()).append('\n');
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element).append('\n');
        }
        return sb.toString();
    }
}
