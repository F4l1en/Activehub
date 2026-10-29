ActiveHub Sports Centre - Booking, Rental & Promotion System (DIT2123 Coursework 1)
=================================================================================
Data files (simple '|' separated lines) are kept in the "data" folder and are
created / updated automatically:
    catalogue.txt     the 8 rental items (created on first run if missing)
    bookings.txt      facility bookings (ACTIVE / CANCELLED)
    transactions.txt  rental transactions (OPEN / COMPLETED) and their items
    bills.txt         paid bills with the payment method used

Business rules / assumptions used in the code
- Service charge 10% is applied AFTER the promotion discount.
- Promotions A and B apply to FACILITY (court) charges only and need a transaction
  linked to a booking (walk-in transactions have no booking time / participants).
- Promotion C needs at least one facility package line plus 3+ Equipment units.
- Only ONE promotion (greatest saving) is applied; ties go to A, then B, then C.
- Double booking = same facility + same date + same start time (cancelled bookings free the slot).
- Booking dates in the past are rejected; start time must be 08:00 - 21:59.
- Daily report: bookings are counted by booking date; transactions and revenue by payment date.
