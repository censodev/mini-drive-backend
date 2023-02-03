package com.censodev.minidrive.services.impl;

import com.censodev.minidrive.data.mappers.FileMapper;
import com.censodev.minidrive.data.mappers.FolderMapper;
import com.censodev.minidrive.data.repositories.FileRepository;
import com.censodev.minidrive.data.repositories.FolderRepository;
import com.censodev.minidrive.services.AwsS3Service;
import com.censodev.minidrive.services.DriveService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Service
@Qualifier("awsDriveService")
@Slf4j
public class AwsDriveService extends BaseDriveService implements DriveService {
    private final AwsS3Service s3;

    public AwsDriveService(FileRepository fileRepository,
                           FolderRepository folderRepository,
                           MessageSource messageSource,
                           FileMapper fileMapper,
                           FolderMapper folderMapper,
                           AwsS3Service s3) {
        super(fileRepository, folderRepository, messageSource, folderMapper, fileMapper);
        this.s3 = s3;
    }

    @SneakyThrows
    @Override
    protected void uploadPhysicalFile(InputStream is, Path filePath) {
        s3.upload(is, filePath);
    }

    @Override
    protected void handleFailedOnUploadPhysicalFile(Exception e, Path filePath) {
        // do nothing
    }

    @Override
    protected Resource loadFileResource(Path path) {
        return new InputStreamResource(s3.load(path));
    }

    @Override
    protected void deletePhysicalFile(Path path) {
        s3.delete(path);
    }

    @Override
    protected void deletePhysicalFiles(List<Path> paths) {
        s3.delete(paths);
    }
}
