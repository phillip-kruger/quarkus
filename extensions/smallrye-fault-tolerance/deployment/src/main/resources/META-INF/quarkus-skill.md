## Key Patterns

### Annotation-based fault tolerance

Apply MicroProfile Fault Tolerance annotations to **CDI bean** methods. All annotations are in `org.eclipse.microprofile.faulttolerance.*`:

```java
@ApplicationScoped
public class ExternalServiceClient {

    @Retry(maxRetries = 3, delay = 500)
    @Fallback(fallbackMethod = "fallbackGetData")
    public String getData(String id) {
        return callExternalService(id);
    }

    private String fallbackGetData(String id) {
        return "cached-" + id;
    }
}
```

### Combining annotations

Multiple annotations can be applied to the same method. They form a call chain (outermost to innermost):

**Fallback → Retry → CircuitBreaker → Timeout → method call**

Retry re-attempts the entire CircuitBreaker+Timeout combo. Fallback catches any exception that survives all retries. The order annotations appear in source doesn't matter.

### Selective retry with retryOn and abortOn

Control which exceptions trigger retries:

```java
@Retry(maxRetries = 3,
       retryOn = {ConnectException.class, TimeoutException.class},
       abortOn = {InvalidInputException.class})
@Fallback(fallbackMethod = "fallback")
public String callService(String input) { ... }
```

- `retryOn` — only these exception types (and subclasses) trigger retries
- `abortOn` — these exceptions abort retries immediately
- If both omitted, ALL exceptions trigger retries
- `abortOn` only prevents **retries**, not fallback — the Fallback layer still catches the exception

### Bulkhead — limit concurrent execution

Prevent resource exhaustion by limiting concurrent calls:

```java
@Bulkhead(5)
@Fallback(fallbackMethod = "bulkheadFallback")
public String limitedCall() { ... }
```

Excess calls throw `BulkheadException`. Combine with `@Fallback` to handle rejection gracefully.

### Fallback methods

A fallback method must have the **same parameters and return type** as the guarded method. It can be private:

```java
@Fallback(fallbackMethod = "myFallback")
public PaymentResult process(String orderId, BigDecimal amount) { ... }

private PaymentResult myFallback(String orderId, BigDecimal amount) {
    return PaymentResult.pending(orderId);
}
```

### Async fault tolerance

Quarkus enables non-compatible mode by default. Methods returning `CompletionStage` or `Uni` automatically get async fault tolerance — **no `@Asynchronous` annotation needed**:

```java
@Retry(maxRetries = 3)
@Fallback(fallbackMethod = "asyncFallback")
public Uni<PaymentResult> asyncProcess(String orderId) {
    return Uni.createFrom().item(() -> callService(orderId));
}
```

## Common Pitfalls

- **All time values default to milliseconds.** `@Timeout(2000)` = 2 seconds. Use `unit = ChronoUnit.SECONDS` for clarity.
- **CircuitBreaker defaults are generous.** `requestVolumeThreshold` = 20, `failureRatio` = 0.5, `delay` = 5000ms. Use smaller values in tests.
- **Annotations only work on CDI beans.** Creating the object with `new` bypasses fault tolerance entirely.
- **Self-invocation bypasses fault tolerance.** Calling an annotated method from within the same bean skips the interceptor.
- **`abortOn` doesn't prevent fallback.** It only skips retries — the Fallback layer (outermost) still catches the exception.

## Testing

```java
@QuarkusTest
class FaultToleranceTest {

    @Inject
    MyService service;

    @Test
    void testSelectiveRetry() {
        // Throws InvalidInputException → abortOn skips retries, fallback returns default
        PaymentResult result = service.process("bad-order", BigDecimal.ZERO);
        assertTrue(result.isPending());
    }
}
```

For circuit breaker tests, use small `requestVolumeThreshold` and `delay` values. Circuit breaker state persists across test methods — use separate test classes or account for shared state.

## Configuration override

Override annotation values via `application.properties`:

```properties
quarkus.fault-tolerance."com.example.MyService/myMethod".retry.max-retries=5
quarkus.fault-tolerance."com.example.MyService/myMethod".circuit-breaker.request-volume-threshold=5
quarkus.fault-tolerance."com.example.MyService/myMethod".bulkhead.value=3
```
