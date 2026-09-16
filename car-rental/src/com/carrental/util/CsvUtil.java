package com.carrental.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CsvUtil {

    /**
     * Reads the file, skipping the header row.
     * Fields are parsed according to RFC 4180: a value that contains a comma,
     * a quote or a line break is written between double quotes, and an inner
     * quote is doubled. The old version used line.split(",") which silently
     * broke every row that contained a comma.
     */
    public static List<String[]> readAll(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("⚠ CSV file not found, starting with an empty data set: " + file.getAbsolutePath());
            return data;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                data.add(parseLine(line));
            }
        }
        return data;
    }

    public static void writeAll(String filePath, String header, List<String> lines) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            bw.write(header);
            bw.newLine();
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }

    /** Builds a CSV row, escaping every field that needs it. */
    public static String toLine(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    public static String escape(String value) {
        if (value == null) return "";
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private static String[] parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }
}
