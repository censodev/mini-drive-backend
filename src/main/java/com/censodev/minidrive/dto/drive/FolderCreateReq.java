package com.censodev.minidrive.dto.drive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderCreateReq {
    private String name;
    private Long parentId;
}
