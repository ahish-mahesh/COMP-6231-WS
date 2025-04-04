package org.example;

import java.io.*;
import java.time.LocalDateTime;

/**
 *
 * @author ahishmahesh
 */
public class Logger {

    private String filePath;
    private final String baseFilePath = "Logs//";
    private String serverName;

    public Logger(String serverName) {
        this.serverName = serverName;
        this.filePath = baseFilePath + File.separator + serverName + ".log";

        try {
            File f = new File(baseFilePath);
            if (!f.exists()) {
                f.mkdir();
            }

            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Initialize the log file with their respective headers
            bufferedWriter.write(this.serverName + "\n");

            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing to file '" + filePath + "'");
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void log(String message) {
        try {
            FileWriter fileWriter = new FileWriter(filePath, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(LocalDateTime.now() + "\t" + message);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            System.out.println("Error writing to file '" + filePath + "'");
        }
    }
}
