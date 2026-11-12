package cl.diego.NanoStock.common.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String email) {

    private static Pattern emailRegex = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public Email {
        Objects.requireNonNull(email, "email cannot be null");
        if (email.length() <=0) {
            throw new IllegalArgumentException("email cannot be empty");
        }
        if (!emailRegex.matcher(email).matches()){
            throw new IllegalArgumentException("email is not a valid email");
        }
    }
}
