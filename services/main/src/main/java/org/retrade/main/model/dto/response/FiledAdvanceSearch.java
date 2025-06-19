package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiledAdvanceSearch {
    List<CategoriesAdvanceSearch>  categoriesAdvanceSearch;
    Set<String> brands;
    Set<String> address;

}
