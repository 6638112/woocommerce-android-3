package com.woocommerce.android.cardreader.payments

sealed class CardInteracRefundStatus {
    object InitializingInteracRefund : CardInteracRefundStatus()
    object CollectingInteracRefund : CardInteracRefundStatus()
    object WaitingForInput : CardInteracRefundStatus()
    object ProcessingInteracRefund : CardInteracRefundStatus()
    object InteracRefundSuccess : CardInteracRefundStatus()
    data class InteracRefundFailure(
        val type: RefundStatusErrorType,
        val errorMessage: String,
        val refundParams: RefundParams
        ) : CardInteracRefundStatus()

    sealed class RefundStatusErrorType {
        object NoNetwork : RefundStatusErrorType()
        object Cancelled : RefundStatusErrorType()
        object Server : RefundStatusErrorType()
        object Generic : RefundStatusErrorType()

        sealed class DeclinedByBackendError : RefundStatusErrorType() {
            /**
             * The specified amount is less than the minimum amount allowed (50 cents at the moment)
             */
            object AmountTooSmall : DeclinedByBackendError()

            /**
             * Declined by stripe api with unknown reason
             */
            object Unknown : DeclinedByBackendError()

            sealed class CardDeclined : DeclinedByBackendError() {
                /**
                 * A possibly temporary error caused the decline (e.g. the issuing
                 * bank's servers could not be contacted.) Tell the user this and prompt
                 * them to try again with the same (or another) payment method.
                 */
                object Temporary : CardDeclined()

                /**
                 * The card has been reported lost or stolen. Don't reveal this
                 * to the user. Treat it like you would a generic decline.
                 */
                object Fraud : CardDeclined()

                /**
                 * The payment was declined for an unspecified reason. Ask
                 * the user to try another payment method.
                 */
                object Generic : CardDeclined()

                /**
                 * The card, or the account it connects to, is not valid. Ask
                 * the user to try another payment method.
                 */
                object InvalidAccount : CardDeclined()

                /**
                 * The card presented is not supported. Tell the user this and
                 * ask them to try another payment method.
                 */
                object CardNotSupported : CardDeclined()

                /**
                 * The currency is not supported by the card presented. Tell the
                 * user this and ask them to try another payment method.
                 */
                object CurrencyNotSupported : CardDeclined()

                /**
                 * An identical transaction was just completed for the card presented.
                 * Tell the user this and ask them to try another payment method if they
                 * really want to do this.
                 */
                object DuplicateTransaction : CardDeclined()

                /**
                 * The card presented has expired. Tell the user this and ask them
                 * to try another payment method.
                 */
                object ExpiredCard : CardDeclined()

                /**
                 * The card presented has a different ZIP/postal code than was
                 * used to place the order. Tell the user this and ask them
                 * to try another payment method (or correct the order.)
                 */
                object IncorrectPostalCode : CardDeclined()

                /**
                 * The card presented has insufficient funds for the purchase.
                 * Tell the user this and ask them to try another payment method.
                 */
                object InsufficientFunds : CardDeclined()

                /**
                 * The card presented does not allow purchases of the amount
                 * given. Tell the user this and ask them to try another payment method.
                 */
                object InvalidAmount : CardDeclined()

                /**
                 * The card presented requires a PIN and the device doesn't support
                 * PIN entry. Tell the user this and ask them to try another payment method.
                 */
                object PinRequired : CardDeclined()

                /**
                 * The card presented has had an incorrect PIN entered too many times.
                 * Tell the user this and ask them to try another payment method.
                 */
                object TooManyPinTries : CardDeclined()

                /**
                 * The card presented is a system test card and cannot be used to
                 * process a payment. Tell the user this and ask them to try another
                 * payment method.
                 */
                object TestCard : CardDeclined()

                /**
                 * The card presented is a live card, but the store/account is in test
                 * mode. Tell the user this and ask them to use a system test card instead.
                 */
                object TestModeLiveCard : CardDeclined()
            }
        }
    }
}
