package de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.user;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.common.util.MeitrexCollectionUtils;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.project.IconEmbeddable;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.project.ProjectEntity;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.role.ProjectRoleEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "user_in_project")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInProjectEntity implements IWithId<UserProjectId> {

    @EmbeddedId
    private UserProjectId id;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private GlobalUserEntity user;

    @MapsId("projectId")
    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToMany(cascade = {})
    @Builder.Default
    private List<ProjectRoleEntity> roles = new ArrayList<>();

    @Embedded
    @Nullable
    @Setter
    private IconEmbeddable currentBadge;

    @PreRemove
    public void onDelete() {
        project.getUsers().remove(this);
        user.getUserInProjects().remove(this);
    }

    public void setRoles(List<ProjectRoleEntity> roles) {
        this.roles = MeitrexCollectionUtils.replaceContent(this.roles, roles);
    }
}
