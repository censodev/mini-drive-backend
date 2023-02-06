package com.censodev.minidrive.data.mappers;

import com.censodev.minidrive.data.domains.File;
import com.censodev.minidrive.data.dto.drive.FileRes;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileMapper extends BaseMapper<File, FileRes> {
}
