package com.censodev.minidrive.data.repositories;

import com.censodev.minidrive.data.domains.File;
import com.censodev.minidrive.data.domains.Folder;
import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.utils.enums.ResourceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByIdAndStatus(UUID id, ResourceStatusEnum status);

    List<File> findByOwnerAndFolderAndStatusOrderByCreatedAtDesc(User owner, Folder folder, ResourceStatusEnum status);

    List<File> findByOwnerAndFolderIsNullAndStatusOrderByCreatedAtDesc(User owner, ResourceStatusEnum status);

    List<File> findByFolder(Folder folder);

    Optional<File> findById(UUID id);

    Optional<File> findByIdAndOwner(UUID id, User owner);
}
