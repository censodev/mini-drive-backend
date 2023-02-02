package com.censodev.minidrive.data.mappers;

import com.censodev.minidrive.data.domains.Folder;
import com.censodev.minidrive.data.dto.drive.FolderRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class FolderMapper extends Mapper<Folder, FolderRes> {
    public FolderMapper(ObjectMapper mapper) {
        super(mapper, Folder.class, FolderRes.class);
    }
}
