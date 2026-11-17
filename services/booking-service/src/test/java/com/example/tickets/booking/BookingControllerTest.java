package com.example.tickets.booking;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    @Test
    void createBooking_validRequest_returns201() {
        BookingRepository repository = Mockito.mock(BookingRepository.class);
        when(repository.save(Mockito.any(Booking.class))).thenReturn(new Booking(1L, 1L, "Jane", 2, Booking.Status.CONFIRMED));

        BookingController controller = new BookingController(repository);
        BookingRequest request = new BookingRequest(1L, "Jane", 2);
        var result = controller.createBooking(request);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        var body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(1);
        assertThat(body.getEventId()).isEqualTo(1L);
        assertThat(body.getCustomerName()).isEqualTo("Jane");
        assertThat(body.getTickets()).isEqualTo(2);
        assertThat(body.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
    }

    @Test
    void getBooking_notFound_returns404() {
        BookingRepository repository = Mockito.mock(BookingRepository.class);
        when(repository.findById(999L)).thenReturn(java.util.Optional.empty());

        BookingController controller = new BookingController(repository);
        var result = controller.getBooking(999);

        assertThat(result.getStatusCode().value()).isEqualTo(404);
    }
}
