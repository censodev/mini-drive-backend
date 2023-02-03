package com.censodev.minidrive.services;

import com.censodev.minidrive.data.dto.drive.DriveRes;
import com.censodev.minidrive.data.dto.drive.FileLoadRes;
import com.censodev.minidrive.data.dto.drive.FileRes;
import com.censodev.minidrive.data.dto.drive.FileUploadReq;
import com.censodev.minidrive.data.dto.drive.FolderCreateReq;
import com.censodev.minidrive.data.dto.drive.FolderRes;
import com.censodev.minidrive.data.enums.ResourceStatusEnum;

public interface DriveService {
    FolderRes createFolder(FolderCreateReq req);

    FileRes uploadFile(FileUploadReq req);

    FileRes detailFile(Long id);

    FileLoadRes loadFile(Long id);

    DriveRes listItemsByFolderAndStatus(Long folderId, ResourceStatusEnum status);

    void moveFile(Long id, Long folderId);

    void deleteFile(Long id, boolean isSoftDelete);

    void deleteFolder(Long id, boolean isSoftDelete);
}
