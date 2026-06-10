package pl.michallysak.notes.application.quarkus.note.attachment.controller;

public record AttachmentContent(byte[] value, String contentType, String fileName) {}
