package com.ricardo.demo.dto;

import java.util.List;

public class TransactionListDto {
    private long pageSizeTotal;
    private long pageNoTotal;
    private List<TransactionDto> data;

    public long getPageSizeTotal() {
        return pageSizeTotal;
    }

    public void setPageSizeTotal(long pageSizeTotal) {
        this.pageSizeTotal = pageSizeTotal;
    }

    public long getPageNoTotal() {
        return pageNoTotal;
    }

    public void setPageNoTotal(long pageNoTotal) {
        this.pageNoTotal = pageNoTotal;
    }

    public List<TransactionDto> getData() {
        return data;
    }

    public void setData(List<TransactionDto> data) {
        this.data = data;
    }
}
