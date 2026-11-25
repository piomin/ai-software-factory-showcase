import { describe, it, expect, vi, beforeEach } from 'vitest'

import {
  ApiError,
  cancelBooking,
  createBooking,
  fetchBooking,
  payBooking,
  type Booking,
  type PaymentResult,
} from './bookings'

const pendingBooking: Booking = {
  id: 42,
  eventId: 7,
  customerName: 'Ada Lovelace',
  tickets: 2,
  status: 'PENDING_PAYMENT',
  amount: 259.98,
  expiresAt: '2025-06-15T10:15:00',
}

function mockResponse(status: number, body: unknown, jsonFails = false): void {
  const json = async () => {
    if (jsonFails) {
      throw new SyntaxError('Unexpected token < in JSON at position 0')
    }
    return body
  }
  vi.spyOn(globalThis, 'fetch').mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    json,
  } as Response)
}

async function expectRejection(promise: Promise<unknown>): Promise<ApiError> {
  try {
    await promise
  } catch (reason: unknown) {
    return reason as ApiError
  }
  throw new Error('Expected the call to reject')
}

function lastRequest(): { path: string; init: RequestInit } {
  const calls = vi.mocked(globalThis.fetch).mock.calls
  const [path, init] = calls[calls.length - 1]
  return { path: String(path), init: (init ?? {}) as RequestInit }
}

describe('bookings api client', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  describe('createBooking', () => {
    it('posts the booking and returns the created booking', async () => {
      mockResponse(201, pendingBooking)

      const result = await createBooking({ eventId: 7, customerName: 'Ada Lovelace', tickets: 2 })

      expect(result).toEqual(pendingBooking)
      const { path, init } = lastRequest()
      expect(path).toBe('/api/bookings')
      expect(init.method).toBe('POST')
      expect(init.headers).toEqual({ 'Content-Type': 'application/json' })
      expect(init.body).toBe(JSON.stringify({ eventId: 7, customerName: 'Ada Lovelace', tickets: 2 }))
    })
  })

  describe('fetchBooking', () => {
    it('gets the booking by id', async () => {
      const confirmed: Booking = { ...pendingBooking, status: 'CONFIRMED', expiresAt: null }
      mockResponse(200, confirmed)

      const result = await fetchBooking(42)

      expect(result).toEqual(confirmed)
      const { path, init } = lastRequest()
      expect(path).toBe('/api/bookings/42')
      expect(init.method).toBeUndefined()
    })
  })

  describe('payBooking', () => {
    it('posts card details and returns the payment result', async () => {
      const payment: PaymentResult = {
        paymentId: 900,
        status: 'SUCCESS',
        amount: 259.98,
        cardLast4: '1111',
        reason: null,
      }
      mockResponse(200, payment)
      const card = { cardNumber: '4242424242421111', holderName: 'Ada Lovelace', expiry: '12/30', cvv: '123' }

      const result = await payBooking(42, card)

      expect(result).toEqual(payment)
      const { path, init } = lastRequest()
      expect(path).toBe('/api/bookings/42/payments')
      expect(init.method).toBe('POST')
      expect(init.body).toBe(JSON.stringify(card))
    })
  })

  describe('cancelBooking', () => {
    it('posts the cancellation and returns the cancelled booking', async () => {
      const cancelled: Booking = { ...pendingBooking, status: 'CANCELLED' }
      mockResponse(200, cancelled)

      const result = await cancelBooking(42)

      expect(result).toEqual(cancelled)
      const { path, init } = lastRequest()
      expect(path).toBe('/api/bookings/42/cancel')
      expect(init.method).toBe('POST')
    })
  })

  it('surfaces the server detail of a 409 problem response', async () => {
    mockResponse(409, {
      title: 'Conflict',
      detail: 'Booking 42 has already been paid',
      status: 409,
    })

    const error = await expectRejection(
      payBooking(42, { cardNumber: '4242424242421111', holderName: 'Ada Lovelace', expiry: '12/30', cvv: '123' }),
    )

    expect(error).toBeInstanceOf(ApiError)
    expect(error.message).toBe('Booking 42 has already been paid')
    expect(error.detail).toBe('Booking 42 has already been paid')
    expect(error.title).toBe('Conflict')
    expect(error.status).toBe(409)
  })

  it('maps a 503 problem response to the unavailable message', async () => {
    mockResponse(503, {
      title: 'Service Unavailable',
      detail: 'event-service is down',
      status: 503,
    })

    const error = await expectRejection(createBooking({ eventId: 7, customerName: 'Ada Lovelace', tickets: 2 }))

    expect(error).toBeInstanceOf(ApiError)
    expect(error.message).toBe('Payments are temporarily unavailable')
    expect(error.status).toBe(503)
    expect(error.message).not.toContain('event-service')
  })

  it('maps a network failure to the unavailable message', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'))

    const error = await expectRejection(fetchBooking(42))

    expect(error).toBeInstanceOf(ApiError)
    expect(error.message).toBe('Payments are temporarily unavailable')
    expect(error.status).toBe(0)
  })

  it('maps an unparseable error body to the unavailable message', async () => {
    mockResponse(500, '<html>Boom</html>', true)

    const error = await expectRejection(cancelBooking(42))

    expect(error).toBeInstanceOf(ApiError)
    expect(error.message).toBe('Payments are temporarily unavailable')
    expect(error.status).toBe(500)
  })
})
