package org.retrade.common.model.dto.request;

import lombok.Builder;
import lombok.Data;
import org.retrade.common.model.constant.QueryOperatorEnum;

@Data
@Builder
public class QueryFieldWrapper {
    private Object value;
    private QueryOperatorEnum operator;
}
