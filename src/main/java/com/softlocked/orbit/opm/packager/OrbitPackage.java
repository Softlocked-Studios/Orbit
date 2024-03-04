package com.softlocked.orbit.opm.packager;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

/**
 * Class representing a package. Packages contain a metadata.yml file that contains information about the package.
 */
public record OrbitPackage(String name, String version, String description, String license, ArrayList<String> authors, String homepage,
                           ArrayList<OrbitPackage.Dependency> dependencies,
                           String entrypoint) {

    public static class Dependency {
        String name;
        String version;
        String url;

        public Dependency(String name, String version, String url) {
            this.name = name;
            this.version = version;
            this.url = url;
        }

        public String name() {
            return name;
        }

        public String version() {
            return version;
        }

        public String url() {
            return url;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj.getClass() != this.getClass()) return false;
            Dependency dep = (Dependency) obj;
            return dep.name.equals(this.name) && dep.version.equals(this.version) && dep.url.equals(this.url);
        }
    }

    public static OrbitPackage fromYaml(String content) {
        Yaml yaml = new Yaml();

        Map<String, Object> data = yaml.load(content);

        String name = (String) data.get("name");
        String version = (String) data.get("version");
        String description = (String) data.get("description");
        String license = (String) data.get("license");

        ArrayList<String> authors = (ArrayList<String>) data.get("authors");

        String homepage = (String) data.get("homepage");

        ArrayList<OrbitPackage.Dependency> dependencies = new ArrayList<>();

        if (data.containsKey("dependencies")) {
            try {
                ArrayList<Map<String, String>> deps = (ArrayList<Map<String, String>>) data.get("dependencies");
                for (Map<String, String> dep : deps) {
                    dependencies.add(new OrbitPackage.Dependency(dep.get("name"), dep.get("version"), dep.get("repository")));
                }
            } catch (ClassCastException ignored) {}
        }

        Map<String, Object> build = (Map<String, Object>) data.get("build");
        String entrypoint = (String) build.get("entrypoint");

        return new OrbitPackage(name != null ? name : "", version != null ? version : "", description != null ? description : "",
                license != null ? license : "", authors != null ? authors : new ArrayList<>(), homepage != null ? homepage : "",
                dependencies, entrypoint != null ? entrypoint : "");
    }

    public int installDependencies(String moduleFolder, boolean log) throws Exception {
        int packageAmount = 0;

        boolean missing = true;
        // Installing dependencies from metadata.yml
        for (OrbitPackage.Dependency dep : dependencies) {
            String path = moduleFolder + dep.name();

            // Check if said folder exists
            File depFolder = new File(path);

            if(depFolder.exists()) continue;

            try {
                int val = (int) PackageDownloader.download(dep.url(), dep.name(), dep.version(), moduleFolder, false);
                packageAmount += val;

                if(val > 0) {
                    if(missing) {
                        if(log) {
                            System.out.println("Missing dependencies, installing...");
                        }
                        missing = false;
                    }
                }

                if(log && val > 0) {
                    System.out.println("Installing " + dep.name() + " " + dep.version() + " from " + dep.url());
                }
            } catch (Exception e) {
                System.err.println("\u001B[31m[ERROR]\u001B[0m Failed to install " + dep.name() + " " + dep.version() + " from " + dep.url());
                System.out.println(e.getMessage());
            }
        }

        if(log && packageAmount > 0) {
            System.out.println("Downloaded " + packageAmount + " packages");
        }

        return packageAmount;
    }

    public static void createMetadataFile(String path) throws Exception {
        File metadataFile = new File(path + File.separator + "metadata.yml");

        String name = new File(path).getName().toLowerCase().replace(" ", "-");
        String version = "1.0.0";
        String description = "";
        String license = "MIT";
        String author = "";
        String entrypoint = "main.orbit";

        System.out.print("name: (" + name + ") ");
        String input = System.console().readLine();
        if(!input.isEmpty()) {
            name = input.toLowerCase().replace(" ", "-");
        }

        System.out.print("version: (" + version + ") ");
        input = System.console().readLine();
        if(!input.isEmpty()) {
            version = input;
        }

        System.out.print("description: ");
        input = System.console().readLine();
        if(!input.isEmpty()) {
            description = input;
        }

        System.out.print("license: (" + license + ") ");
        input = System.console().readLine();
        if(!input.isEmpty()) {
            license = input;
        }

        System.out.print("author: ");
        input = System.console().readLine();
        if(!input.isEmpty()) {
            author = input;
        }

        System.out.print("entrypoint: (" + entrypoint + ") ");
        input = System.console().readLine();
        if(!input.isEmpty()) {
            entrypoint = input;
        }

        String content = "name: " + name + "\n" +
                "version: " + version + "\n" +
                "description: " + description + "\n" +
                "license: " + license + "\n" +
                "authors:\n" +
                "  - " + author + "\n" +
                "build:\n" +
                "  entrypoint: " + entrypoint;

        System.out.println("\n" + content + "\n");

        System.out.print("Is this information correct? (y/n) ");

        input = System.console().readLine();
        if(!input.equalsIgnoreCase("y")) {
            System.out.println("Aborted.");
            return;
        }

        metadataFile.createNewFile();
        Files.write(metadataFile.toPath(), content.getBytes());

        System.out.println("metadata.yml file created successfully.");
    }

    public String toYaml() {
        StringBuilder yaml = new StringBuilder();
        yaml.append("name: ").append(name).append("\n");
        yaml.append("version: ").append(version).append("\n");
        yaml.append("description: ").append(description).append("\n");
        yaml.append("license: ").append(license).append("\n");
        yaml.append("authors:");
        if (!authors.isEmpty()) {
            yaml.append("\n");
            for (String author : authors) {
                yaml.append("  - ").append(author).append("\n");
            }
        } else {
            yaml.append(" []\n");
        }
        yaml.append("homepage: ").append(homepage).append("\n");
        if (!dependencies.isEmpty()) {
            yaml.append("dependencies:\n");
            for (OrbitPackage.Dependency dependency : dependencies) {
                yaml.append("  - name: ").append(dependency.name).append("\n");
                yaml.append("    version: ").append(dependency.version).append("\n");
                yaml.append("    repository: ").append(dependency.url).append("\n");
            }
        }
        yaml.append("build:\n");
        yaml.append("  entrypoint: ").append(entrypoint).append("\n");
        return yaml.toString();
    }
}
