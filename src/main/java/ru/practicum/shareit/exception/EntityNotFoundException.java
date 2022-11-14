package ru.practicum.shareit.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class<?> classEntity, Long id) {
        super(classEntity.getSimpleName() + " with id=" + id + " not found");
    }
}
