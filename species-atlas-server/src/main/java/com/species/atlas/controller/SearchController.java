package com.species.atlas.controller;

import com.species.atlas.dto.response.ApiResponse;
import com.species.atlas.dto.response.SearchResultResponse;
import com.species.atlas.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/species")
@RequiredArgsConstructor
@Tag(name = "物种搜索", description = "跨类别搜索接口")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    @Operation(summary = "跨类别搜索", description = "在所有物种类别中搜索")
    public ApiResponse<List<SearchResultResponse>> search(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "每类返回数量") @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        List<SearchResultResponse> results = searchService.searchSpecies(keyword, pageSize);
        return ApiResponse.success(results);
    }
}
