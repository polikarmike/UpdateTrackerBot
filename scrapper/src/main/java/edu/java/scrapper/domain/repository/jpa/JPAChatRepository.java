package edu.java.scrapper.domain.repository.jpa;

import edu.java.scrapper.dto.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPAChatRepository extends JpaRepository<Chat, Long> {

}
