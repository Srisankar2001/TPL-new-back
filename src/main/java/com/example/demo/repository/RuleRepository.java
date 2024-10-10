package com.example.demo.repository;

import com.example.demo.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuleRepository extends JpaRepository<Rule,Integer> {
    Optional<Rule> findByName(String name);
}
