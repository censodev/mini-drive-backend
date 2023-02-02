package com.censodev.minidrive.utils;

import com.censodev.minidrive.data.dto.PageReq;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

public class PageUtil {
    public static Pageable getPageReq(PageReq pageReq) {
        Integer page = Optional.ofNullable(pageReq.getPage()).orElse(0);
        Integer size = Optional.ofNullable(pageReq.getLimit()).orElse(10);
        String order = Optional.ofNullable(pageReq.getOrder()).orElse("desc");
        String orderBy = Optional.ofNullable(pageReq.getOrderBy()).orElse("id");
        Sort sort = order.equals("asc")
                ? Sort.by(orderBy).ascending()
                : Sort.by(orderBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
