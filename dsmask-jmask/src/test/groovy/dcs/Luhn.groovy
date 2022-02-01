/**
 * Calculate the correct Luhn digit for the specified PAN
 */
String calcLuhnDigit(String card) {
    String digit;
    /* convert to array of int for simplicity */
    int[] digits = new int[card.length()];
    for (int i = 0; i < card.length(); i++) {
        digits[i] = Character.getNumericValue(card.charAt(i));
    }

    /* double every other starting from right - jumping from 2 in 2 */
    for (int i = digits.length - 1; i >= 0; i -= 2)	{
        digits[i] += digits[i];
        /* taking the sum of digits grater than 10 - simple trick by substract 9 */
        if (digits[i] >= 10) {
            digits[i] = digits[i] - 9;
        }
    }
    int sum = 0;
    for (int i = 0; i < digits.length; i++) {
        sum += digits[i];
    }
    /* multiply by 9 step */
    sum = sum * 9;

    /* convert to string to be easier to take the last digit */
    digit = sum + "";
    return digit.substring(digit.length() - 1);
}

/**
 * Normalize the PAN number and calculate the correct Luhn code
 */
Object invoke(Object input) {
    if (input==null)
        return null;
    String pan = input.toString().trim();
    pan = pan.replaceAll("[^\\d]", "");
    switch (pan.length()) {
        case 12: break;
        case 13: pan = pan.substring(0, 12); break;
        case 15: break;
        case 16: pan = pan.substring(0, 15); break;
        case 18: break;
        case 19: pan = pan.substring(0, 18); break;
        default:
            if (pan.length() > 19)
                pan = pan.substring(0, 18);
            else // invalid card number length
                return input;
    }
    return pan + String.valueOf(calcLuhnDigit(pan));
}

println invoke("4276 3801 0435 7228")
