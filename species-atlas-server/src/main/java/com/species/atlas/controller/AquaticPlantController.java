package com.species.atlas.controller;

import com.species.atlas.dto.request.BatchDeleteRequest;
import com.species.atlas.dto.request.SpeciesCreateRequest;
import com.species.atlas.dto.request.SpeciesQueryRequest;
import com.species.atlas.dto.request.SpeciesUpdateRequest;
import com.species.atlas.dto.response.ApiResponse;
import com.species.atlas.dto.response.PageResponse;
import com.species.atlas.dto.response.SpeciesResponse;
import com.species.atlas.service.AquaticPlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/species/aquatic-plant")
@RequiredArgsConstructor
@Tag(name = "水生植物管理", description = "水生植物数据的增删改查接口")
public class AquaticPlantController {

    private final AquaticPlantService service;

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "分页查询水生植物列表")
    public ApiResponse<PageResponse<SpeciesResponse>> list(@Valid SpeciesQueryRequest request) {
        return ApiResponse.success(service.list(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情", description = "根据 ID 获取水生植物详情")
    public ApiResponse<SpeciesResponse> getById(@Parameter(description = "ID") @PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增", description = "新增水生植物数据")
    public ApiResponse<SpeciesResponse> create(@Valid @RequestBody SpeciesCreateRequest request) {
        return ApiResponse.success("创建成功", service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新", description = "更新水生植物数据")
    public ApiResponse<SpeciesResponse> update(
            @Parameter(description = "ID") @PathVariable Long id,
            @Valid @RequestBody SpeciesUpdateRequest request) {
        return ApiResponse.success("更新成功", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除", description = "删除水生植物数据")
    public ApiResponse<Void> delete(@Parameter(description = "ID") @PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除", description = "批量删除水生植物数据")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        service.batchDelete(request.getIds());
        return ApiResponse.success("批量删除成功", null);
    }
}
