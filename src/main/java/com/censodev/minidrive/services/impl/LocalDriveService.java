package com.censodev.minidrive.services.impl;

import com.censodev.minidrive.data.mappers.FileMapper;
import com.censodev.minidrive.data.mappers.FolderMapper;
import com.censodev.minidrive.data.repositories.FileRepository;
import com.censodev.minidrive.data.repositories.FolderRepository;
import com.censodev.minidrive.services.DriveService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Primary
@Qualifier("localDriveService")
public class LocalDriveService extends BaseDriveService implements DriveService {
    public LocalDriveService(FileRepository fileRepository,
                             FolderRepository folderRepository,
                             MessageSource messageSource,
                             FolderMapper folderMapper,
                             FileMapper fileMapper) {
        super(fileRepository, folderRepository, messageSource, folderMapper, fileMapper);
    }

    @SneakyThrows
    @Override
    protected void uploadPhysicalFile(InputStream is, Path filePath) {
        var dirPath = getFolderPath();
        Files.createDirectories(dirPath);
        Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    @SneakyThrows
    @Override
    protected void handleFailedOnUploadPhysicalFile(Exception e, Path filePath) {
        Files.deleteIfExists(filePath);
    }

    @SneakyThrows
    @Override
    protected Resource loadFileResource(Path path) {
        return new UrlResource(path.toUri());
    }

    @SneakyThrows
    @Override
    protected void deletePhysicalFile(Path path) {
        Files.deleteIfExists(path);
    }

    @Override
    protected void deletePhysicalFiles(List<Path> paths) {
        Optional.ofNullable(paths)
                .orElseGet(Collections::emptyList)
                .forEach(this::deletePhysicalFile);
    }
}
