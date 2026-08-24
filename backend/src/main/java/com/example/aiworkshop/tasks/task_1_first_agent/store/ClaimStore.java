package com.example.aiworkshop.tasks.task_1_first_agent.store;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class ClaimStore {
    private final ConcurrentMap<String, Claim> claims = new ConcurrentHashMap<>();

    public void save(Claim aClaim) {
        claims.put(aClaim.id(), aClaim);
    }

    public Optional<Claim> findById(String id) {
        return Optional.ofNullable(claims.get(id));
    }

    public List<Claim> findAll() {
        return claims.values().stream().sorted(Comparator.comparing(Claim::reference)).toList();
    }

    public void deleteAll() {
        claims.clear();
    }
}
