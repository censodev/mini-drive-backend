package com.censodev.minidrive.http.controllers;

import com.censodev.minidrive.data.dto.Res;
import com.censodev.minidrive.data.dto.drive.DriveRes;
import com.censodev.minidrive.data.dto.drive.FileRes;
import com.censodev.minidrive.data.dto.drive.FileUploadReq;
import com.censodev.minidrive.data.dto.drive.FolderCreateReq;
import com.censodev.minidrive.data.dto.drive.FolderRes;
import com.censodev.minidrive.data.enums.ResourceStatusEnum;
import com.censodev.minidrive.services.DriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("api/drive")
@RequiredArgsConstructor
public class DriveController {
    private final DriveService driveService;
    private final MessageSource messageSource;

    @GetMapping("")
    public Res<DriveRes> list(@RequestParam ResourceStatusEnum status) {
        return new Res<>(driveService.listItemsByFolderAndStatus(null, status), null);
    }

    @GetMapping("{folderId}")
    public Res<DriveRes> listByFolder(@PathVariable Long folderId,
                                      @RequestParam ResourceStatusEnum status) {
        return new Res<>(driveService.listItemsByFolderAndStatus(folderId, status), null);
    }

    @PostMapping("folder")
    public Res<FolderRes> createFolder(@RequestBody FolderCreateReq req, Locale locale) {
        return new Res<>(driveService.createFolder(req), messageSource.getMessage("drive.create-folder-success", null, locale));
    }

    @DeleteMapping("folder/{id}")
    public Res<String> deleteFolder(@PathVariable Long id,
                                    @RequestParam Boolean soft,
                                    Locale locale) {
        driveService.deleteFolder(id, soft);
        return new Res<>(null, messageSource.getMessage("drive.delete-folder-success", null, locale));
    }

    @PostMapping("file/upload")
    public Res<FileRes> uploadFile(FileUploadReq req, Locale locale) {
        var file = driveService.uploadFile(req);
        return new Res<>(file, messageSource.getMessage("drive.upload-file-success", null, locale));
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
    public Res<FileRes> fileDetails(@PathVariable String id) {
        return new Res<>(driveService.detailFile(UUID.fromString(id)), null);
    }

    @PutMapping("file/{id}/move/{folderId}")
    public Res<String> moveFile(@PathVariable String id,
                                @PathVariable Long folderId,
                                Locale locale) {
        driveService.moveFile(UUID.fromString(id), folderId);
        return new Res<>(null, messageSource.getMessage("drive.move-file-success", null, locale));
    }

    @DeleteMapping("file/{id}")
    public Res<String> deleteFile(@PathVariable String id,
                                  @RequestParam Boolean soft,
                                  Locale locale) {
        driveService.deleteFile(UUID.fromString(id), soft);
        return new Res<>(null, messageSource.getMessage("drive.delete-file-success", null, locale));
    }
}
