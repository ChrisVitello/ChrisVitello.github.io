package Game_folder;

import java.io.*;
import java.util.*;

/*
 * ============================================================
 * CSV LOADER
 * ============================================================
 * Responsible for:
 * - Reading CSV file
 * - Storing headers (column names)
 * - Storing all rows as maps
 */
public class CsvLoader {

    public static List<Map<String, String>> data = new ArrayList<>();
    public static List<String> headers = new ArrayList<>();

    public static void load(String filePath) {
        data.clear();
        headers.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            // First row = headers
            String headerLine = br.readLine();
            String[] headerArray = headerLine.split(",");

            for (String h : headerArray) {
                headers.add(h.trim());
            }

            // Read all rows
            String line;
            while ((line = br.readLine()) != null) {

                String[] values = line.split(",");

                Map<String, String> entry = new HashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    if (i < values.length) {
                        entry.put(headers.get(i), values[i].trim());
                    } else {
                        entry.put(headers.get(i), "");
                    }
                }

                data.add(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}