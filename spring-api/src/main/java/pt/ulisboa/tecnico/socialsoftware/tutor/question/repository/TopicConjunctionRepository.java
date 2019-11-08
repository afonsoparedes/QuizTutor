package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction;

@Repository
public interface TopicConjunctionRepository extends JpaRepository<TopicConjunction, Integer> {

}