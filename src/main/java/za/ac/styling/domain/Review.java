package za.ac.styling.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;


    @ManyToOne
    private User user;


    @ManyToOne
    private Product product;


    private int rating;
    private String comment;
    private Date reviewDate;
}