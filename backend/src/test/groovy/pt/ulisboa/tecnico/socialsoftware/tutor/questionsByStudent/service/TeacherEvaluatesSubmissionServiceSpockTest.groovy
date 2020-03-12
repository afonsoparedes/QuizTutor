package pt.ulisboa.tecnico.socialsoftware.tutor.questionsByStudent.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.*
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsByStudent.QuestionsByStudentService
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsByStudent.Submission
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsByStudent.SubmissionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsByStudent.TeacherEvaluatesSubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class TeacherEvaluatesSubmissionServiceSpockTest extends Specification{
    static final String COURSE_ONE ="CourseOne"
    static final String WRONG_COURSE = "WrongCourse"
    static final int QUESTION_ID =10
    static final int QUESTION_KEY =14
    static final int SUBMISSION_ID =14

    static final String ACRONYM =14
    static final String ACADEMIC_TERM =14
    static final String NAME = "Rito"
    static final String USERNAME = "Silva"
    static final int KEY = 10

    @Autowired
    TeacherEvaluatesSubmissionService teacherEvaluatesSubmissionService

    @Autowired
     CourseRepository courseRepository;

    @Autowired
    CourseExecutionRepository courseExecutionRepository;

    @Autowired
    SubmissionRepository submissionRepository

    @Autowired
     QuestionRepository questionRepository;

    @Autowired
     TopicRepository topicRepository;

    @Autowired
     UserRepository userRepository;

    def setup() {

    }


    //fUNC 2
    def "the user is not a teacher"() {
        given: "a user"
        def user = new User(NAME, USERNAME, KEY, User.Role.STUDENT)
        userRepository.save(user)
        and: "a course"
        def course = new Course(COURSE_ONE, Course.Type.TECNICO)
        and: "a question"
        def question = new Question()
        question.setKey(QUESTION_KEY)
        question.setCourse(course)
        and: "a submission"
        def submission = new Submission(question, user.getId())
        submission.setId(SUBMISSION_ID)
        submissionRepository.save(submission)

        when:

        teacherEvaluatesSubmissionService.teacherEvaluatesQuestion(user, submission.getId())

        then:
        thrown(TutorException)
    }

    def "the submission does not exist in the repository"() {
        given: "a user"
        def user = new User(NAME, USERNAME, KEY, User.Role.TEACHER)
        userRepository.save(user)
        and: "a course"
        def course = new Course(COURSE_ONE, Course.Type.TECNICO)
        and: "a question"
        def question = new Question()
        question.setKey(QUESTION_KEY)
        question.setCourse(course)
        and: "a submission"
        def submission = new Submission(question, user.getId())
        submission.setId(SUBMISSION_ID)

        when:

        teacherEvaluatesSubmissionService.teacherEvaluatesQuestion(user, submission.getId())

        then:
        thrown(TutorException)
    }

    def "the professor and submission exist and approves submission"()  {
        given: "a user"
        def user = new User(NAME, USERNAME, KEY, User.Role.TEACHER)
        userRepository.save(user)
        and: "a course"
        def course = new Course(COURSE_ONE, Course.Type.TECNICO)
        courseRepository.save(course)
        and: "a course execution"
        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        Set<CourseExecution> set = new HashSet<CourseExecution>()
        set.add(courseExecution)
        user.setCourseExecutions(set)
        and: "a question"
        def question = new Question()
        question.setKey(QUESTION_KEY)
        question.setCourse(course)
        questionRepository.save(question);
        and: "a submission"
        def submission = new Submission(question, user.getId())
        submissionRepository.save(submission)

        when:
        def result = teacherEvaluatesSubmissionService.teacherEvaluatesQuestion(user, submission.getId())

        then: "the returned data are correct"
        result.getStatus().toString() == "APPROVED"
        and: "submission approved"
    }

    def "the professor and submission exist and rejects submission"()  {
        given: "a user"
        def user = new User(NAME, USERNAME, KEY, User.Role.TEACHER)
        userRepository.save(user)
        and: "a course"
        def course = new Course(WRONG_COURSE, Course.Type.TECNICO)
        courseRepository.save(course)
        and: "a course execution"
        def courseExecution = new CourseExecution(course, COURSE_ONE, COURSE_ONE, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        Set<CourseExecution> set = new HashSet<CourseExecution>()
        set.add(courseExecution)
        user.setCourseExecutions(set)
        and: "a question"
        def question = new Question()
        question.setKey(QUESTION_ID)
        question.setCourse(course)
        questionRepository.save(question);
        and: "a submission"
        def submission = new Submission(question, user.getId())
        submissionRepository.save(submission)

        when:
        def result = teacherEvaluatesSubmissionService.teacherEvaluatesQuestion(user, submission.getId())

        then: "the returned data are correct"
        result.getStatus().toString() == "APPROVED"
        and: "submission approved"
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TeacherEvaluatesSubmissionService teacherEvaluatesSubmissionService() {
            return new TeacherEvaluatesSubmissionService()
        }
    }
}