package pt.ulisboa.tecnico.socialsoftware.tutor.questionsByStudent;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsByStudent.SubmissionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;


@Service
public class QuestionsByStudentService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;

    //PpA - Feature 1
    @Transactional( isolation = Isolation.REPEATABLE_READ)
    public QuestionDto studentSubmitQuestion(int courseId, QuestionDto questionDto) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));

        verification(questionDto);

        Question question = new Question(course, questionDto);
        question.setCreationDate(LocalDateTime.now());
        this.entityManager.persist(question);
        QuestionDto qDto = new QuestionDto(question);
        qDto.setStatus("ONHOLD");
        return qDto;
    }

    private void verification(QuestionDto questionDto) {
        if (questionDto.getKey() == null) {
            int maxQuestionNumber = questionRepository.getMaxQuestionNumber() != null ?
                    questionRepository.getMaxQuestionNumber() : 0;
            questionDto.setKey(maxQuestionNumber + 1);
        }
    }

    //PpA - Feature 2
    public void makeSubmissionApproved(SubmissionDto submission, String justification){
        submission.setStatus("APPROVED");
        submission.setJustification(justification);
    }
    public void makeSubmissionRejected(SubmissionDto submission,  String justification){
        submission.setStatus("REJECTED");
        submission.setJustification(justification);
    }


    public SubmissionDto teacherEvaluatesQuestion(User user, int submissionId) {
        //user é prof?
        isTeacher(user);

        Submission submission = submissionRepository.findById(submissionId).orElseThrow(() -> new TutorException(SUBMISSION_NOT_FOUND, submissionId));
        Question question = submission.getQuestion();
        Course course = question.getCourse();
        Set cexec = user.getCourseExecutions();
        SubmissionDto submissionDto = new SubmissionDto(submission);
        return makeDecision(course, cexec, submissionDto);
    }

    private SubmissionDto makeDecision(Course course, Iterable<CourseExecution> cexec, SubmissionDto submissionDto) {
        for (CourseExecution f : cexec) {

            if (f.getCourse().equals(course)) {
                makeSubmissionApproved(submissionDto, "Question weel structered and correct");

                return submissionDto;
            }

        }
        makeSubmissionRejected(submissionDto, "Teacher is not assigned to this course");
        return submissionDto;
    }

    private void isTeacher(User user) {
        if (!user.getRole().toString().equals("TEACHER")) {
            throw new TutorException(NOT_TEACHER_ERROR);
        }
    }

    //PpA - Feature 3
    public List<SubmissionDto> findQuestionsSubmittedByStudent(int userID) {

        return submissionRepository.findSubmissionByStudent(userID).stream().map(SubmissionDto::new).collect(Collectors.toList());
    }
}
