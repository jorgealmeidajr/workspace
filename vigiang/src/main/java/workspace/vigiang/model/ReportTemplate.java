package workspace.vigiang.model;

import java.util.Objects;

public class ReportTemplate {

    private final String reportCode;
    private final String reportId;
    private final String reportType;
    private final String carrierCode;
    private final String carrierName;
    private final byte[] template;

    public ReportTemplate(String reportCode, String reportId, String reportType, String carrierCode, String carrierName, byte[] template) {
        this.reportCode = ObjectUtils.requireNonBlank(reportCode, "Report code must not be blank");
        this.reportId = ObjectUtils.requireNonBlank(reportId, "Report id must not be blank");
        this.reportType = ObjectUtils.requireNonBlank(reportType, "Report type must not be blank");
        this.carrierCode = Objects.requireNonNull(carrierCode, "carrierCode cannot be null");
        this.carrierName = carrierName;
        this.template = template;
    }

    public String getReportCode() {
        return reportCode;
    }

    public String getReportId() {
        return reportId;
    }

    public String getReportType() {
        return reportType;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public byte[] getTemplate() {
        return template;
    }

}
