package pl.michallysak.notes.note.model;

public interface NotePagedQuery extends PagedQuery, SortQuery {

  Boolean getIsShared();

  Boolean getIsPinned();

  String getSearchQuery();
}
