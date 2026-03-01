package com.example.check.controller;
import com.example.check.core.InterfaceEngine;
import com.example.check.common.Result;
import com.example.check.common.WhiteListConfig;
import com.example.check.dto.RequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UniversalController {
    private final InterfaceEngine engine;
    @PostMapping("/{interfaceName}")
    public Result<?> invoke(
            @PathVariable String interfaceName,
            @RequestBody RequestDTO request,
            @RequestBody(required = false) WhiteListConfig whiteList) {
        return engine.execute(interfaceName, request, whiteList);
    }
}

