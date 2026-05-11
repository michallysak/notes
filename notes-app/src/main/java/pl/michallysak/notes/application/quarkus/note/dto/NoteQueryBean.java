package pl.michallysak.notes.application.quarkus.note.dto;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.michallysak.notes.application.quarkus.common.SortList;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.NotePagedQuery;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteQueryBean implements NotePagedQuery {

  @QueryParam("isShared")
  private Boolean isShared;

  @QueryParam("isPinned")
  private Boolean isPinned;

  @QueryParam("page")
  @DefaultValue("0")
  private int page;

  @QueryParam("size")
  @DefaultValue("10")
  private int size;

  @QueryParam("sort")
  private SortList sort;

  public List<FieldSort> getSort() {
    if (sort == null) {
      return Collections.emptyList();
    }
    return sort.values();
  }
}
