package com.uroria.fastconfig.internal.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@UtilityClass
public class FileUtils {
    public List<File> listFiles(@NonNull File folder) {
        return listFiles(folder, null);
    }

    public List<File> listFiles(@NonNull File folder, @Nullable String extension) {
        ObjectList<File> result = new ObjectArrayList<>();

        File[] files = folder.listFiles();

        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (extension == null || file.getName().endsWith(extension)) {
                result.add(file);
            }
        }

        return result;
    }

    public File getAndMake(@NonNull String name, @NonNull String path) {
        return getAndMake(new File(path, name));
    }

    public File getAndMake(@NonNull File file) {
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
        return file;
    }

    private String getExtension(@NonNull String path) {
        return path.lastIndexOf(".") > 0
                ? path.substring(path.lastIndexOf(".") + 1)
                : "";
    }

    public String getExtension(@NonNull File file) {
        return getExtension(file.getName());
    }

    public String replaceExtensions(@NonNull String fileName) {
        if (!fileName.contains(".")) {
            return fileName;
        }
        return fileName.replace("." + getExtension(fileName), "");
    }

    public String getParentDirPath(@NonNull File file) {
        return getParentDirPath(file.getAbsolutePath());
    }

    private String getParentDirPath(@NonNull String fileOrDirPath) {
        boolean endsWithSlash = fileOrDirPath.endsWith(File.separator);
        return fileOrDirPath
                .substring(0, fileOrDirPath.lastIndexOf(File.separatorChar, endsWithSlash
                        ? fileOrDirPath.length() - 2
                        : fileOrDirPath.length() - 1));
    }

    public boolean hasChanged(File file, long timeStamp) {
        if (file == null) {
            return false;
        }
        return timeStamp < file.lastModified();
    }

    public InputStream createInputStream(@NonNull File file) {
        try {
            return Files.newInputStream(file.toPath());
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private OutputStream createOutputStream(@NonNull File file) {
        try {
            return new FileOutputStream(file);
        } catch (final FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Reader createReader(@NonNull File file) {
        try {
            return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        } catch (final FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Writer createWriter(@NonNull File file) {
        try {
            return new OutputStreamWriter(new FileOutputStream(file, false),StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void write(@NonNull File file, @NonNull List<String> lines) {
        try {
            Files.write(file.toPath(), lines);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeToFile(@NonNull File file, @NonNull InputStream inputStream) {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            if (!file.exists()) {
                Files.copy(inputStream, file.toPath());
            } else {
                byte[] data = new byte[8192];
                int count;
                while ((count = inputStream.read(data, 0, 8192)) != -1) {
                    outputStream.write(data, 0, count);
                }
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private byte[] readAllBytes(@NonNull File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> readAllLines(@NonNull File file) {
        byte[] fileBytes = readAllBytes(file);
        String asString = new String(fileBytes);
        try (BufferedReader reader = new BufferedReader(new StringReader(asString))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SneakyThrows
    public void zipFile(String sourceDirectory, String to) {
        File fileTo = getAndMake(new File(to + ".zip"));

        @Cleanup ZipOutputStream zipOutputStream = new ZipOutputStream(createOutputStream(fileTo));
        Path pathFrom = Paths.get(new File(sourceDirectory).toURI());

        Stream<Path> walk = Files.walk(pathFrom);

        walk.filter(path -> !Files.isDirectory(path)).forEach(
                        path -> {
                            ZipEntry zipEntry = new ZipEntry(pathFrom.relativize(path).toString());

                            try {
                                zipOutputStream.putNextEntry(zipEntry);

                                Files.copy(path, zipOutputStream);
                                zipOutputStream.closeEntry();
                            } catch (final IOException ex) {
                                ex.printStackTrace();
                            }
                        });
        walk.close();
    }

    // ----------------------------------------------------------------------------------------------------
    // Checksums
    // ----------------------------------------------------------------------------------------------------

    public String md5ChecksumAsString(@NonNull File filename) {
        byte[] checkSum = md5Checksum(filename);
        StringBuilder result = new StringBuilder();

        for (byte b : checkSum) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return result.toString();
    }

    private byte[] md5Checksum(@NonNull File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = fileInputStream.read(buffer);

                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            return complete.digest();
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void copyFolder(@NonNull File source, @NonNull File destination) throws IOException {

        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String[] files = source.list();

            if (files == null) {
                return;
            }

            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);

                copyFolder(srcFile, destFile);
            }
        } else {

            @Cleanup InputStream in = createInputStream(source);
            @Cleanup OutputStream out = createOutputStream(destination);
            byte[] buffer = new byte[1024];

            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }
}
