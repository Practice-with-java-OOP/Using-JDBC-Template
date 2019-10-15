package com.jidian.cosalon.migration.pos365.controller;

import com.jidian.cosalon.migration.pos365.retrofitservice.Pos365RetrofitService;
import com.jidian.cosalon.migration.pos365.service.CategoriesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"Categories API"})
@RestController
@RequestMapping("/api/v1/categories")
public class CategoriesController {

    @Autowired
    private CategoriesService service;

    @ApiOperation("Tạo mới category(nhóm sản phẩm/dịch vụ) fetch dữ liệu từ POS365")
    @PostMapping("/fetch")
    public ResponseEntity createCategory() {
        try {
            return ResponseEntity.ok(service.createFetchingCategory());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
