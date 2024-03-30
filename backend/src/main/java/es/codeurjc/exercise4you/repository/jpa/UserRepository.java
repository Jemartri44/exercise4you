package es.codeurjc.exercise4you.repository.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.Usr;

@Repository
public interface UserRepository extends JpaRepository<Usr, Integer>{
    Optional<Usr> findByEmail(String email);
}
