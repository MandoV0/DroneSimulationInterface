package utils;

import java.io.IOException;
import java.util.logging.*;

public class RootLogger {
    public static void init() throws IOException {
        // If append is false, then the file gets reset everytime the App gets started
        FileHandler fh = new FileHandler("logs/dsi_logs.txt", false);
        fh.setFormatter(new SimpleFormatter());

        Logger rootLogger = Logger.getLogger("");
        rootLogger.addHandler(fh);

        // Stops console logging
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof ConsoleHandler) {
                rootLogger.removeHandler(handler);
            }
        }

    }
}
