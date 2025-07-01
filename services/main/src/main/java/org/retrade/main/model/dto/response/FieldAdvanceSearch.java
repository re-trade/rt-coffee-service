package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldAdvanceSearch {
    private List<CategoriesAdvanceSearch>  categoriesAdvanceSearch;
    private Set<BrandResponse> brands;
    private Set<String> states;
    private Set<SellerFilterResponse> sellers;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
