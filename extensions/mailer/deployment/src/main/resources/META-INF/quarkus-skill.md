## Key Patterns

### Sending emails

Inject `Mailer` (imperative/blocking) or `ReactiveMailer` (returns `Uni<Void>`). Use `Mail` static factories to build messages:

```java
@Inject
Mailer mailer;

@Inject
io.quarkus.mailer.reactive.ReactiveMailer reactiveMailer;

// Plain text
mailer.send(Mail.withText("to@example.com", "Subject", "Body text"));

// HTML
mailer.send(Mail.withHtml("to@example.com", "Subject", "<h1>Hello</h1>"));

// With attachment (byte[])
mailer.send(Mail.withText("to@example.com", "Subject", "See attached")
    .addAttachment("report.txt", data, "text/plain"));

// Reactive — returns Uni<Void>
Uni<Void> result = reactiveMailer.send(Mail.withText("to@example.com", "Subject", "Body"));
```

### Inline attachments in HTML emails

Use content-id references in HTML and `addInlineAttachment`. The content-id must be in `<id@domain>` format:

```java
String html = "<p>Logo: <img src=\"cid:logo@quarkus.io\"/></p>";
mailer.send(Mail.withHtml("to@example.com", "Subject", html)
    .addInlineAttachment("logo.png", logoBytes, "image/png", "<logo@quarkus.io>"));
```

### Using with Quarkus REST

The imperative `Mailer` blocks the calling thread. When using it from a Quarkus REST endpoint, add `@Blocking`:

```java
@POST
@Blocking
public void sendEmail(EmailRequest request) {
    mailer.send(Mail.withText(request.email(), "Subject", "Body"));
}
```

Alternatively, use `ReactiveMailer` to avoid blocking — no `@Blocking` needed since it returns `Uni<Void>`.

## Common Pitfalls

- **`ReactiveMailer` import changed.** Use `io.quarkus.mailer.reactive.ReactiveMailer`, not `io.quarkus.mailer.ReactiveMailer` (deprecated).
- **`Mail` methods are fluent.** `addAttachment()`, `addTo()`, `setCc()`, etc. return `this` for chaining.
- **Set `quarkus.mailer.from`** to avoid `null` sender. Without it, the `from` field is empty in dev/test mock mode and will fail in production.
- **Attachment overloads.** `addAttachment(name, byte[], contentType)` is the simplest. Also accepts `File` or reactive `Publisher<Byte>`.

## Testing

Mock mode is enabled by default in dev and test profiles — emails are captured in memory instead of sent via SMTP. Inject `MockMailbox` to inspect them:

```java
@QuarkusTest
class MailTest {

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void clear() {
        mailbox.clear();
    }

    @Test
    void testEmailSent() {
        // trigger your email-sending code, then:
        List<Mail> sent = mailbox.getMailsSentTo("to@example.com");
        assertEquals(1, sent.size());
        assertEquals("Expected Subject", sent.get(0).getSubject());
        assertTrue(sent.get(0).getText().contains("expected content"));

        // for HTML emails:
        assertNotNull(sent.get(0).getHtml());

        // for attachments:
        assertEquals(1, sent.get(0).getAttachments().size());
    }
}
```

**Use `getMailsSentTo()`** (not the deprecated `getMessagesSentTo()`). For richer message objects including headers, use `getMailMessagesSentTo()` which returns `List<MailMessage>`.
