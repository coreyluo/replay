package com.bazinga.base;

import lombok.Data;

import java.util.List;

@Data
public class PagingResult<Q extends ShardingQuery, T> {

    private Q query;

    private List<T> list;

    private boolean isLastPage;

    public PagingResult(Q query, List<T> list, boolean isLastPage) {
        this.query = query;
        this.list = list;
        this.isLastPage = isLastPage;
    }


}
