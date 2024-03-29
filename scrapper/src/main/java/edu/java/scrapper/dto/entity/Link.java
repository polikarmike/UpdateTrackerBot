package edu.java.scrapper.dto.entity;

import edu.java.scrapper.utils.converter.URIConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Link")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false, unique = true)
    @Convert(converter = URIConverter.class)
    private URI uri;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_updated_at")
    private OffsetDateTime lastUpdatedAt;


    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "links")
    private List<Chat> chats = new ArrayList<>();

    public Link() {
    }

    public Link(URI uri) {
        this.uri = uri;
        this.createdAt = OffsetDateTime.now();
        this.lastUpdatedAt = OffsetDateTime.now();
    }

    @Override
    public String toString() {
        return "Link{"
            + "id=" + id
            + ", url=" + uri
            + ", createdAt=" + createdAt
            + ", lastUpdatedAt=" + lastUpdatedAt
            + '}';
    }
}

