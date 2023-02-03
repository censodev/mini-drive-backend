package com.censodev.minidrive.data.repositories;

import com.censodev.minidrive.data.domains.File;
import com.censodev.minidrive.data.enums.ResourceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByIdAndStatus(Long id, ResourceStatusEnum status);

    List<File> findByOwnerIdAndFolderIdAndStatusOrderByCreatedAtDesc(Long ownerId, Long folderId, ResourceStatusEnum status);

    List<File> findByOwnerIdAndFolderIsNullAndStatusOrderByCreatedAtDesc(Long ownerId, ResourceStatusEnum status);

    List<File> findByFolderId(Long folderId);

    Optional<File> findByIdAndOwnerId(Long id, Long ownerId);
}
