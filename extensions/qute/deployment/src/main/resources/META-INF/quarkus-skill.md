## Key Patterns

### Type-safe templates with @CheckedTemplate

Declare templates as `static native` methods inside a `@CheckedTemplate`-annotated nested class. Method parameters become template parameters, validated at build time:

```java
@Path("products")
public class ProductResource {

    @CheckedTemplate
    static class Templates {
        static native TemplateInstance list(List<Product> products);
        static native TemplateInstance detail(Product product);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list() {
        return Templates.list(service.findAll());
    }
}
```

Alternative: use a Java record that implements `TemplateInstance`:

```java
record Hello(String name) implements TemplateInstance {}
// template: templates/Hello.html
```

### Template file path resolution

Templates go in `src/main/resources/templates/`. The path is derived from the class structure:

| Declaration | Method | Template path |
|---|---|---|
| Nested `Templates` in `ProductResource` | `list()` | `templates/ProductResource/list.html` |
| Top-level class | `hello()` | `templates/hello.html` |
| `@CheckedTemplate(basePath = "items")` | `detail()` | `templates/items/detail.html` |

Use `@CheckedTemplate(defaultName = CheckedTemplate.HYPHENATED_ELEMENT_NAME)` to convert camelCase method names to hyphenated file names (e.g., `productList()` → `product-list.html`).

### Template syntax quick reference

```html
{name}                          <!-- expression -->
{product.name}                  <!-- property access -->
{#for item in items}...{/for}   <!-- loop -->
{#for item in items}{item_count}{/for}  <!-- 1-based index -->
{#if condition}...{#else}...{/if}       <!-- conditional -->
{#let x = expr}...{/let}               <!-- local variable -->
{item.formattedPrice}           <!-- template extension method -->
{item ?: 'default'}             <!-- default value for null -->
```

### Template extension methods

Add computed properties to any type using `@TemplateExtension`. The first parameter is the target type:

```java
@TemplateExtension
static class ProductExtensions {
    static String formattedPrice(Product product) {
        return String.format("$%.2f", product.price());
    }
}
```

In templates: `{product.formattedPrice}` — no parentheses, looks like a property.

## Common Pitfalls

- **Template path must match the class/method structure exactly.** For a nested `Templates` class inside `ProductResource`, templates go in `templates/ProductResource/`, not `templates/`. Build fails if the file is missing.
- **Return `TemplateInstance`, not `String`.** Quarkus REST automatically renders `TemplateInstance` return values — do not call `.render()` in the endpoint.
- **Template expressions are type-checked by default.** Undefined properties cause a build error. Use `@TemplateData` on types that don't have public getters, or disable with `@CheckedTemplate(requireTypeSafeExpressions = false)`.
- **`{#for}` uses `in`, not `:`.** The syntax is `{#for item in items}`, not `{#for item : items}`.

## Testing

Test template rendering via HTTP endpoints with REST-assured:

```java
@QuarkusTest
class ProductResourceTest {

    @Test
    void testListPage() {
        given()
            .when().get("/products")
            .then()
            .statusCode(200)
            .body(containsString("Product Name"))
            .body(containsString("On Sale!"));
    }
}
```

For unit-testing templates directly, inject `Engine` and render manually:

```java
@Inject
Engine engine;

@Test
void testTemplate() {
    String result = engine.getTemplate("ProductResource/list")
        .data("products", List.of(product))
        .render();
    assertTrue(result.contains("expected"));
}
```
