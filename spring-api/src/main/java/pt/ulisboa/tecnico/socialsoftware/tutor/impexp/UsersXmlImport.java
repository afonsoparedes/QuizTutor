package pt.ulisboa.tecnico.socialsoftware.tutor.impexp;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.service.UserService;

import java.io.*;
import java.nio.charset.Charset;

public class UsersXmlImport {
	private UserService userService;

	public void importUsers(InputStream inputStream) {
		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);

		Document doc;
		try {
			Reader reader = new InputStreamReader(inputStream, Charset.defaultCharset());
			doc = builder.build(reader);
		} catch (FileNotFoundException e) {
			throw new TutorException(TutorException.ExceptionError.USERS_IMPORT_ERROR, "File not found");
		} catch (JDOMException e) {
			throw new TutorException(TutorException.ExceptionError.USERS_IMPORT_ERROR, "Coding problem");
		} catch (IOException e) {
			throw new TutorException(TutorException.ExceptionError.USERS_IMPORT_ERROR, "File type or format");
		}

		if (doc == null) {
			throw new TutorException(TutorException.ExceptionError.USERS_IMPORT_ERROR, "File not found ot format error");
		}

		importUsers(doc);
	}

	public void importUsers(String usersXml, UserService userService) {
		this.userService = userService;

		SAXBuilder builder = new SAXBuilder();
		builder.setIgnoringElementContentWhitespace(true);

		InputStream stream = new ByteArrayInputStream(usersXml.getBytes());

		importUsers(stream);
	}

	private void importUsers(Document doc) {
		XPathFactory xpfac = XPathFactory.instance();
		XPathExpression<Element> xp = xpfac.compile("//users/user", Filters.element());
		for (Element element : xp.evaluate(doc)) {
			String username = element.getAttributeValue("username");
			if (userService.findByUsername(username) == null) {
				String name = element.getAttributeValue("name");
				User.Role role = User.Role.valueOf(element.getAttributeValue("role"));

				User user = userService.create(name, username, role);

				if (element.getAttributeValue("year") != null) {
					user.setYear(Integer.valueOf(element.getAttributeValue("year")));
				}
			}
		}
	}

}
