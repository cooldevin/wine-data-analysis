package com.example.sales.service;

import com.example.sales.entity.Customer;
import com.example.sales.repository.CustomerRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Page<Customer> findCustomers(String name, Pageable pageable) {
        if (StringUtils.isBlank(name)) {
            return customerRepository.findAll(pageable);
        }
        return customerRepository.findByNameContaining(name, pageable);
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        if (customer.getId() != null) {
            Customer existing = customerRepository.findById(customer.getId())
                .orElseThrow(() -> new IllegalArgumentException("客户不存在"));
            existing.setName(customer.getName());
            existing.setContact(customer.getContact());
            existing.setPhone(customer.getPhone());
            existing.setEmail(customer.getEmail());
            existing.setAddress(customer.getAddress());
            return customerRepository.save(existing);
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("客户不存在");
        }
        customerRepository.deleteById(id);
    }
}
