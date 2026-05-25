# 0.7.0

Upgrade to Micronaut 5.0.0, Java 25, Gradle wrapper latest, and updated dependencies (shadow 9.4.1, reactor-core 3.8.5, gson 2.14.0, appengine plugin 2.8.7).

# 0.6.0

Update dependencies.

# 0.5.0

Remove Thread.sleep(), it pauses event processing.

# 0.4.0

Fix test to work with security.

# 0.3.0

Close loop test. A cloud scheduler calls a heartbeat method which verifies a daily run of the event-tester.
The event tester verifies that the heartbeat was called and that Trevorism infrastructure is working as expected.

# 0.2.0

Add last controller to help introspect the system.

# 0.1.0

Initial implementation
