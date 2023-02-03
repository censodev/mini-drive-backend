package com.censodev.minidrive.services.impl;

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
import com.censodev.minidrive.services.DriveService;
import com.censodev.minidrive.utils.SessionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class BaseDriveService implements DriveService {
    private static final Path ROOT = Paths.get("drive");
    protected final FileRepository fileRepository;
    protected final FolderRepository folderRepository;
    protected final MessageSource messageSource;
    protected final FolderMapper folderMapper;
    protected final FileMapper fileMapper;

    protected Path getFolderPath(String uid) {
        return ROOT.resolve(uid);
    }

    protected Path getFolderPath() {
        return getFolderPath(SessionUtil.getAuthUser().getId().toString());
    }

    protected Path getFilePath(String uid, String fileAlias) {
        return getFolderPath(uid)
                .resolve(fileAlias);
    }

    protected Path getFilePath(String fileAlias) {
        return getFilePath(SessionUtil.getAuthUser().getId().toString(), fileAlias);
    }

    protected FolderAndFile getSubFoldersAndFilesIncludeRootFolder(Folder rootFolder) {
        var files = fileRepository
                .findByFolderId(rootFolder.getId());
        var folderAndFile = new FolderAndFile();
        folderAndFile.getFolders().add(rootFolder);
        folderAndFile.getFiles().addAll(files);

        folderRepository.findByParent(rootFolder)
                .forEach(f -> folderAndFile.push(getSubFoldersAndFilesIncludeRootFolder(f)));
        return folderAndFile;
    }

    protected abstract void uploadPhysicalFile(InputStream is, Path filePath);

    protected abstract void handleFailedOnUploadPhysicalFile(Exception e, Path filePath);

    protected abstract Resource loadFileResource(Path path);

    protected abstract void deletePhysicalFile(Path path);

    protected abstract void deletePhysicalFiles(List<Path> paths);

    @SneakyThrows
    protected String generateFileAlias(String originName) {
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
    public FolderRes createFolder(FolderCreateReq req) {
        var folderBuilder = Folder.builder()
                .name(req.getName())
                .owner(User.builder().id(SessionUtil.getAuthUser().getId()).build());
        if (req.getParentId() != null) {
            var parent = folderRepository.findById(req.getParentId())
                    .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.parent-folder-not-found", null, LocaleContextHolder.getLocale())));
            folderBuilder.parent(parent);
        }
        var folder = folderRepository.save(folderBuilder.build());
        return folderMapper.convert(folder);
    }

    @Override
    public FileRes detailFile(Long id) {
        return fileRepository.findById(id)
                .map(fileMapper::convert)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.file-not-found", null, LocaleContextHolder.getLocale())));
    }

    @Override
    public DriveRes listItemsByFolderAndStatus(Long folderId, ResourceStatusEnum status) {
        var me = SessionUtil.getAuthUser();
        List<Folder> folders;
        List<File> files;
        if (folderId != null) {
            var parent = folderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.folder-not-found", null, LocaleContextHolder.getLocale())));
            folders = folderRepository.findByOwnerAndParentAndStatusOrderByIdDesc(me, parent, status);
            files = fileRepository.findByOwnerIdAndFolderIdAndStatusOrderByCreatedAtDesc(me.getId(), parent.getId(), status);
        } else {
            folders = folderRepository.findByOwnerAndParentIsNullAndStatusOrderByIdDesc(me, status);
            files = fileRepository.findByOwnerIdAndFolderIsNullAndStatusOrderByCreatedAtDesc(me.getId(), status);
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
    public void moveFile(Long id, Long folderId) {
        var me = SessionUtil.getAuthUser();
        var folder = folderRepository.findByIdAndOwner(folderId, me)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.folder-not-found", null, LocaleContextHolder.getLocale())));
        var file = fileRepository.findByIdAndOwnerId(id, me.getId())
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.file-not-found", null, LocaleContextHolder.getLocale())));
        file.setFolder(folder);
        fileRepository.save(file);
    }

    @Override
    public FileRes uploadFile(FileUploadReq req) {
        var multipartFile = req.getFile();
        var originName = multipartFile.getOriginalFilename();
        var alias = generateFileAlias(originName);
        var authUser = SessionUtil.getAuthUser();
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

        try (InputStream is = multipartFile.getInputStream()) {
            uploadPhysicalFile(is, filePath);
            var file = fileRepository.save(fileBuilder.build());
            return fileMapper.convert(file);
        } catch (Exception e) {
            e.printStackTrace();
            handleFailedOnUploadPhysicalFile(e, filePath);
            throw new BusinessException(messageSource.getMessage("drive.upload-file-failed", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public FileLoadRes loadFile(Long id) {
        var file = fileRepository.findByIdAndStatus(id, ResourceStatusEnum.ACTIVE)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.file-not-found", null, LocaleContextHolder.getLocale())));
        try {
            var path = getFilePath(file.getOwner().getId().toString(), file.getAlias());
            var resource = loadFileResource(path);
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
    @Transactional
    public void deleteFile(Long id, boolean isSoftDelete) {
        var me = SessionUtil.getAuthUser();
        var file = fileRepository.findByIdAndOwnerId(id, me.getId())
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.file-not-found", null, LocaleContextHolder.getLocale())));
        if (isSoftDelete) {
            file.setStatus(ResourceStatusEnum.TRASHED);
            file.setTrashedAt(Instant.now());
        } else {
            file.setStatus(ResourceStatusEnum.DELETED);
            var path = getFilePath(file.getAlias());
            try {
                deletePhysicalFile(path);
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
        var me = SessionUtil.getAuthUser();
        var folder = folderRepository.findByIdAndOwner(id, me)
                .orElseThrow(() -> new BusinessException(messageSource.getMessage("drive.folder-not-found", null, LocaleContextHolder.getLocale())));
        var faf = getSubFoldersAndFilesIncludeRootFolder(folder);
        if (isSoftDelete) {
            var now = Instant.now();
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
                    deletePhysicalFiles(paths);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new BusinessException(messageSource.getMessage("drive.delete-folder-failed", null, LocaleContextHolder.getLocale()));
                }
            }
        }
        folderRepository.saveAll(faf.getFolders());
        fileRepository.saveAll(faf.getFiles());
    }

    @Getter
    private static class FolderAndFile {
        private List<Folder> folders = new ArrayList<>();
        private List<File> files = new ArrayList<>();

        public void push(FolderAndFile folderAndFile) {
            folders.addAll(folderAndFile.folders);
            files.addAll(folderAndFile.files);
        }
    }
}
