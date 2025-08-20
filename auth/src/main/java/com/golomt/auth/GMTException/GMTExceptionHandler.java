package com.golomt.auth.GMTException;

import com.golomt.auth.GMTConstant.GMTException;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTErrorDTO;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTHeaderDTO;
import com.golomt.auth.GMTDTO.GMTResponseDTO.GMTCommonDTO.GMTResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GMTExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + "|" + error.getDefaultMessage())
                .collect(Collectors.toList());

        GMTErrorDTO errorDTO = new GMTErrorDTO(
                String.valueOf(status.value()),
                ex.getMessage(),
                GMTException.VALIDATION.value(),
                fieldErrors
        );

        GMTHeaderDTO header = new GMTHeaderDTO();
        header.setStatus("ERROR");
        header.setMessage("Validation failed");
        header.setErrorCode(status.value());

        GMTResponseDTO responseDTO = new GMTResponseDTO(header, errorDTO);

        return new ResponseEntity<>(responseDTO, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GMTResponseDTO> handleRuntimeException(RuntimeException ex) {
        GMTErrorDTO errorDTO = new GMTErrorDTO(
                "500",
                ex.getMessage(),
                GMTException.RUN_TIME.value()
        );

        GMTHeaderDTO header = new GMTHeaderDTO();
        header.setStatus("ERROR");
        header.setMessage("Runtime error occurred");
        header.setErrorCode(500);

        GMTResponseDTO responseDTO = new GMTResponseDTO(header, errorDTO);

        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GMTValidationException.class)
    public ResponseEntity<GMTResponseDTO> handleValidationException(GMTValidationException ex) {
        GMTErrorDTO errorDTO = new GMTErrorDTO(
                "400",
                ex.getMessage(),
                GMTException.VALIDATION.value()
        );

        GMTHeaderDTO header = new GMTHeaderDTO();
        header.setStatus("ERROR");
        header.setMessage("Validation failed");
        header.setErrorCode(400);

        GMTResponseDTO responseDTO = new GMTResponseDTO(header, errorDTO);

        return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(GMTCustomException.class)
    public ResponseEntity<GMTResponseDTO> handleCustomException(GMTCustomException ex) {
        // Determine appropriate HTTP status based on exception message
        HttpStatus httpStatus = determineHttpStatus(ex.getMessage());

        GMTErrorDTO errorDTO = new GMTErrorDTO(
                String.valueOf(httpStatus.value()),
                ex.getMessage(),
                GMTException.CUSTOM.value()
        );

        GMTHeaderDTO header = new GMTHeaderDTO();
        header.setStatus("ERROR");
        header.setMessage("Custom error occurred");
        header.setErrorCode(httpStatus.value());

        GMTResponseDTO responseDTO = new GMTResponseDTO(header, errorDTO);

        return new ResponseEntity<>(responseDTO, httpStatus);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GMTResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        GMTErrorDTO errorDTO = new GMTErrorDTO(
                "400",
                ex.getMessage(),
                GMTException.API_PARAM.value()
        );

        GMTHeaderDTO header = new GMTHeaderDTO();
        header.setStatus("ERROR");
        header.setMessage("Invalid parameter");
        header.setErrorCode(400);

        GMTResponseDTO responseDTO = new GMTResponseDTO(header, errorDTO);

        return new ResponseEntity<>(responseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GMTResponseDTO> handleGenericException(Exception ex) {
        GMTErrorDTO errorDTO = new GMTErrorDTO(
                "500",
                "An unexpected error occurred",
                GMTException.RUN_TIME.value()
        );

        GMTHeaderDTO header = new GMTHeaderDTO();
        header.setStatus("ERROR");
        header.setMessage("Internal server error");
        header.setErrorCode(500);

        GMTResponseDTO responseDTO = new GMTResponseDTO(header, errorDTO);

        return new ResponseEntity<>(responseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Determines appropriate HTTP status based on exception message content
     */
    private HttpStatus determineHttpStatus(String message) {
        if (message == null) {
            return HttpStatus.BAD_REQUEST;
        }

        String lowerMessage = message.toLowerCase();

        // Not Found cases (404)
        if (lowerMessage.contains("not found") ||
                lowerMessage.contains("олдсонгүй") ||
                lowerMessage.contains("user not found") ||
                lowerMessage.contains("хэрэглэгч олдсонгүй")) {
            return HttpStatus.NOT_FOUND;
        }

        // Unauthorized cases (401)
        if (lowerMessage.contains("нэвтрэх нэр буруу") ||
                lowerMessage.contains("нууц үг буруу") ||
                lowerMessage.contains("unauthorized") ||
                lowerMessage.contains("токен олдсонгүй")) {
            return HttpStatus.UNAUTHORIZED;
        }

        // Conflict cases (409)
        if (lowerMessage.contains("ашиглагдсан") ||
                lowerMessage.contains("already exists") ||
                lowerMessage.contains("duplicate")) {
            return HttpStatus.CONFLICT;
        }

        // Forbidden cases (403)
        if (lowerMessage.contains("forbidden") ||
                lowerMessage.contains("хандах эрхгүй")) {
            return HttpStatus.FORBIDDEN;
        }

        // Default to Bad Request (400)
        return HttpStatus.BAD_REQUEST;
    }
}