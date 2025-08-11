// JavaScript Frontend Example for Streamlined Booking API

class MovieBookingService {
    constructor(baseUrl, authToken) {
        this.baseUrl = baseUrl || 'http://localhost:8080';
        this.authToken = authToken;
    }

    // Step 1: Select Seats
    async selectSeats(slotId, seatNumbers) {
        try {
            const response = await fetch(`${this.baseUrl}/api/bookings/select-seats`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.authToken}`
                },
                body: JSON.stringify({
                    slotId: slotId,
                    seatNumbers: seatNumbers
                })
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }

            const seatSelection = await response.json();
            console.log('Seat selection successful:', seatSelection);
            return seatSelection;
        } catch (error) {
            console.error('Seat selection failed:', error);
            throw error;
        }
    }

    // Step 2: Complete Booking with Payment
    async completeBooking(slotId, paymentDetails) {
        try {
            const response = await fetch(`${this.baseUrl}/api/bookings/payment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${this.authToken}`
                },
                body: JSON.stringify({
                    slotId: slotId,
                    paymentMethod: paymentDetails.paymentMethod || 'MOCK_PAYMENT',
                    cardNumber: paymentDetails.cardNumber,
                    cardHolderName: paymentDetails.cardHolderName,
                    expiryDate: paymentDetails.expiryDate,
                    cvv: paymentDetails.cvv
                })
            });

            if (!response.ok) {
                const error = await response.text();
                throw new Error(error);
            }

            const booking = await response.json();
            console.log('Booking successful:', booking);
            return booking;
        } catch (error) {
            console.error('Booking failed:', error);
            throw error;
        }
    }

    // Complete flow: Select seats then book
    async bookMovie(slotId, seatNumbers, paymentDetails) {
        try {
            // Step 1: Select seats
            const seatSelection = await this.selectSeats(slotId, seatNumbers);
            
            // Display seat selection details to user
            this.displaySeatSelection(seatSelection);
            
            // Step 2: Complete booking
            const booking = await this.completeBooking(slotId, paymentDetails);
            
            // Display booking confirmation
            this.displayBookingConfirmation(booking);
            
            return booking;
        } catch (error) {
            console.error('Movie booking failed:', error);
            throw error;
        }
    }

    displaySeatSelection(seatSelection) {
        console.log(`
Movie: ${seatSelection.movieTitle}
Cinema: ${seatSelection.cinemaName}
Screen: ${seatSelection.screenName}
Date: ${seatSelection.showDate}
Time: ${seatSelection.showTime}
Seats: ${seatSelection.seatNumbers.join(', ')}
Total Amount: ₹${seatSelection.totalAmount}
        `);
    }

    displayBookingConfirmation(booking) {
        console.log(`
Booking Confirmed!
Booking ID: ${booking.bookingId}
Movie: ${booking.movieTitle}
Cinema: ${booking.cinemaName}
Date: ${booking.showDate}
Time: ${booking.showTime}
Seats: ${booking.seatNumbers.join(', ')}
Total Paid: ₹${booking.totalAmount}
Payment ID: ${booking.paymentId}
Status: ${booking.status}
        `);
    }
}

// Usage Example
async function bookMovieExample() {
    const bookingService = new MovieBookingService(
        'http://localhost:8080',
        'your-jwt-token-here'
    );

    const slotId = 1;
    const seatNumbers = ['A1', 'A2'];
    const paymentDetails = {
        cardNumber: '4111111111111111',
        cardHolderName: 'John Doe',
        expiryDate: '12/25',
        cvv: '123'
    };

    try {
        const booking = await bookingService.bookMovie(slotId, seatNumbers, paymentDetails);
        alert('Movie booked successfully! Booking ID: ' + booking.bookingId);
    } catch (error) {
        alert('Booking failed: ' + error.message);
    }
}

// React Component Example
function MovieBookingForm() {
    const [seatSelection, setSeatSelection] = useState(null);
    const [loading, setLoading] = useState(false);
    const [paymentForm, setPaymentForm] = useState({
        cardNumber: '',
        cardHolderName: '',
        expiryDate: '',
        cvv: ''
    });

    const handleSeatSelection = async (slotId, seats) => {
        setLoading(true);
        try {
            const bookingService = new MovieBookingService(baseUrl, authToken);
            const selection = await bookingService.selectSeats(slotId, seats);
            setSeatSelection(selection);
        } catch (error) {
            alert('Seat selection failed: ' + error.message);
        }
        setLoading(false);
    };

    const handleBookingComplete = async () => {
        if (!seatSelection) return;
        
        setLoading(true);
        try {
            const bookingService = new MovieBookingService(baseUrl, authToken);
            const booking = await bookingService.completeBooking(seatSelection.slotId, paymentForm);
            alert('Booking successful! ID: ' + booking.bookingId);
            setSeatSelection(null);
        } catch (error) {
            alert('Booking failed: ' + error.message);
        }
        setLoading(false);
    };

    return (
        <div>
            {!seatSelection ? (
                <SeatSelectionComponent onSelectSeats={handleSeatSelection} />
            ) : (
                <div>
                    <SeatSelectionSummary selection={seatSelection} />
                    <PaymentForm 
                        paymentForm={paymentForm}
                        onChange={setPaymentForm}
                        onSubmit={handleBookingComplete}
                        loading={loading}
                    />
                </div>
            )}
        </div>
    );
}

// Test Data
const testPaymentDetails = {
    visa: {
        cardNumber: '4111111111111111',
        cardHolderName: 'John Doe',
        expiryDate: '12/25',
        cvv: '123'
    },
    mastercard: {
        cardNumber: '5555555555554444',
        cardHolderName: 'Jane Smith',
        expiryDate: '06/26',
        cvv: '456'
    },
    amex: {
        cardNumber: '378282246310005',
        cardHolderName: 'Bob Johnson',
        expiryDate: '09/27',
        cvv: '7890'
    }
};

console.log('Movie Booking Service loaded with streamlined API support');
