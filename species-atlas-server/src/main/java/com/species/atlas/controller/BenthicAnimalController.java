package com.species.atlas.controller;

import com.species.atlas.dto.request.BatchDeleteRequest;
import com.species.atlas.dto.request.SpeciesCreateRequest;
import com.species.atlas.dto.request.SpeciesQueryRequest;
import com.species.atlas.dto.request.SpeciesUpdateRequest;
import com.species.atlas.dto.response.ApiResponse;
import com.species.atlas.dto.response.PageResponse;
import com.species.atlas.dto.response.SpeciesResponse;
import com.species.atlas.service.BenthicAnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 底栖动物控制器
 */
@RestController
@RequestMapping("/species/benthic-animal")
@RequiredArgsConstructor
@Tag(name = "底栖动物管理", description = "底栖动物数据的增删改查接口")
public class BenthicAnimalController {

    private final BenthicAnimalService service;

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "分页查询底栖动物列表，支持按名称等条件筛选")
    public ApiResponse<PageResponse<SpeciesResponse>> list(@Valid SpeciesQueryRequest request) {
        PageResponse<SpeciesResponse> response = service.list(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情", description = "根据 ID 获取底栖动物详情")
    public ApiResponse<SpeciesResponse> getById(
            @Parameter(description = "底栖动物ID") @PathVariable Long id
    ) {
        SpeciesResponse response = service.getById(id);
        return ApiResponse.success(response);
    }

    @PostMapping
    @Operation(summary = "新增", description = "新增底栖动物数据")
    public ApiResponse<SpeciesResponse> create(@Valid @RequestBody SpeciesCreateRequest request) {
        SpeciesResponse response = service.create(request);
        return ApiResponse.success("创建成功", response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新", description = "更新底栖动物数据")
    public ApiResponse<SpeciesResponse> update(
            @Parameter(description = "底栖动物ID") @PathVariable Long id,
            @Valid @RequestBody SpeciesUpdateRequest request
    ) {
        SpeciesResponse response = service.update(id, request);
        return ApiResponse.success("更新成功", response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除", description = "删除底栖动物数据（软删除）")
    public ApiResponse<Void> delete(
            @Parameter(description = "底栖动物ID") @PathVariable Long id
    ) {
        service.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除", description = "批量删除底栖动物数据")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        service.batchDelete(request.getIds());
        return ApiResponse.success("批量删除成功", null);
    }
}
