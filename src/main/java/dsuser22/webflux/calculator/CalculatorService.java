package dsuser22.webflux.calculator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CalculatorService {

    private ScriptEngine engine;
    private Invocable invocable;

    @Value("${delayValue}")
    private long delay;

    private void addScript(String textFunc1, String textFunc2){
        engine = new ScriptEngineManager().getEngineByName("JavaScript");
        try {
            engine.eval(textFunc1);
            engine.eval(textFunc2);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        invocable = (Invocable) engine;

    }

    public Flux<Integer> generate(FormData data){
        if(data.getAligned().equalsIgnoreCase("YES")){
            return Flux.empty();
        } else if(data.getAligned().equalsIgnoreCase("NO")){
            return generateNotAlignedFlux(data);
        } else {
            throw new IllegalStateException("only yes/no");
        }
    }

    public Flux<Integer> generateNotAlignedFlux(FormData data){

        int times = data.getTimes();
        String textFunc1 = data.getTextFunc1();
        String textFunc2 = data.getTextFunc2();
        String funcName1 = getFuncName(data.getTextFunc1());
        String funcName2 = getFuncName(data.getTextFunc2());

        addScript(textFunc1, textFunc2);

        AtomicInteger atomic = new AtomicInteger(1);
        List<Integer> integerList = new CopyOnWriteArrayList();

        Flux
                .range(0,times)
                .delaySubscription(Duration.ofSeconds(delay))
                .subscribeOn(Schedulers.newParallel("A"))
                .map(i ->{
                    long start = System.currentTimeMillis();
                    Integer[] arr = new Integer[4];
                    arr[0] = atomic.getAndIncrement();
                    arr[1] = 1;
                    try {
                        arr[2] = ((Double) invocable.invokeFunction(funcName1, i)).intValue();
                    } catch (ScriptException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    arr[3] = ((int)(System.currentTimeMillis() - start));
                    integerList.addAll(Arrays.asList(arr));
                    return i;
                }).subscribe();
        Flux
                .range(0,times)
                .delaySubscription(Duration.ofSeconds(delay))
                .subscribeOn(Schedulers.newParallel("B"))
                .map(i ->{
                    long start = System.currentTimeMillis();
                    Integer[] arr = new Integer[4];
                    arr[0] = atomic.getAndIncrement();
                    arr[1] = 2;
                    try {
                        arr[2] = ((Double) invocable.invokeFunction(funcName2, i)).intValue();
                    } catch (ScriptException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    arr[3] = ((int)(System.currentTimeMillis() - start));
                    integerList.addAll(Arrays.asList(arr));
                    return i;
                }).subscribe();
        return Flux
                .fromIterable(integerList)
                .delaySubscription(Duration.ofSeconds(delay+1))
                .retry(5)
                .subscribeOn(Schedulers.newParallel("C"));

    }
    public String getFuncName(String textFunction){
        Matcher m= Pattern.compile("(\\w+)\\(\\w+\\)").matcher(textFunction);
        m.find();
        return m.group(1);
    }

}
