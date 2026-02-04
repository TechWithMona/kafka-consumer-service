# kafka-consumer-service
kafka-consumer-service

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">

</head>

<body>

<h1>Kafka Consumer Service – Order Lifecycle Processing</h1>

<p>
This project is a <strong>production-style Kafka consumer service</strong> built using
<strong>Spring Boot and Apache Kafka</strong>.  
It processes <strong>Order Lifecycle Events</strong> and demonstrates real-world Kafka
consumer patterns used in enterprise systems.
</p>

<div class="box">
<strong>Order Lifecycle:</strong><br/>
CREATED → CONFIRMED → SHIPPED
</div>

<hr/>

<h2>Architecture Overview</h2>

<pre>
Postman / REST Client
        |
        v
Kafka Producer Service
        |
        v
Kafka Topic: order-events
        |
        v
Kafka Consumer Service
   - Validation
   - Manual Acknowledgment
   - Retry Topics
   - Dead Letter Topic (DLT)
   - Offset Replay (via REST)
   - Failed Event Persistence (H2 DB)
</pre>

<hr/>

<h2>Order Event Model</h2>

<pre>
{
  "orderId": "O-10008",
  "type": "CREATED | CONFIRMED | SHIPPED",
  "quantity": 2,
  "timestamp": "2026-02-03T18:00:00Z"
}
</pre>

<hr/>

<h2>Technology Stack</h2>
<ul>
  <li>Java 17</li>
  <li>Spring Boot 4.x</li>
  <li>Spring Kafka</li>
  <li>Apache Kafka (Docker)</li>
  <li>H2 In-Memory Database</li>
  <li>Maven</li>
</ul>

<hr/>

<h2>Kafka Topics</h2>

<ul>
  <li><code>order-events</code> – Main order lifecycle events</li>
  <li><code>order-events-retry-2000</code> – Retry after 2 seconds</li>
  <li><code>order-events-retry-4000</code> – Retry after 4 seconds</li>
  <li><code>order-events-dlt</code> – Dead Letter Topic</li>
</ul>

<hr/>

<h2>Consumer Features</h2>

<h3>1. Manual Acknowledgment</h3>

<pre>
spring.kafka.listener.ack-mode=manual
spring.kafka.consumer.enable-auto-commit=false
</pre>

<p>
Offsets are committed <strong>only after successful processing</strong>.
This prevents duplicate consumption and is the preferred production approach.
</p>

<hr/>

<h3>2. Validation & Failure Handling</h3>

<p>
Events are validated before acknowledgment.  
Invalid events trigger retry and eventually DLT.
</p>

<pre>
if (event.getQuantity() <= 1) {
    throw new ValidationException("Quantity must be greater than 1");
}
</pre>

<hr/>

<h3>3. Retry with Exponential Backoff</h3>

<pre>
@RetryableTopic(
  attempts = "3",
  backOff = @BackOff(delay = 2000, multiplier = 2.0),
  dltTopicSuffix = "-dlt"
)
</pre>

<p>
Retry flow:
</p>

<pre>
order-events
 → order-events-retry-2000
 → order-events-retry-4000
 → order-events-dlt
</pre>

<hr/>

<h3>4. Dead Letter Topic (DLT)</h3>

<p>
After retries are exhausted, messages are sent to the DLT.
</p>

<p>
The DLT consumer:
</p>
<ul>
  <li>Consumes failed events</li>
  <li>Extracts error metadata from headers</li>
  <li>Stores the full event and error details in H2 DB</li>
</ul>

<hr/>

<h3>5. Multiple Consumers & Rebalancing</h3>

<p>
Multiple instances of the consumer run with the same <code>group.id</code>.
</p>

<ul>
  <li>Kafka distributes partitions automatically</li>
  <li>Stopping one consumer triggers rebalance</li>
  <li>Other consumer takes over seamlessly</li>
</ul>

<div class="highlight">
This behavior was tested by running two consumer instances in separate terminals.
</div>

<hr/>

<h3>6. Offset Replay (Code-Based, Production Pattern)</h3>

<p>
Offsets can be replayed dynamically using a REST endpoint.
</p>

<pre>
POST /admin/replay?topic=order-events&partition=0&offset=5
</pre>

<p>
Implementation uses:
</p>

<ul>
  <li><code>ConsumerSeekAware</code></li>
  <li>Partition ownership checks</li>
  <li>Safe seek only on owning consumer</li>
</ul>

<p>
This pattern is commonly used in production incident recovery.
</p>

<hr/>

<h2>How to Run</h2>

<h3>Start Kafka</h3>

<pre>
docker compose up -d
</pre>

<h3>Start Consumers (Multiple Instances)</h3>

<pre>
mvn spring-boot:run
</pre>

<h3>Publish Events</h3>

<p>
Use Postman to hit producer endpoints:
</p>

<ul>
  <li>/orders/created</li>
  <li>/orders/confirmed</li>
  <li>/orders/shipped</li>
</ul>

<hr/>

<h2>What This Project Demonstrates</h2>

<ul>
  <li>Real Kafka consumer lifecycle</li>
  <li>Manual offset control</li>
  <li>Retry and DLT handling</li>
  <li>Consumer rebalancing</li>
  <li>Offset replay via REST</li>
  <li>Failure persistence</li>
  <li>Dockerized Kafka setup</li>
</ul>

<hr/>


</body>
</html>
