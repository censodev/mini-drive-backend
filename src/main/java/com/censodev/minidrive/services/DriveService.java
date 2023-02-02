package com.censodev.minidrive.services;

import com.censodev.minidrive.data.dto.drive.DriveRes;
import com.censodev.minidrive.data.dto.drive.FileLoadRes;
import com.censodev.minidrive.data.dto.drive.FileRes;
import com.censodev.minidrive.data.dto.drive.FileUploadReq;
import com.censodev.minidrive.data.dto.drive.FolderCreateReq;
import com.censodev.minidrive.data.dto.drive.FolderRes;
import com.censodev.minidrive.data.enums.ResourceStatusEnum;

import java.util.UUID;

public interface DriveService {
    FolderRes createFolder(FolderCreateReq req);

    FileRes uploadFile(FileUploadReq req);

    FileRes detailFile(UUID id);

    FileLoadRes loadFile(UUID id);

    String generateFileAlias(String originName);

    DriveRes listItemsByFolderAndStatus(Long folderId, ResourceStatusEnum status);

    void moveFile(UUID id, Long folderId);

    void deleteFile(UUID id, boolean isSoftDelete);

    void deleteFolder(Long id, boolean isSoftDelete);
}
