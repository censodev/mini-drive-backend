package com.censodev.minidrive.services;

import com.censodev.minidrive.data.domains.File;
import com.censodev.minidrive.data.domains.Folder;
import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.data.repositories.FileRepository;
import com.censodev.minidrive.data.repositories.FolderRepository;
import com.censodev.minidrive.dto.drive.*;
import com.censodev.minidrive.utils.SessionUtil;
import com.censodev.minidrive.utils.enums.ResourceStatusEnum;
import com.censodev.minidrive.utils.mappers.FileMapper;
import com.censodev.minidrive.utils.mappers.FolderMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DriveServiceImpl implements DriveService {
    private final Path root = Paths.get("drive");

    private final FileRepository fileRepository;
    private final FolderRepository folderRepository;
    private final SessionUtil session;
    private final FileMapper fileMapper;
    private final FolderMapper folderMapper;
    private final AwsS3Service s3;

    public DriveServiceImpl(FileRepository fileRepository,
                            FolderRepository folderRepository,
                            SessionUtil session,
                            FileMapper fileMapper,
                            FolderMapper folderMapper,
                            AwsS3Service s3) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
        this.session = session;
        this.fileMapper = fileMapper;
        this.folderMapper = folderMapper;
        this.s3 = s3;
    }

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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục cha"));
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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục cha"));
            fileBuilder = fileBuilder.folder(folder);
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tải lên tệp");
        }
    }

    @Override
    public FileRes detailFile(UUID id) {
        return fileRepository.findById(id)
                .map(fileMapper::convert)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    public FileLoadRes loadFile(UUID id) {
        var file = fileRepository.findByIdAndStatus(id, ResourceStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tệp không tồn tại"));
        try {
            var path = getFilePath(file.getOwner().getId().toString(), file.getAlias());
//            var resource = new UrlResource(path.toUri());
            var resource = new InputStreamResource(s3.load(path));

            if (resource.exists() || resource.isReadable())
                return FileLoadRes.builder()
                        .resource(resource)
                        .details(fileMapper.convert(file))
                        .build();
            throw new RuntimeException("Không thể đọc file!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tải tệp");
        }
    }

    @Override
    public String generateFileAlias(String originName) {
        var temp = Normalizer.normalize(originName, Normalizer.Form.NFD);
        var pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        var normalizedName = pattern.matcher(temp)
                .replaceAll("")
                .replaceAll(" ", "_")
                .replaceAll("Đ", "D")
                .replace("đ", "")
                .toLowerCase();

        var now = new Date().getTime();
        var random = String.format("%06d", new Random().nextInt(999999));

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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục"));
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục"));
        var file = fileRepository.findByIdAndOwner(id, me)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tệp"));
        file.setFolder(folder);
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFile(UUID id, boolean isSoftDelete) {
        var me = session.getAuthUser();
        var file = fileRepository.findByIdAndOwner(id, me)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tệp"));
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
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể xóa vĩnh viễn tệp");
            }
        }
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFolder(Long id, boolean isSoftDelete) {
        var me = session.getAuthUser();
        var folder = folderRepository.findByIdAndOwner(id, me)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục"));
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
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể xóa vĩnh viễn thư mục");
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
