package za.ac.styling.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class ResponseUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);

    public static ResponseEntity<?> success(Object data) {
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    public static ResponseEntity<?> success(String message) {
        return ResponseEntity.ok(Map.of("success", true, "message", message));
    }

    public static ResponseEntity<?> success(Object data, String message) {
        return ResponseEntity.ok(Map.of("success", true, "data", data, "message", message));
    }

    public static ResponseEntity<?> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", message));
    }

    public static ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", message));
    }

    public static ResponseEntity<?> error(String context, Exception e) {
        logger.error("{}: {}", context, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", context + ": " + e.getMessage()));
    }

    public static ResponseEntity<?> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", message));
    }

    public static ResponseEntity<?> created(Object data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", data));
    }

    public static ResponseEntity<?> created(Object data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", data, "message", message));
    }
}
