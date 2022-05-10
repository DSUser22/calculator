package dsuser22.webflux.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class CalculatorController {
    private CalculatorService service;

    @Autowired
    public CalculatorController(CalculatorService service) {
        this.service = service;
    }

    @GetMapping(path = "/calculate")
    @ResponseBody
    public Flux<Integer> resultFlux(@RequestBody FormData data) {
        return service.generate(data);
    }
}
