package com.censodev.minidrive.services;

import com.censodev.minidrive.data.dto.drive.DriveRes;
import com.censodev.minidrive.data.dto.drive.FileLoadRes;
import com.censodev.minidrive.data.dto.drive.FileRes;
import com.censodev.minidrive.data.dto.drive.FileUploadReq;
import com.censodev.minidrive.data.dto.drive.FolderCreateReq;
import com.censodev.minidrive.data.dto.drive.FolderRes;
import com.censodev.minidrive.data.enums.ResourceStatusEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Primary
@Qualifier("localDriveService")
public class LocalDriveService implements DriveService {
    @Override
    public FolderRes createFolder(FolderCreateReq req) {
        return null;
    }

    @Override
    public FileRes uploadFile(FileUploadReq req) {
        return null;
    }

    @Override
    public FileRes detailFile(UUID id) {
        return null;
    }

    @Override
    public FileLoadRes loadFile(UUID id) {
        return null;
    }

    @Override
    public String generateFileAlias(String originName) {
        return null;
    }

    @Override
    public DriveRes listItemsByFolderAndStatus(Long folderId, ResourceStatusEnum status) {
        return null;
    }

    @Override
    public void moveFile(UUID id, Long folderId) {

    }

    @Override
    public void deleteFile(UUID id, boolean isSoftDelete) {

    }

    @Override
    public void deleteFolder(Long id, boolean isSoftDelete) {

    }
}
