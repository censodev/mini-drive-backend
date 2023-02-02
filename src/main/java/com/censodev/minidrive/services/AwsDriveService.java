package com.censodev.minidrive.services;

import com.censodev.minidrive.data.domains.File;
import com.censodev.minidrive.data.domains.Folder;
import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.data.dto.drive.DriveRes;
import com.censodev.minidrive.data.dto.drive.FileLoadRes;
import com.censodev.minidrive.data.dto.drive.FileRes;
import com.censodev.minidrive.data.dto.drive.FileUploadReq;
import com.censodev.minidrive.data.dto.drive.FolderCreateReq;
import com.censodev.minidrive.data.dto.drive.FolderRes;
import com.censodev.minidrive.data.enums.ResourceStatusEnum;
import com.censodev.minidrive.data.mappers.FileMapper;
import com.censodev.minidrive.data.mappers.FolderMapper;
import com.censodev.minidrive.data.repositories.FileRepository;
import com.censodev.minidrive.data.repositories.FolderRepository;
import com.censodev.minidrive.exceptions.BusinessException;
import com.censodev.minidrive.utils.SessionUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Qualifier("awsDriveService")
@Slf4j
@RequiredArgsConstructor
public class AwsDriveService implements DriveService {
    private final Path root = Paths.get("drive");
    private final MessageSource messageSource;
    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final SessionUtil session;
    private final FileMapper fileMapper;
    private final FolderMapper folderMapper;
    private final AwsS3Service s3;


    private Path getFolderPath(String uid) {
        return root.resolve(uid);
    }

    private Path getFolderPath() {
        return getFolderPath(session.getAuthUser().getId().toString());
    }

    private Path getFilePath(String uid, String fileAlias) {
        return getFolderPath(uid)
                .resolve(fileAlias);
    }

    private Path getFilePath(String fileAlias) {
        return getFilePath(session.getAuthUser().getId().toString(), fileAlias);
    }

    private FolderAndFile getSubFoldersAndFilesIncludeRootFolder(Folder rootFolder) {
        var files = fileRepository
                .findByFolder(rootFolder);
        var folderAndFile = new FolderAndFile();
        folderAndFile.getFolders().add(rootFolder);
        folderAndFile.getFiles().addAll(files);

        folderRepository.findByParent(rootFolder)
                .forEach(f -> folderAndFile.push(getSubFoldersAndFilesIncludeRootFolder(f)));
        return folderAndFile;
    }

    @Override
    public FolderRes createFolder(FolderCreateReq req) {
        var folderBuilder = Folder.builder()
                .name(req.getName())
                .owner(User.builder().id(session.getAuthUser().getId()).build());
        if (req.getParentId() != null) {
            var parent = folderRepository.findById(req.getParentId())
                    .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.parent-folder-not-found", null, LocaleContextHolder.getLocale())));
            folderBuilder = folderBuilder.parent(parent);
        }
        var folder = folderRepository.save(folderBuilder.build());
        return folderMapper.convert(folder);
    }

