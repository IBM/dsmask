class Globals {
    static final int[] INN10 = [ 2,4,10,3,5,9,4,6,8 ];
    static final int[] INN12x1 = [  7, 2, 4,10, 3, 5, 9, 4, 6, 8, 0 ];
    static final int[] INN12x2 = [  3, 7, 2, 4,10, 3, 5, 9, 4, 6, 8 ];
}

// Convert a string of numbers to numerical digit values
int[] digitsINN(String inn) {
    int[] digits = new int[inn.length()];
    for (int i=0; i<inn.length(); ++i)
        digits[i] = Character.getNumericValue(inn.charAt(i));
    return digits;
}

// control character for 10-digit Russian INN
String fixINN10(String inn) {
    int[] digits = digitsINN(inn);
    int control = 0;
    for (int i=0; i<Globals.INN10.length; ++i) {
        control += digits[i] * Globals.INN10[i];
    }
    control = control % 11;
    if (control > 9)
        control = 0;
    return inn.substring(0,9) + String.valueOf(control);
}

// control character for 12-digit Russian INN
String fixINN12(String inn) {
    int[] digits = digitsINN(inn);
    int control1 = 0, control2 = 0;
    for (int i=0; i<Globals.INN12x1.length; ++i) {
        control1 += digits[i] * Globals.INN12x1[i];
    }
    control1 = control1 % 11;
    if (control1 > 9)
        control1 = 0;
    digits[10] = control1;
    for (int i=0; i<Globals.INN12x2.length; ++i) {
        control2 += digits[i] * Globals.INN12x2[i];
    }
    control2 = control2 % 11;
    if (control2 > 9)
        control2 = 0;
    return inn.substring(0,10) + String.valueOf(control1) + String.valueOf(control2);
}

// Compute the control characters for Russian INN
Object invoke(Object input) {
    if (input==null)
        return null;
    switch (input.length()) {
        case 10: input = fixINN10(input); break;
        case 12: input = fixINN12(input); break;
        default: input = "BAD-" + input;
    }
    return input;
}

println invoke("6949009560")
println invoke("190504234900")
println invoke("210504234900")
println invoke("370504224900")
println invoke("410504234900")
println invoke("540504234900")
println invoke("690504234900")
println invoke("730504234900")
println invoke("870504234900")
println invoke("910504234900")
println invoke("100504234900")
