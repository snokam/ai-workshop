package com.example.aiworkshop.cases.store;

import com.example.aiworkshop.cases.model.Case;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class CaseStore {
    private final ConcurrentMap<String, Case> cases = new ConcurrentHashMap<>();

    public void save(Case aCase) {
        cases.put(aCase.id(), aCase);
    }

    public Optional<Case> findById(String id) {
        return Optional.ofNullable(cases.get(id));
    }

    public List<Case> findAll() {
        return cases.values().stream().sorted(Comparator.comparing(Case::reference)).toList();
    }

    public void deleteAll() {
        cases.clear();
    }
}
