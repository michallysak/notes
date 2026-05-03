package pl.michallysak.notes.note.model;

import java.util.List;

public record Paged<T>(List<T> data, long page, long size) {}
