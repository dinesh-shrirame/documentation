package com.filetask.fileTask.service.impl;


import com.filetask.fileTask.entities.StudentDocument;
import com.filetask.fileTask.exception.FileStorageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtility {

    private ZipUtility() {
    }

    public static byte[] createZip(
            List<StudentDocument> documents) {

        try (
                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream();

                ZipOutputStream zipOutputStream =
                        new ZipOutputStream(byteArrayOutputStream)
        ) {

            Set<String> usedNames = new HashSet<>();

            for (StudentDocument document : documents) {

                Path filePath = Path.of(document.getFilePath());

                if (!Files.exists(filePath)) {
                    throw new FileStorageException(
                            "File not found: " + document.getFileName()
                    );
                }

                String zipFileName = getUniqueFileName(
                        document.getFileName(),
                        usedNames
                );

                ZipEntry zipEntry = new ZipEntry(zipFileName);

                zipOutputStream.putNextEntry(zipEntry);

                Files.copy(filePath, zipOutputStream);

                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();

            return byteArrayOutputStream.toByteArray();

        } catch (IOException exception) {

            throw new FileStorageException(
                    "Unable to create ZIP file",
                    exception
            );
        }
    }

    private static String getUniqueFileName(
            String fileName,
            Set<String> usedNames) {

        if (usedNames.add(fileName)) {
            return fileName;
        }

        int counter = 1;

        int dotIndex = fileName.lastIndexOf('.');

        String name = dotIndex > 0
                ? fileName.substring(0, dotIndex)
                : fileName;

        String extension = dotIndex > 0
                ? fileName.substring(dotIndex)
                : "";

        String newFileName;

        do {

            newFileName =
                    name + "_" + counter + extension;

            counter++;

        } while (!usedNames.add(newFileName));

        return newFileName;
    }
}