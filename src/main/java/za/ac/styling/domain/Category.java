package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"parentCategory", "subCategory"})
@ToString(exclude = {"parentCategory", "subCategory"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;


    private String name;
    private String description;
    private String imageUrl;


    @ManyToOne
    private Category parentCategory;


    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<Category> subCategory;


    private boolean isActive;
}