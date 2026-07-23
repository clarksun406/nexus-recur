package com.nexus.recur.interfaces.rest;

import com.nexus.recur.application.service.CustomerService;
import com.nexus.recur.interfaces.rest.common.ApiResponse;
import com.nexus.recur.interfaces.rest.dto.CustomerDtos.*;
import com.nexusflow.permission.client.CheckPermission;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @CheckPermission("customer:create")
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.ok(customerService.create(request));
    }

    @GetMapping
    @CheckPermission("customer:read")
    public ApiResponse<Page<CustomerResponse>> list(
            @RequestParam String merchantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(customerService.list(merchantId, page, limit));
    }

    @GetMapping("/{customerId}")
    @CheckPermission("customer:read")
    public ApiResponse<CustomerResponse> get(@PathVariable String customerId) {
        return ApiResponse.ok(customerService.get(customerId));
    }

    @PutMapping("/{customerId}")
    @CheckPermission("customer:update")
    public ApiResponse<CustomerResponse> update(@PathVariable String customerId,
                                                @Valid @RequestBody UpdateCustomerRequest request) {
        return ApiResponse.ok(customerService.update(customerId, request));
    }
}
