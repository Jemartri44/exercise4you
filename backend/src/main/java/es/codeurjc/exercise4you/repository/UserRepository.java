package es.codeurjc.exercise4you.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.exercise4you.entity.Usr;

public interface UserRepository extends JpaRepository<Usr, Integer>{
    Optional<Usr> findByEmail(String email);
}
