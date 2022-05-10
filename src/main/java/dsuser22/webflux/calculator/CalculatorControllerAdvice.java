package dsuser22.webflux.calculator;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;


@ControllerAdvice
public class CalculatorControllerAdvice {
    @ExceptionHandler(Exception.class)
    protected Mono<ResponseEntity<String>> handle(Exception ex) {
        System.out.println("========================================");
        System.out.println(ex.getLocalizedMessage());
        ex.printStackTrace();
        System.out.println("========================================");
        return Mono.just(new ResponseEntity<>("Something is wrong. Look in the console for details", HttpStatus.BAD_REQUEST));
    }
}