package com.example.sales.controller;


import com.example.sales.entity.Customer;
import com.example.sales.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.apache.shiro.authz.annotation.RequiresRoles;
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }



    @GetMapping("/api/list")
    @ResponseBody
    @RequiresRoles("ROLE_USER")
    public ResponseEntity<Page<Customer>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(customerService.findCustomers(name, PageRequest.of(page, size)));
    }

    @PostMapping("/api/save")
    @ResponseBody
    @RequiresRoles("ROLE_ADMIN")
    public ResponseEntity<?> saveCustomer(@RequestBody Customer customer) {
        try {
            Customer saved = customerService.saveCustomer(customer);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    @RequiresRoles("ROLE_ADMIN")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
