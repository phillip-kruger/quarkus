### Bean Validation Annotations

- Use `@NotNull`, `@NotBlank`, `@NotEmpty`, `@Size`, `@Min`, `@Max`, `@Email`, `@Pattern` on fields/parameters.
- Use `@Valid` on method parameters to trigger nested validation on objects.
- Place constraints on REST endpoint parameters for automatic 400 responses.

### REST Integration

- Constraint violations on REST endpoint **parameters** automatically return HTTP 400 with validation details.
- Annotate request body DTOs with constraints, and use `@Valid` on the endpoint parameter.
- Return value validation and service method validation result in HTTP 500, not 400 — handle `ConstraintViolationException` explicitly if you need custom error responses for those.

### Custom Validators

- Create a constraint annotation with `@Constraint(validatedBy = MyValidator.class)`.
- Implement `ConstraintValidator<MyAnnotation, TargetType>` as a CDI bean.
- CDI injection works inside custom validators.

### Method Validation

- Annotate CDI bean method parameters and return values with constraints.
- Method validation is automatic for CDI beans — no extra config needed.

### Testing

- Test validation by sending invalid input via REST Assured and asserting 400 status.
- For unit testing validators, use `Validator` injection and `validator.validate(object)`.

### Common Pitfalls

- Do NOT forget `@Valid` on nested object parameters — constraints on nested fields are not checked without it.
- Validation only works on CDI-managed beans — plain `new` objects are not validated.
