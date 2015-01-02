package com.ryancarrigan.chatman;

import java.io.*;

/**
 * Created by Suave Peanut on 2015.1.1.
 */
public class LogParser {

    private final String fileName;

    LogParser(final String fileName) {
        this.fileName = fileName;
    }

    private void load() {
        BufferedReader reader = null;
        String line;
        try {
            final File file = new File(fileName);
            reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
