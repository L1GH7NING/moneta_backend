package com.track.moneta.backend.payload;

import com.track.moneta.backend.models.Category;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class CategoryExpense {
    private String category;
    private Double total;
}