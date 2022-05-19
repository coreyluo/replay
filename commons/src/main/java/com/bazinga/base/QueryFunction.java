package com.bazinga.base;

import java.util.List;

/**
 * 〈查询函数〉<p>
 * 〈功能详细描述〉
 *
 * @author zixiao
 * @date 18/11/29
 */
public interface QueryFunction<T> {

    List<T> query();
}
