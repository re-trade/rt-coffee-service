package org.retrade.main.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequest {
    @NotBlank(message = "Brand ID must not be blank")
    private String name;
    @NotBlank(message = "Brand Image URL must not be blank")
    private String imgUrl;
    @NotBlank(message = "Brand Description must not be blank")
    private String description;
    private Set<String> categoryIds;
}
