package fr.umlv.conc;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;

public class VarHandleUtils {

    private static Path HOME;
    private final static VarHandle HOME_HANDLE;
    private final static Object lock = new Object();

    static {
        try {
            var lookup = MethodHandles.lookup();
            HOME_HANDLE = lookup.findStaticVarHandle(VarHandleUtils.class, "HOME", Path.class);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    public static Path getHome() {
        var home = (Path) HOME_HANDLE.getAcquire();
        if(home == null) {
            synchronized(lock) {
                home = (Path) HOME_HANDLE.getAcquire();
                if(home == null) {
                    HOME_HANDLE.setRelease(Path.of(System.getenv("HOME")));
                    return (Path) HOME_HANDLE.getAcquire();
                }
            }
        }
        return home;
    }

}