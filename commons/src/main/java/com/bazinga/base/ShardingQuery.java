package com.bazinga.base;

import lombok.Data;

@Data
public class ShardingQuery extends  PagingQuery{

    protected String tableSuffix;

    public  Boolean checkIndex;

    protected  Integer tableIndex;
}
