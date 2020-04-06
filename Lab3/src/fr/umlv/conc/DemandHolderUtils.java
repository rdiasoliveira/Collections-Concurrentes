package fr.umlv.conc;

import java.nio.file.Path;

public class DemandHolderUtils {

    private static class HomePath {
        static final Path homePath = Path.of(System.getenv("HOME"));
    }

    public static Path Home() {
        return HomePath.homePath;
    }

}
