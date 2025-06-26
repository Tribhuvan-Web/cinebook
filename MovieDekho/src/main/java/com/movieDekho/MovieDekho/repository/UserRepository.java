package com.movieDekho.MovieDekho.repository;

import com.movieDekho.MovieDekho.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
