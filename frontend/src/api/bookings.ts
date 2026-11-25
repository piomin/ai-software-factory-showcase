export type BookingStatus = 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED'

export interface Booking {
  id: number
  eventId: number
  customerName: string
  tickets: number
  status: BookingStatus
  amount: number
  expiresAt: string | null
}

export type PaymentStatus = 'SUCCESS' | 'DECLINED'

export interface PaymentResult {
  paymentId: number
  status: PaymentStatus
  amount: number
  cardLast4: string
  reason: string | null
}

export interface NewBooking {
  eventId: number
  customerName: string
  tickets: number
}

export interface CardDetails {
  cardNumber: string
  holderName: string
  expiry: string
  cvv: string
}

/**
 * Error carrying the RFC 9457 Problem Details returned by the services. `message` is the
 * human-readable text to show the user: the server `detail` when the response is a parseable
 * 4xx, or {@link UNAVAILABLE_MESSAGE} for 5xx, unparseable bodies and network failures (N3).
 * `status` is the HTTP status, or 0 when no response was received at all.
 */
export class ApiError extends Error {
  readonly title: string
  readonly detail: string
  readonly status: number

  constructor(message: string, title: string, detail: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.title = title
    this.detail = detail
    this.status = status
  }
}

export const UNAVAILABLE_MESSAGE = 'Payments are temporarily unavailable'

interface ProblemDetails {
  title: string
  detail: string
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(path, init)
  } catch {
    throw unavailable(0)
  }

  if (response.ok) {
    try {
      return (await response.json()) as T
    } catch {
      throw unavailable(response.status)
    }
  }

  const problem = await readProblem(response)
  if (response.status >= 500 || problem === null) {
    throw unavailable(response.status)
  }
  throw new ApiError(problem.detail, problem.title, problem.detail, response.status)
}

async function readProblem(response: Response): Promise<ProblemDetails | null> {
  try {
    const body: unknown = await response.json()
    if (typeof body !== 'object' || body === null) {
      return null
    }
    const problem = body as Record<string, unknown>
    const detail = typeof problem.detail === 'string' ? problem.detail : null
    if (detail === null) {
      return null
    }
    const title = typeof problem.title === 'string' ? problem.title : 'Error'
    return { title, detail }
  } catch {
    return null
  }
}

function unavailable(status: number): ApiError {
  return new ApiError(UNAVAILABLE_MESSAGE, 'Service Unavailable', UNAVAILABLE_MESSAGE, status)
}

function postJson(body: NewBooking | CardDetails): RequestInit {
  return {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }
}

export function createBooking(booking: NewBooking): Promise<Booking> {
  return request<Booking>('/api/bookings', postJson(booking))
}

export function fetchBooking(id: number): Promise<Booking> {
  return request<Booking>(`/api/bookings/${id}`)
}

export function payBooking(id: number, card: CardDetails): Promise<PaymentResult> {
  return request<PaymentResult>(`/api/bookings/${id}/payments`, postJson(card))
}

export function cancelBooking(id: number): Promise<Booking> {
  return request<Booking>(`/api/bookings/${id}/cancel`, { method: 'POST' })
}
