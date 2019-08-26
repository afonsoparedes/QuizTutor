package pt.ulisboa.tecnico.socialsoftware.tutor.impexp;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.service.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QuestionsXmlImport {
	private QuestionService questionService;

	public void importQuestions(InputStream inputStream) {
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);

		Document doc;
		try {
			Reader reader = new InputStreamReader(inputStream, Charset.defaultCharset());
			doc = builder.build(reader);
		} catch (FileNotFoundException e) {
			throw new TutorException(TutorException.ExceptionError.QUESTIONS_IMPORT_ERROR, "File not found");
		} catch (JDOMException e) {
			throw new TutorException(TutorException.ExceptionError.QUESTIONS_IMPORT_ERROR, "Coding problem");
		} catch (IOException e) {
			throw new TutorException(TutorException.ExceptionError.QUESTIONS_IMPORT_ERROR, "File type or format");
		}

		if (doc == null) {
			throw new TutorException(TutorException.ExceptionError.QUESTIONS_IMPORT_ERROR, "File not found ot format error");
		}

		importQuestions(doc);
	}

	public void importQuestions(String questionsXml, QuestionService questionService) {
		this.questionService = questionService;

		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);

		InputStream stream = new ByteArrayInputStream(questionsXml.getBytes());

		importQuestions(stream);
	}

	private void importQuestions(Document doc) {
		XPathFactory xpfac = XPathFactory.instance();
		XPathExpression<Element> xp = xpfac.compile("//questions/question", Filters.element());
		for (Element element : xp.evaluate(doc)) {
			importQuestion(element);
		}
	}

	private void importQuestion(Element questionElement) {
		Integer number = Integer.valueOf(questionElement.getAttributeValue("number"));
		String content = questionElement.getAttributeValue("content");
		String title = questionElement.getAttributeValue("title");
		Boolean active = Boolean.valueOf(questionElement.getAttributeValue("active"));

		QuestionDto questionDto = new QuestionDto();
		questionDto.setNumber(number);
		questionDto.setContent(content);
		questionDto.setTitle(title);
		questionDto.setActive(active);

		Element imageElement = questionElement.getChild("image");
		if (imageElement != null) {
			Integer width = imageElement.getAttributeValue("width") != null ?
					Integer.valueOf(imageElement.getAttributeValue("width")) : null;
			String url = imageElement.getAttributeValue("url");

			ImageDto imageDto = new ImageDto();
			imageDto.setWidth(width);
			imageDto.setUrl(url);

			questionDto.setImage(imageDto);
		}

		List<OptionDto> optionDtos = new ArrayList<>();
		for (Element optionElement : questionElement.getChild("options").getChildren("option")) {
			Integer optionNumber = Integer.valueOf( optionElement.getAttributeValue("number"));
			String optionContent = optionElement.getAttributeValue("content");
			Boolean correct = Boolean.valueOf(optionElement.getAttributeValue("correct"));

			OptionDto optionDto = new OptionDto();
			optionDto.setNumber(optionNumber);
			optionDto.setContent(optionContent);
			optionDto.setCorrect(correct);

			optionDtos.add(optionDto);
		}
		questionDto.setOptions(optionDtos);

		questionService.createQuestion(questionDto);
	}

}
