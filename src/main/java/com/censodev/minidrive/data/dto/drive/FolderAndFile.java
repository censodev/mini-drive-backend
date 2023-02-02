package com.censodev.minidrive.data.dto.drive;

import com.censodev.minidrive.data.domains.File;
import com.censodev.minidrive.data.domains.Folder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FolderAndFile {
    private List<Folder> folders = new ArrayList<>();
    private List<File> files = new ArrayList<>();

    public void push(FolderAndFile folderAndFile) {
        folders.addAll(folderAndFile.folders);
        files.addAll(folderAndFile.files);
    }
}