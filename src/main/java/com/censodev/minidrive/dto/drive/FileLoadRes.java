package com.censodev.minidrive.dto.drive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileLoadRes {
    private Resource resource;
    private FileRes details;
}
