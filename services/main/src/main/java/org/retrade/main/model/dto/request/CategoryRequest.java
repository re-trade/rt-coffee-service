package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name must not be blank")
    private String name;
    @NotBlank(message = "Category description must not be blank")
    private String description;
    private String categoryParentId;
    private Boolean visible;
}
