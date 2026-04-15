package se.mau.localzero.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user")
@Table(name = "sustainability_action")
public class SustainabilityAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private Double carbonSaving;

    @Column(nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public SustainabilityAction(String title, String description, Category category, Double carbonSaving, User user) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.carbonSaving = carbonSaving;
        setUser(user);
    }

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getSustainabilityActions().remove(this);
        }
        this.user = user;
        if (user != null) {
            user.getSustainabilityActions().add(this);
        }
    }

    @PrePersist
    void onCreate() {
        this.performedAt = LocalDateTime.now();
    }
}
