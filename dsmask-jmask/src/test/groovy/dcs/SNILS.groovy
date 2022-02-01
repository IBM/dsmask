class Globals {
    static final int[] SNILS_POS = [ 9,8,7,6,5,4,3,2,1 ];
}

// Convert a string of numbers to numerical digit values
int[] makeDigits(String str) {
    int[] digits = new int[str.length()];
    for (int i=0; i<str.length(); ++i)
        digits[i] = Character.getNumericValue(str.charAt(i));
    return digits;
}

// control character for Russian SNILS
String fixSNILS(String snils) {
    int[] digits = makeDigits(snils);
    int control = 0;
    for (int i=0; i<Globals.SNILS_POS.length; ++i) {
        control += digits[i] * Globals.SNILS_POS[i];
    }
    if (control > 101) {
        control = control % 101;
    }
    if (control == 100 || control == 101)
        control = 0;
    String suf = String.valueOf(control);
    if (suf.length()==1)
        suf = "0" + suf;
    return snils.substring(0,9) + suf;
}

// Compute the control characters for Russian SNILS
Object invoke(Object input) {
    if (input==null)
        return null;
    String snils = input.toString();
    if (snils.length()==0)
        return input;
    return fixSNILS(snils);
}

println invoke("11223344500")
println invoke("21223344500")
println invoke("31223344500")
println invoke("41223344500")
println invoke("51223344500")
println invoke("61223344500")
println invoke("71223344500")
println invoke("81223344500")
println invoke("91223344500")
println invoke("10223344500")
