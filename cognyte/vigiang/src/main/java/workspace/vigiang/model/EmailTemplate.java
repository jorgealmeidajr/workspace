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
    private final String carrierName;

    public EmailTemplate(String carrierId, String id, String subject, String name, String fileName, String emailFrom, String emailTo, String body, String carrierName) {
        this.carrierId = ObjectUtils.requireNonBlank(carrierId, "Carrier ID must not be blank");
        this.id = ObjectUtils.requireNonBlank(id, "ID must not be blank");
        this.subject = subject;
        this.name = name;
        this.fileName = fileName;
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.body = (body == null) ? " " : body;
        this.carrierName = carrierName;
    }

    public String[] toArray() {
        return new String[] { carrierId, carrierName, id, subject, name, fileName, emailFrom, emailTo };
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

    public String getCarrierName() {
        return carrierName;
    }

}
