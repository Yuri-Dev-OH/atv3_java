package com.autobots.automanager.repositorios;

import com.autobots.automanager.entidades.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepositorio extends JpaRepository<Email, Long> {}
