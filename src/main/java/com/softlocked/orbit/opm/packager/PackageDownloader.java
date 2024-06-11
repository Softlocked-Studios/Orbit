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

        String localPath = modulePath + File.separator + packageName;

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

    public static String getPackagesPath() {
        File file = new File(System.getProperty("user.home") + File.separator + ".orbit");
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(file.getAbsolutePath() + File.separator + "packages");
        if (!file.exists()) {
            file.mkdirs();
        }
        return System.getProperty("user.home") + File.separator + ".orbit" + File.separator + "packages";
    }
}
