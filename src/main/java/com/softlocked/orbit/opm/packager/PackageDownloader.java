package com.softlocked.orbit.opm.packager;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PackageDownloader {
    public static Object download(String url, String packageName, String packageVersion, String modulePath, boolean log) throws Exception {
        int packageAmount = 0;

        URL downloadUrl = new URL(url + '/' + packageName + "/" + packageVersion);

        URLConnection connection = downloadUrl.openConnection();

        // Set the user agent to avoid 403 errors
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        connection.connect();

        if(!packageVersion.equals("latest")) {
            String localPath = modulePath + File.separator + packageName + "-" + packageVersion;

            // Download the file
            try (InputStream in = connection.getInputStream()) {
                new File(localPath).mkdirs();

                File file = new File(localPath + File.separator + "package.zip");
                file.createNewFile();

                OutputStream out = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }

                out.close();
            }

            packageAmount++;

            // Now unzip the file
            String zipPath = localPath + File.separator + "package.zip";
            unzip(zipPath, localPath);

            // Delete the zip file
            File zipFile = new File(zipPath);

            Files.deleteIfExists(zipFile.toPath());

            // Now go open the metadata.yml files and download ITS dependencies
            File metadataFile = new File(localPath + File.separator + "metadata.yml");

            if (metadataFile.exists()) {
                byte[] metadata = Files.readAllBytes(metadataFile.toPath());
                String metadataContent = new String(metadata);

                OrbitPackage pkg = OrbitPackage.fromYaml(metadataContent);

                packageAmount += pkg.installDependencies(modulePath, false);
            }

            if (log) {
                return OrbitPackage.fromYaml(new String(Files.readAllBytes(metadataFile.toPath())));
            }

            return packageAmount;
        } else {
            // Download the zip into the modules folder instead, and then check the metadata.yml file to copy it in the right place
            try (InputStream in = connection.getInputStream()) {
                File file = new File(modulePath + File.separator + "package.zip");
                file.createNewFile();

                OutputStream out = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }

                out.close();
            }

            packageAmount++;

            // Now unzip the file
            String zipPath = modulePath + File.separator + "package.zip";
            unzip(zipPath, modulePath + File.separator + packageName + "-latest");

            // Delete the zip file
            File zipFile = new File(zipPath);

            Files.deleteIfExists(zipFile.toPath());

            // Now go to the destination and open the metadata.yml
            File metadataFile = new File(modulePath + File.separator + packageName + "-latest" + File.separator + "metadata.yml");

            if (!metadataFile.exists()) {
                throw new FileNotFoundException("metadata.yml not found in " + modulePath + File.separator + packageName + "-latest");
            }

            byte[] metadata = Files.readAllBytes(metadataFile.toPath());
            String metadataContent = new String(metadata);

            OrbitPackage pkg = OrbitPackage.fromYaml(metadataContent);

            // And create a new folder with the correct version
            File newFolder = new File(modulePath + File.separator + packageName + "-" + pkg.version());
            newFolder.mkdirs();

            // Copy the contents of the latest folder to the new folder
            File latestFolder = new File(modulePath + File.separator + packageName + "-latest");
            File[] files = latestFolder.listFiles();

            try {
                for (File f : files) {
                    if (f.isDirectory()) {
                        File newDir = new File(newFolder.getAbsolutePath() + File.separator + f.getName());
                        newDir.mkdirs();

                        File[] subFiles = f.listFiles();

                        for (File subFile : subFiles) {
                            Files.copy(subFile.toPath(), new File(newDir.getAbsolutePath() + File.separator + subFile.getName()).toPath());
                        }
                    } else {
                        Files.copy(f.toPath(), new File(newFolder.getAbsolutePath() + File.separator + f.getName()).toPath());
                    }
                }
            } catch (IOException ignored) {
                // If an error occurs, just delete the folder and return
                delete(latestFolder);
                return 0;
            }

            // Lastly remove the latest folder
            delete(latestFolder);

            // and install the dependencies
            packageAmount += pkg.installDependencies(modulePath, false);

            if (log) {
                return pkg;
            }

            return packageAmount;
        }
    }

    private static List<File> unzip(String zipFilePath, String destDirectory) throws IOException {
        List<File> files = new ArrayList<>();
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    File file = extractFile(zipIn, filePath);
                    files.add(file);
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();

                    files.add(dir);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }

        return files;
    }

    private static File extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[1024];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }

        return new File(filePath);
    }

    private static void delete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }
        file.delete();
    }
}
