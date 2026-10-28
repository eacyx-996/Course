package com.example.spring.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页返回结构
 */
@Data
public class PageVO<T> {

    private Long total;
    private Long pages;
    private Long pageNum;
    private Long pageSize;
    private List<T> list;

    public static <T> PageVO<T> of(Page<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.setTotal(page.getTotal());
        vo.setPages(page.getPages());
        vo.setPageNum(page.getCurrent());
        vo.setPageSize(page.getSize());
        vo.setList(page.getRecords());
        return vo;
    }
}
