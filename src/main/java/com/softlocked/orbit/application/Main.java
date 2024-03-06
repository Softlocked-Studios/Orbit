package com.softlocked.orbit.application;

import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.project.LocalProject;
import com.softlocked.orbit.opm.packager.OrbitPackage;
import com.softlocked.orbit.project.OrbitREPL;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length > 0) {
            if(args[0].equals("opkg")) {
                if(args.length < 2) {
                    System.out.println("Usage:");
                    System.out.println("opkg <command>");
                    System.out.println();
                    System.out.println("Commands:");
                    System.out.println("opkg init                      Creates a metadata.yml file in the current directory");
                    System.out.println("opkg install <name>            Installs a package from the central repository");
                    System.out.println("opkg install <name> <url>      Installs a package from a custom repository");
                    System.out.println("opkg uninstall <name>          Uninstalls a package from the current project");
                    System.out.println("opkg build                     Builds the project in the current directory");
                    System.out.println("opkg run                       Runs the project in the current directory");
                    return;
                }
                int command = 0;
                switch (args[1]) {
                    case "init" -> command = 1;
                    case "build" -> command = 2;
                    case "run" -> command = 3;
                    case "install" -> {
                        if(args.length < 3) {
                            System.out.println("\u001B[31mNo package name specified.\u001B[0m");
                            return;
                        }
                        command = 4;
                    }
                    case "uninstall" -> {
                        if(args.length < 3) {
                            System.out.println("\u001B[31mNo package name specified.\u001B[0m");
                            return;
                        }
                        command = 5;
                    }
                    default -> {
                        System.out.println("\u001B[31mInvalid command.\u001B[0m");
                        return;
                    }
                }

                String basePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();

                // Decode the path to handle spaces and special characters
                basePath = URLDecoder.decode(basePath, StandardCharsets.UTF_8);

                // Get the parent directory of the JAR file
                File baseDir = new File(basePath).getParentFile();

                if(command == 1) {
                    File metadataFile = new File(baseDir.getAbsolutePath() + File.separator + "metadata.yml");
                    if(metadataFile.exists()) {
                        System.out.println("\u001B[31mA metadata.yml file already exists in this directory.\u001B[0m");
                        return;
                    }
                    OrbitPackage.createMetadataFile(baseDir.getAbsolutePath());
                    return;
                }

                LocalProject project = new LocalProject(baseDir.getAbsolutePath());

                switch(command) {
                    case 2 -> {
                        project.build();
                    }
                    case 3 -> {
                        project.run();
                    }
                    case 4 -> {
                        if (args.length < 4) {
                            throw new IllegalArgumentException("Orbit Central Repository not implemented yet");
                        }
                        String url = args[3];
                        String name = args[2];
                        // Split name by @ to get the version
                        String[] split = name.split("@");

                        if (split.length > 1) {
                            project.install(url, split[0], split[1], baseDir.getAbsolutePath());
                        } else {
                            project.install(url, args[2], "latest", baseDir.getAbsolutePath());
                        }
                    }
                }
            }
            else if(args[0].equals("orbit")) {
                if(args.length == 1) {
                    String basePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();

                    // Decode the path to handle spaces and special characters
                    basePath = URLDecoder.decode(basePath, StandardCharsets.UTF_8);

                    basePath = new File(basePath).getParentFile().getAbsolutePath();

                    String modulesPath = basePath + File.separator + "modules" + File.separator;

                    OrbitREPL repl = new OrbitREPL(new GlobalContext(basePath, modulesPath));

                    repl.run();
                }
            }
        } else {
            System.out.println("\u001B[31mNo command specified.\u001B[0m");
        }
    }
}