package com.fast.homestay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class StockRecord {
    private Long recordId;
    private String productId;
    private Long supplierId;
    private Integer quantity;
    private BigDecimal purchasePrice;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date entryTime; // 送货时间
}
