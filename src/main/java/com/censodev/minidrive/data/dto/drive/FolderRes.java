package com.censodev.minidrive.data.dto.drive;

import com.censodev.minidrive.data.dto.user.UserRes;
import com.censodev.minidrive.data.enums.ResourceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderRes {
    private Long id;
    private String name;
    private ResourceStatusEnum status;
    private UserRes owner;
    private LocalDateTime createdAt;
    private LocalDateTime trashedAt;
}
