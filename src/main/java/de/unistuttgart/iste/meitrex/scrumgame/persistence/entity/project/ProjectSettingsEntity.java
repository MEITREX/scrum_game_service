package de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.project;

import de.unistuttgart.iste.meitrex.common.util.MeitrexCollectionUtils;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.*;

@Entity
@Table(name = "project_settings")
@Getter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSettingsEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "code_repository_settings_id")
    @Setter
    private CodeRepositorySettingsEntity codeRepositorySettings;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ims_settings_id")
    @Setter
    private ImsSettingsEntity imsSettings;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DefinitionOfDoneItemEntity> definitionOfDone = new ArrayList<>();

    public void setDefinitionOfDone(List<DefinitionOfDoneItemEntity> definitionOfDone) {
        this.definitionOfDone = MeitrexCollectionUtils.replaceContent(this.definitionOfDone, definitionOfDone);
    }
}
