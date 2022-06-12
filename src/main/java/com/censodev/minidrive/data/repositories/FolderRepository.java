package com.censodev.minidrive.data.repositories;

import com.censodev.minidrive.data.domains.Folder;
import com.censodev.minidrive.data.domains.User;
import com.censodev.minidrive.utils.enums.ResourceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByOwnerAndParentAndStatusOrderByIdDesc(User owner, Folder parent, ResourceStatusEnum status);

    List<Folder> findByOwnerAndParentIsNullAndStatusOrderByIdDesc(User owner, ResourceStatusEnum status);

    Optional<Folder> findByIdAndOwner(Long id, User owner);

    List<Folder> findByParent(Folder parent);
}
