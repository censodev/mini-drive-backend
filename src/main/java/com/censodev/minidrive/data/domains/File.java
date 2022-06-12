package com.censodev.minidrive.data.domains;

import com.censodev.minidrive.utils.enums.ResourceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class File {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;
    private String name;
    private String alias;
    private Float size;
    private String mime;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime trashedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ResourceStatusEnum status = ResourceStatusEnum.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder folder;
}
