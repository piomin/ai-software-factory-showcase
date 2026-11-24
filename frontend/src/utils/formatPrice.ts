const plnFormatter = new Intl.NumberFormat('pl-PL', {
  style: 'currency',
  currency: 'PLN',
})

export function formatPln(price: number): string {
  return plnFormatter.format(price)
}
