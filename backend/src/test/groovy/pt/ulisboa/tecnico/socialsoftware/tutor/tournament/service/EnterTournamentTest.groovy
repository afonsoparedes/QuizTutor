package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament.TournamentState
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.format.DateTimeFormatter

@DataJpaTest
class EnterTournamentTest extends Specification{
    public static final String TOURNAMENT_TITLE = "Tournament"
    public static final String CREATION_DATE = "2020-09-22 12:12"
    public static final String AVAILABLE_DATE = "2020-09-23 12:12"
    public static final String CONCLUSION_DATE = "2020-09-24 12:12"
    public static final Integer ID = 2
    public static final TournamentState STATE = TournamentState.OPEN
    public static final User USER = new User("Pedro","Minorca",2, User.Role.STUDENT)

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    UserRepository userRepository


    def formatter

    def setup(){

    }

    def "Student enters in a closed tournament"(){

        given: "a tournamentDto"
        def tournamentDto = getTournamentDto(TournamentState.CLOSED)

        and: "a student"
        def student = new User('name', "username", 32, User.Role.STUDENT)
        userRepository.save(student)

        when:
        tournamentService.enrollInTournament(student, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_IS_NOT_OPEN

    }

    def "Teacher enters in an open tournament"(){
        given: "a tournamentDto"
        def tournamentDto = getTournamentDto(TournamentState.CLOSED)


        and: "a teacher"
        def teacher = new User('name', "username", 32, User.Role.TEACHER)
        userRepository.save(teacher)

        when:
        tournamentService.enrollInTournament(teacher, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_IS_NOT_STUDENT
    }


    def "Admin enters in an open tournament"(){
        given: "a tournamentDto"
        def tournamentDto = getTournamentDto(TournamentState.CLOSED)


        and: "an admin"
        def admin = new User('name', "username", 32, User.Role.ADMIN)
        userRepository.save(admin)

        when:
        tournamentService.enrollInTournament(admin, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_IS_NOT_STUDENT

    }

    def "Demo admin enters in an open tournament"(){
        given: "a tournamentDto"
        def tournamentDto = getTournamentDto(TournamentState.CLOSED)


        and: "a demo"
        def demo = new User('name', "username", 32, User.Role.DEMO_ADMIN)
        userRepository.save(demo)

        when:
        tournamentService.enrollInTournament(demo, tournamentDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_IS_NOT_STUDENT


    }

    def "Reentering an already enrolled tournament"() {
        given: "a tournamentDto"
        def tournamentDto = getTournamentDto(TournamentState.OPEN)

        and: "a student"
        def student = new User('name', "username", 32, User.Role.STUDENT)
        userRepository.save(student)

        when:
        def result = tournamentService.enrollInTournament(student, tournamentDto)
        tournamentService.enrollInTournament(student, result)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_IS_ALREADY_ENROLLED

    }

        private TournamentDto getTournamentDto(TournamentState state) {
        def tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setAvailableDate(AVAILABLE_DATE)
        tournamentDto.setConclusionDate(CONCLUSION_DATE)
        tournamentDto.setCreationDate(CREATION_DATE)
        tournamentDto.setId(ID)
        tournamentDto.setState(state)
        tournamentDto.setTournametCreator(USER)
        return tournamentDto
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}