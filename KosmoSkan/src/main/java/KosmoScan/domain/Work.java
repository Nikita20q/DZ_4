package KosmoScan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "works")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private Double latitude;
    private LocalDateTime deadline;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}