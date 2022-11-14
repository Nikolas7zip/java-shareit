package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BookingPredicates {
    public static Predicate<Booking> isWaiting() {
        return booking -> booking.getStatus() == BookingStatus.WAITING;
    }

    public static Predicate<Booking> isRejected() {
        return booking -> booking.getStatus() == BookingStatus.REJECTED;
    }

    public static Predicate<Booking> isPast() {
        return booking -> booking.getEnd().isBefore(LocalDateTime.now());
    }

    public static Predicate<Booking> isFuture() {
        return booking -> booking.getStart().isAfter(LocalDateTime.now());
    }

    public static Predicate<Booking> isCurrent() {
        return booking -> {
            LocalDateTime nowMoment = LocalDateTime.now();
            return (booking.getStart().isBefore(nowMoment) || booking.getStart().isEqual(nowMoment)) &&
                    (booking.getEnd().isAfter(nowMoment) || booking.getEnd().isEqual(nowMoment));
        };
    }

    public static List<Booking> filterBookings(List<Booking> bookings, Predicate<Booking> predicate)
    {
        return bookings.stream()
                .filter( predicate )
                .collect(Collectors.toList());
    }
}