    @Override
    public FileRes uploadFile(FileUploadReq req) {
        var multipartFile = req.getFile();
        var originName = multipartFile.getOriginalFilename();
        var alias = generateFileAlias(originName);
        var authUser = session.getAuthUser();
        var filePath = getFilePath(alias);

        var fileBuilder = File.builder()
                .name(originName)
                .alias(alias)
                .owner(User.builder().id(authUser.getId()).build())
                .size((float) multipartFile.getSize())
                .mime(URLConnection.guessContentTypeFromName(originName));
        if (req.getFolderId() != null) {
            var folder = folderRepository.findById(req.getFolderId())
                    .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.parent-folder-not-found", null, LocaleContextHolder.getLocale())));
            fileBuilder.folder(folder);
        }

        try {
//            var dirPath = getFolderPath();
//            Files.createDirectories(dirPath);
//            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            s3.upload(multipartFile.getInputStream(), filePath);
            var file = fileRepository.save(fileBuilder.build());
            return fileMapper.convert(file);
        } catch (Exception e) {
//            try {
//                Files.deleteIfExists(filePath);
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }
            e.printStackTrace();
            throw new BusinessException(messageSource.getMessage("drive.upload-file-failed", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public FileRes detailFile(UUID id) {
        return fileRepository.findById(id)
                .map(fileMapper::convert)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.file-not-found", null, LocaleContextHolder.getLocale())));
    }

    @Override
    public FileLoadRes loadFile(UUID id) {
        var file = fileRepository.findByIdAndStatus(id, ResourceStatusEnum.ACTIVE)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.file-not-found", null, LocaleContextHolder.getLocale())));
        try {
            var path = getFilePath(file.getOwner().getId().toString(), file.getAlias());
//            var resource = new UrlResource(path.toUri());
            var resource = new InputStreamResource(s3.load(path));

            if (resource.exists() || resource.isReadable())
                return FileLoadRes.builder()
                        .resource(resource)
                        .details(fileMapper.convert(file))
                        .build();
            throw new BusinessException(messageSource.getMessage("drive.load-file-failed", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(messageSource.getMessage("drive.load-file-failed", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    @SneakyThrows
    public String generateFileAlias(String originName) {
        var temp = Normalizer.normalize(originName, Normalizer.Form.NFD);
        var pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        var normalizedName = pattern.matcher(temp)
                .replaceAll("")
                .replace(" ", "_")
                .replace("Đ", "D")
                .replace("đ", "")
                .toLowerCase();

        var now = new Date().getTime();
        var random = String.format("%06d", SecureRandom.getInstanceStrong().nextInt(999999));

        return String.valueOf(now)
                .concat("_")
                .concat(random)
                .concat("_")
                .concat(normalizedName);
    }

    @Override
    public DriveRes listItemsByFolderAndStatus(Long folderId, ResourceStatusEnum status) {
        var me = session.getAuthUser();
        List<Folder> folders;
        List<File> files;
        if (folderId != null) {
            var parent = folderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.folder-not-found", null, LocaleContextHolder.getLocale())));
            folders = folderRepository.findByOwnerAndParentAndStatusOrderByIdDesc(me, parent, status);
            files = fileRepository.findByOwnerAndFolderAndStatusOrderByCreatedAtDesc(me, parent, status);
        } else {
            folders = folderRepository.findByOwnerAndParentIsNullAndStatusOrderByIdDesc(me, status);
            files = fileRepository.findByOwnerAndFolderIsNullAndStatusOrderByCreatedAtDesc(me, status);
        }
        return DriveRes.builder()
                .folders(folders.stream()
                        .map(folderMapper::convert)
                        .collect(Collectors.toList()))
                .files(files.stream()
                        .map(fileMapper::convert)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public void moveFile(UUID id, Long folderId) {
        var me = session.getAuthUser();
        var folder = folderRepository.findByIdAndOwner(folderId, me)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.folder-not-found", null, LocaleContextHolder.getLocale())));
        var file = fileRepository.findByIdAndOwner(id, me)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.file-not-found", null, LocaleContextHolder.getLocale())));
        file.setFolder(folder);
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFile(UUID id, boolean isSoftDelete) {
        var me = session.getAuthUser();
        var file = fileRepository.findByIdAndOwner(id, me)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.file-not-found", null, LocaleContextHolder.getLocale())));
        if (isSoftDelete) {
            file.setStatus(ResourceStatusEnum.TRASHED);
            file.setTrashedAt(LocalDateTime.now());
        } else {
            file.setStatus(ResourceStatusEnum.DELETED);
            var path = getFilePath(file.getAlias());
            try {
//                Files.deleteIfExists(path);
                s3.delete(path);
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException(messageSource.getMessage("drive.delete-file-failed", null, LocaleContextHolder.getLocale()));
            }
        }
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFolder(Long id, boolean isSoftDelete) {
        var me = session.getAuthUser();
        var folder = folderRepository.findByIdAndOwner(id, me)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.folder-not-found", null, LocaleContextHolder.getLocale())));
        var faf = getSubFoldersAndFilesIncludeRootFolder(folder);
        if (isSoftDelete) {
            var now = LocalDateTime.now();
            faf.getFolders().forEach(f -> {
                f.setStatus(ResourceStatusEnum.TRASHED);
                f.setTrashedAt(now);
            });
            faf.getFiles().forEach(f -> {
                f.setStatus(ResourceStatusEnum.TRASHED);
                f.setTrashedAt(now);
            });
        } else {
            faf.getFolders().forEach(f -> f.setStatus(ResourceStatusEnum.DELETED));
            if (!faf.getFiles().isEmpty()) {
                var paths = faf.getFiles()
                        .stream()
                        .peek(f -> f.setStatus(ResourceStatusEnum.DELETED))
                        .map(f -> getFilePath(f.getAlias()))
                        .collect(Collectors.toList());
                try {
                    s3.delete(paths);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new BusinessException(messageSource.getMessage("drive.delete-folder-failed", null, LocaleContextHolder.getLocale()));
                }
            }
//                for (var f : files) {
//                    f.setStatus(FileStatusEnum.DELETED);
//                    Files.deleteIfExists(getFilePath(f.getAlias()));
//                }
        }
        folderRepository.saveAll(faf.getFolders());
        fileRepository.saveAll(faf.getFiles());
    }

    @Data
    private static class FolderAndFile {
        private List<Folder> folders = new ArrayList<>();
        private List<File> files = new ArrayList<>();

        public void push(FolderAndFile folderAndFile) {
            folders.addAll(folderAndFile.folders);
            files.addAll(folderAndFile.files);
        }
    }
}
