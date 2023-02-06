package com.censodev.minidrive.data.mappers;

import com.censodev.minidrive.data.domains.Folder;
import com.censodev.minidrive.data.dto.drive.FolderRes;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FolderMapper extends BaseMapper<Folder, FolderRes> {
}
