package workspace.vigiang.model;

public class EmailTemplate {

    private final String carrierId;
    private final String id;
    private final String subject;
    private final String name;
    private final String fileName;
    private final String emailFrom;
    private final String emailTo;
    private final String body;

    public EmailTemplate(String carrierId, String id, String subject, String name, String fileName, String emailFrom, String emailTo, String body) {
        this.carrierId = carrierId;
        this.id = id;
        this.subject = subject;
        this.name = name;
        this.fileName = fileName;
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.body = body;
    }

    public String[] toArray() {
        return new String[] { carrierId, id, subject, name, fileName, emailFrom, emailTo, body };
    }

    public String getCarrierId() {
        return carrierId;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public String getBody() {
        return body;
    }

}
