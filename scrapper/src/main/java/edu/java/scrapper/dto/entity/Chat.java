package edu.java.scrapper.dto.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "Chat")
@Setter
@Getter
@EqualsAndHashCode(of = "id")
public class Chat {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "Link_Chat",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "link_id")
    )

    private List<Link> links = new ArrayList<>();

    public Chat() {}

    public Chat(Long id) {
        this.id = id;
        this.createdAt = OffsetDateTime.now();
    }

    @Override
    public String toString() {
        return "Chat{"
            + "id=" + id
            + ", createdAt=" + createdAt
            + '}';
    }

}
