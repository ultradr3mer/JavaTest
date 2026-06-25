package com.example.demo;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counter")
@CrossOrigin(origins = "http://localhost:5173")
public class CounterController {

    private final AtomicInteger counter = new AtomicInteger(0);

    @GetMapping
    public Map<String, Integer> getCounter() {
        return Map.of("count", counter.get());
    }

    @PostMapping("/increment")
    public Map<String, Integer> increment() {
        return Map.of("count", counter.incrementAndGet());
    }

    @PostMapping("/reset")
    public Map<String, Integer> reset() {
        counter.set(0);
        return Map.of("count", counter.get());
    }
}
