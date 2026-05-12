package pl.michallysak.notes.application.quarkus.note.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paged response with pagination metadata")
public class PagedResponse<T> {
  @Schema(required = true, description = "List of items in the current page")
  private List<T> data;

  @Schema(required = true, description = "Current page number (0-indexed)", examples = "0")
  private long page;

  @Schema(required = true, description = "Page size", examples = "10")
  private long size;

  @Schema(required = true, description = "Total number of items across all pages", examples = "50")
  private long total;
}
