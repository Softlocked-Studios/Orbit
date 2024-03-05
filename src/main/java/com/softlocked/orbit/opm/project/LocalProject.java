package com.softlocked.orbit.opm.project;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.opm.packager.PackageDownloader;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.opm.packager.OrbitPackage;
import com.softlocked.orbit.parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class LocalProject implements Runner {
    private final OrbitPackage pkg;
    private final String entrypoint; // Path to the entrypoint file, for easy access
    private final String packagePath; // Path to the package directory

    public LocalProject(String path) throws IOException {
        // Look for path/metadata.yml
        String metadataPath = path + File.separator + "metadata.yml";
        File metadataFile = new File(metadataPath);

        if (!metadataFile.exists()) {
            throw new IllegalArgumentException("metadata.yml not found in " + path);
        }

        byte[] metadata = Files.readAllBytes(metadataFile.toPath());
        String metadataContent = new String(metadata);

        // Parse metadata.yml
        this.pkg = OrbitPackage.fromYaml(metadataContent);

        // Set entrypoint
        this.entrypoint = path + File.separator + pkg.entrypoint();

        // Set package path
        this.packagePath = path + File.separator + "modules" + File.separator;

        new File(this.packagePath).mkdirs();
    }

    public OrbitPackage getPackage() {
        return pkg;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public void run() {
        try {
            install();
        } catch (Exception | Error e) {
            System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to install dependencies for " + pkg.entrypoint());
            System.out.println(e.getMessage());

            return;
        }

        GlobalContext context = new GlobalContext(new File(entrypoint).getParent(), packagePath);

        try {
            byte[] mainFile = Files.readAllBytes(new File(entrypoint).toPath());
            String mainCode = new String(mainFile);

            try {
                List<String> tokens = new Lexer(mainCode).tokenize();

                ASTNode program = Parser.parse(tokens, context);

                program.evaluate(context);
            } catch (ParsingException e) {
                System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to parse " + pkg.entrypoint());
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to run " + pkg.entrypoint());

                if (e.getMessage() != null) {
                    System.out.println(e.getMessage());
                } else {
                    System.out.println("Unknown error occurred");
                }
            } catch (OutOfMemoryError e) {
                System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to run " + pkg.entrypoint() + " (fatal error)");
                System.out.println("Out of memory");
            } catch (StackOverflowError e) {
                System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to run " + pkg.entrypoint() + " (fatal error)");
                System.out.println("Stack overflow (infinite recursion?)");
            } catch (Error e) {
                System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to run " + pkg.entrypoint() + " (fatal error)");

                if (e.getMessage() != null) {
                    System.out.println(e.getMessage());
                } else {
                    System.out.println("Unknown error occurred");
                }
            }
        } catch (IOException e) {
            System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to read " + pkg.entrypoint());
            System.out.println(e.getMessage());
        }
    }

    public void build() throws Exception {
        System.out.println("Building project as package...");

        install();

        throw new UnsupportedOperationException("Not implemented. gotta implement the parser first");
    }

    public void install() throws Exception {
        pkg.installDependencies(packagePath, true);
    }

    public void install(String url, String name, String version, String baseFolder) throws Exception {
        try {
            OrbitPackage downloaded = (OrbitPackage) PackageDownloader.download(url, name, version, baseFolder + File.separator + "modules", true);

            // Add this to the current project's metadata.yml as a dependency
            String currentMetadataPath = baseFolder + File.separator + "metadata.yml";
            File currentMetadataFile = new File(currentMetadataPath);

            if (!currentMetadataFile.exists()) {
                throw new FileNotFoundException("metadata.yml not found in " + baseFolder);
            }

            byte[] currentMetadata = Files.readAllBytes(currentMetadataFile.toPath());
            String currentMetadataContent = new String(currentMetadata);

            OrbitPackage currentPkg = OrbitPackage.fromYaml(currentMetadataContent);

            OrbitPackage.Dependency dep = new OrbitPackage.Dependency(downloaded.name(), downloaded.version(), url);
            if (!currentPkg.dependencies().contains(dep))
                currentPkg.dependencies().add(dep);

            Files.write(currentMetadataFile.toPath(), currentPkg.toYaml().getBytes());
        } catch (Exception e) {
            System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to install " + name + " " + version + " from " + url);
            System.out.println(e.getMessage());
        }
    }
}
