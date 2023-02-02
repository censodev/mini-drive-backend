package com.censodev.minidrive.data.dto.drive;

import com.censodev.minidrive.data.dto.user.UserRes;
import com.censodev.minidrive.data.enums.ResourceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRes {
    private UUID id;
    private String name;
    private String alias;
    private Float size;
    private String mime;
    private ResourceStatusEnum status;
    private UserRes owner;
    private LocalDateTime createdAt;
    private LocalDateTime trashedAt;
}
