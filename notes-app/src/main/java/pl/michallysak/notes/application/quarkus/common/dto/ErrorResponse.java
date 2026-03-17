package pl.michallysak.notes.application.quarkus.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for error cases")
public class ErrorResponse {
    @Schema(description = "Status message", example = "An error occurred while processing the request")
    private String message;
}
