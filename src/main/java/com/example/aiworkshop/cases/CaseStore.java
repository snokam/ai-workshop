package com.example.aiworkshop.cases;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Where Cases live. In memory, so everything is lost on restart.
 *
 * <p>Deliberately the same shape as {@code DocumentStore}: these are the two classes to replace when
 * persistence arrives, and nothing above them knows how storage works.
 */
@Component
public class CaseStore {

    private final ConcurrentMap<String, Case> cases = new ConcurrentHashMap<>();

    public void save(Case aCase) {
        cases.put(aCase.id(), aCase);
    }

    public Optional<Case> findById(String id) {
        return Optional.ofNullable(cases.get(id));
    }

    /** By reference, so a Case Handler's list does not reshuffle itself between page loads. */
    public List<Case> findAll() {
        return cases.values().stream().sorted(Comparator.comparing(Case::reference)).toList();
    }

    public void deleteAll() {
        cases.clear();
    }
}
