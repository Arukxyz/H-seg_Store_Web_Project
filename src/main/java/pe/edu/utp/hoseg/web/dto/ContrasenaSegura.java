package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Regla de contraseña de cliente: 8 a 72 caracteres (BCrypt ignora lo que
 * pase de 72 bytes), con al menos una mayúscula y un número. La usan el
 * registro, el cambio de contraseña y el restablecimiento.
 */
@NotBlank
@Size(min = 8, max = 72)
@Pattern(regexp = "^(?=.*[A-ZÁÉÍÓÚÑ])(?=.*\\d).*$")
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContrasenaSegura {

    String message() default "Usa de 8 a 72 caracteres, con al menos una mayúscula y un número.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
