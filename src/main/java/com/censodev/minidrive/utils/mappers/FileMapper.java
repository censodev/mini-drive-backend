package com.censodev.minidrive.utils.mappers;

import com.censodev.minidrive.data.domains.File;
import com.censodev.minidrive.dto.drive.FileRes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class FileMapper extends Mapper<File, FileRes> {
    public FileMapper(ObjectMapper mapper) {
        super(mapper, File.class, FileRes.class);
    }
}
