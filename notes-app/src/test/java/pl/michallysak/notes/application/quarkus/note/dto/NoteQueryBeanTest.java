package pl.michallysak.notes.application.quarkus.note.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.common.SortList;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.SortDirection;

class NoteQueryBeanTest {

  @Test
  void getSort_shouldReturnEmptyList_whenSortIsNull() {
    // given
    NoteQueryBean queryBean = new NoteQueryBean();
    queryBean.setSort(null);
    // when
    List<FieldSort> result = queryBean.getSort();
    // then
    assertEquals(Collections.emptyList(), result);
  }

  @Test
  void getSort_shouldReturnSortValues_whenSortIsNotNull() {
    // given
    FieldSort fieldSort = new FieldSort("field", SortDirection.ASC);
    SortList sortList = new SortList(List.of(fieldSort));
    NoteQueryBean queryBean = new NoteQueryBean();
    queryBean.setSort(sortList);
    // when
    List<FieldSort> result = queryBean.getSort();
    // then
    assertEquals(List.of(fieldSort), result);
  }
}
