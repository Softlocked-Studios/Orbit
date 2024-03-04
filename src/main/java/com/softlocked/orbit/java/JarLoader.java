package com.softlocked.orbit.java;

import com.softlocked.orbit.interpreter.memory.GlobalContext;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

public class JarLoader {
    public static void loadLibrary(GlobalContext globalContext, String path) {
        try (JarFile jarFile = new JarFile(new File(path))) {
            jarFile.stream().forEach(jarEntry -> {
                if (jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName().replace("/", ".").replace(".class", "");
                    try (URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(path).toURI().toURL()})) {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (OrbitJavaLibrary.class.isAssignableFrom(clazz)) {
                            OrbitJavaLibrary library = (OrbitJavaLibrary) clazz.getConstructor().newInstance();
                            library.load(globalContext);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load library", e);
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load library", e);
        }
    }
}
