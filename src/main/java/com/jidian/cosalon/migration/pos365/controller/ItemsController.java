package com.jidian.cosalon.migration.pos365.controller;

import com.jidian.cosalon.migration.pos365.service.ItemsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"Items API"})
@RestController
@RequestMapping("/api/v1/items")
public class ItemsController {

    @Autowired
    private ItemsService service;

    @ApiOperation("Tạo mới items(dịch vụ) fetch dữ liệu từ POS365")
    @PostMapping("/fetch")
    public ResponseEntity createCategory() {
        try {
            return ResponseEntity.ok(service.createFetchingItem());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
