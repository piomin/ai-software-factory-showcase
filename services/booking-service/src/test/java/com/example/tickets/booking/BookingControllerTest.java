package com.example.tickets.booking;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    private final BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
    private final EventServiceClient eventServiceClient = Mockito.mock(EventServiceClient.class);
    private final BookingController controller = new BookingController(bookingRepository, eventServiceClient);

    @Test
    void createBooking_validRequest_returns201() {
        EventReservationResponse response = new EventReservationResponse(1, 5);
        when(eventServiceClient.reserveTickets(anyLong(), anyInt())).thenReturn(response);
        when(bookingRepository.save(any())).thenReturn(new Booking(1L, 1L, "Jane", 2, null));

        BookingRequest request = new BookingRequest(1L, "Jane", 2);
        var result = controller.createBooking(request);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody().getId()).isEqualTo(1L);
        assertThat(result.getBody().getEventId()).isEqualTo(1L);
        assertThat(result.getBody().getCustomerName()).isEqualTo("Jane");
        assertThat(result.getBody().getTickets()).isEqualTo(2);
    }

    @Test
    void getBooking_notFound_returns404() {
        when(bookingRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        var result = controller.getBooking(999);

        assertThat(result.getStatusCode().value()).isEqualTo(404);
    }
}