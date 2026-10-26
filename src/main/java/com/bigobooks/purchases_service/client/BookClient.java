package com.bigobooks.purchases_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bigobooks.dto.BookResponse;

@FeignClient(name = "stock-service")
public interface BookClient {

    @GetMapping("/books/{id}")
    BookResponse getBookById(@PathVariable("id") Long id);
}