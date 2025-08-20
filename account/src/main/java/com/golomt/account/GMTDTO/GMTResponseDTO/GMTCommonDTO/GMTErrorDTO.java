package com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.golomt.account.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTGeneralDTO;

import java.util.List;
public class GMTErrorDTO implements GMTGeneralDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorDescription;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> errors;


    /**
     * Constructor
     */
    public GMTErrorDTO(String errorCode, String errorDescription, String errorType, List<String> errors) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.errorType = errorType;
        this.errors = errors;
    }
    public GMTErrorDTO(String errorCode, String errorDescription, String errorType) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.errorType = errorType;
    }
    public GMTErrorDTO(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }
    public GMTErrorDTO(String errorCode) {
        this.errorCode = errorCode;
    }



    /**
     * Getter Setter
     */

    public String getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public String getErrorDescription() {
        return errorDescription;
    }
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
    public String getErrorType() {
        return errorType;
    }
    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
    public List<String> getErrors() {
        return errors;
    }
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

}
