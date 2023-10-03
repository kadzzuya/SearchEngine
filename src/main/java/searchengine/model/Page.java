package searchengine.model;

import com.sun.istack.NotNull;
import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Table(name = "page", indexes = {@Index(name = "path_index", columnList = "path, site_id", unique = true)})
public class Page {
    @Id
    @NotNull
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "site_id")
    private int siteId;

    @ManyToOne(cascade = CascadeType.ALL)@JoinColumn(name = "site_id", insertable = false, updatable = false)
    private Site site;

    @Column(name = "path", length = 1000, columnDefinition = "VARCHAR(515)", nullable = false)
    private String sitePath;

    @Column(name = "code")
    private int httpCode;

    @Column(name = "content", length = 16777215, columnDefinition = "MEDIUMTEXT")
    private String htmlContent;
}