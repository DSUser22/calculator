package dsuser22.webflux.calculator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FormData {
    private String textFunc1;
    private String textFunc2;
    private int times;
    private String aligned;
}
