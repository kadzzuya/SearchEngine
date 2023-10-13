package searchengine.model;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "index_search")
public class Index {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "page_id")
    private int pageId;

    @NotNull
    @Column(name = "lemma_id")
    private int lemmaId;

    @NotNull
    @Column(name = "lemma_rank")
    private float lemmaCount;
}