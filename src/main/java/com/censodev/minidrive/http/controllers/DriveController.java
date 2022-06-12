package com.censodev.minidrive.http.controllers;

import com.censodev.minidrive.dto.Res;
import com.censodev.minidrive.dto.drive.*;
import com.censodev.minidrive.services.DriveService;
import com.censodev.minidrive.utils.enums.ResourceStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("api/drive")
public class DriveController {
    @Autowired
    private DriveService driveService;

    @GetMapping("")
    public ResponseEntity<Res<DriveRes>> list(@RequestParam ResourceStatusEnum status) {
        return ResponseEntity.ok(new Res<>(driveService.listItemsByFolderAndStatus(null, status), null));
    }

    @GetMapping("{folderId}")
    public ResponseEntity<Res<DriveRes>> listByFolder(@PathVariable Long folderId,
                                                      @RequestParam ResourceStatusEnum status) {
        return ResponseEntity.ok(new Res<>(driveService.listItemsByFolderAndStatus(folderId, status), null));
    }

    @PostMapping("folder")
    public ResponseEntity<Res<FolderRes>> createFolder(@RequestBody FolderCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Res<>(driveService.createFolder(req), "Tạo thư mục thành công"));
    }

    @DeleteMapping("folder/{id}")
    public ResponseEntity<Res<String>> deleteFolder(@PathVariable Long id,
                                                    @RequestParam Boolean soft) {
        driveService.deleteFolder(id, soft);
        return ResponseEntity.ok(new Res<>(null, "Xóa thư mục thành công"));
    }

    @PostMapping("file/upload")
    public ResponseEntity<Res<FileRes>> uploadFile(FileUploadReq req) {
        var file = driveService.uploadFile(req);
        return ResponseEntity.ok(new Res<>(file, "Tải lên tệp thành công"));
    }

    @GetMapping("file/{id}")
    public ResponseEntity<Resource> loadFile(@PathVariable String id,
                                             @RequestParam(required = false) Boolean preview) {
        var file = driveService.loadFile(UUID.fromString(id));
        var filename = URLEncoder.encode(file.getDetails().getName(), StandardCharsets.UTF_8);
        var mime = file.getDetails().getMime();

        if (preview != null && preview && mime != null && !mime.isEmpty())
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, mime)
                    .body(file.getResource());
        else
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(file.getResource());
    }

    @GetMapping("file/{id}/details")
    public ResponseEntity<Res<FileRes>> fileDetails(@PathVariable String id) {
        return ResponseEntity.ok(new Res<>(driveService.detailFile(UUID.fromString(id)), null));
    }

    @PutMapping("file/{id}/move/{folderId}")
    public ResponseEntity<Res<String>> moveFile(@PathVariable String id,
                                                @PathVariable Long folderId) {
        driveService.moveFile(UUID.fromString(id), folderId);
        return ResponseEntity.ok(new Res<>(null, "Chuyển tệp thành công"));
    }

    @DeleteMapping("file/{id}")
    public ResponseEntity<Res<String>> deleteFile(@PathVariable String id,
                                                  @RequestParam Boolean soft) {
        driveService.deleteFile(UUID.fromString(id), soft);
        return ResponseEntity.ok(new Res<>(null, "Xóa tệp thành công"));
    }
}
