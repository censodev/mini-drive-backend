package com.censodev.minidrive.data.domains;

import com.censodev.minidrive.utils.enums.ResourceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime trashedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ResourceStatusEnum status = ResourceStatusEnum.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;
}
