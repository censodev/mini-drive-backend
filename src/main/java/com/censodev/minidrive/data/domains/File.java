package com.censodev.minidrive.data.domains;

import com.censodev.minidrive.data.enums.ResourceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "files")
@Getter
@Setter
@ToString(callSuper = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class File extends Domain {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    private String alias;
    private Float size;
    private String mime;
    private Instant trashedAt;

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
