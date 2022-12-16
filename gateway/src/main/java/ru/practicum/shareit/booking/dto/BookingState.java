package ru.practicum.shareit.booking.dto;

public enum BookingState {
    // Все
    ALL,
    // Текущие
    CURRENT,
    // Будущие
    FUTURE,
    // Завершенные
    PAST,
    // Отклоненные
    REJECTED,
    // Ожидающие подтверждения
    WAITING;

    public static BookingState from(String stringState) {
        try {
            return BookingState.valueOf(stringState.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown state: " + stringState);
        }
    }
}
