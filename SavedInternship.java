package com.mindsmart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_internships", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "internship_id"})
})
public class SavedInternship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt = LocalDateTime.now();

    public SavedInternship() {}

    public SavedInternship(User user, Internship internship) {
        this.user = user;
        this.internship = internship;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Internship getInternship() { return internship; }
    public void setInternship(Internship internship) { this.internship = internship; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
