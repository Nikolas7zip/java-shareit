package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingOutput;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class BookingMapper {

    public static Booking mapToNewBooking(BookingDto bookingDto, User booker, Item item) {
        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(booker);
        booking.setItem(item);

        return booking;
    }

    public static BookingOutput mapToBookingOutput(Booking booking) {
        BookingOutput bookingOutput = new BookingOutput();
        bookingOutput.setId(booking.getId());
        bookingOutput.setStart(booking.getStart());
        bookingOutput.setEnd(booking.getEnd());
        bookingOutput.setStatus(booking.getStatus());
        bookingOutput.setItem(ItemMapper.mapToItemDto(booking.getItem()));
        bookingOutput.setBooker(UserMapper.mapToUserDto(booking.getBooker()));

        return bookingOutput;
    }

    public static List<BookingOutput> mapToBookingOutput(List<Booking> bookings) {
        List<BookingOutput> outputBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            outputBookings.add(mapToBookingOutput(booking));
        }

        return outputBookings;
    }
}
