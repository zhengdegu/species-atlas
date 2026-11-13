package com.species.atlas.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应")
public class PageResponse<T> {

    @Schema(description = "数据列表")
    private List<T> rows;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize;

    @Schema(description = "总页数", example = "5")
    private Integer pages;

    /**
     * 从 Spring Data Page 转换
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .rows(page.getContent())
                .total(page.getTotalElements())
                .pageNum(page.getNumber() + 1)  // Spring Data 页码从 0 开始
                .pageSize(page.getSize())
                .pages(page.getTotalPages())
                .build();
    }

    /**
     * 从列表和分页信息构建
     */
    public static <T> PageResponse<T> of(List<T> rows, long total, int pageNum, int pageSize) {
        int pages = (int) Math.ceil((double) total / pageSize);
        return PageResponse.<T>builder()
                .rows(rows)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .pages(pages)
                .build();
    }
}
