## Design
The solution itself is to run periodic job (every 30 minutes for example, it can be adjusted) looking for ready to pay invoices. I defined ready to pay invoices that have `payment date` equals or less to current and in state pending. In such implementation we are not fragile to any errors in billing job, and will try charge customer until invoice either paid or cancelled. The process itself is for every suitable such invoice:
- we mark invoice as processing, this will work as a lock for row in serializable transaction isolation level
- we try charge customer
- we mark invoice as paid and create next invoice (with payment date first day of next month)
- If something goes wrong we try again in next job run.

## Some thoughts, simplifications and limitations:

- I would almost never use serializable isolation in production, here I use it for operation consistency for billing operation (decrease chance to charge customer twice). By consistency I mean it is no possible to have paid invoice and not charge customer. Despite that operation is consistent it is not atomic and there is a minor chance to have situation when we charge customer but invoice still not paid (db issue just after charge method). In that case I would expect external payment provider should either to have method for checking that or to throw exception that invoice was already charged (this can be implemented storing invoice id in transaction on external payment system side)

- I didn’t implement graceful shutdown for daemon jobs and job runner, but I would do so in production

- I didn’t implement pagination or streaming for fetching invoices to not load them all to memmory, but I would do so in production

- I have added dependency to core module in job module, but in production I would have client for rest service and call methods for billing through it in job module.
 
 
## Implementation

- Added ConnectionProvider abstraction for working with db transactions (this is helpful for making dal calls transactional)
- Implemented BillingService with described logic above 
- Implemented BillingJob with coroutines and timer
- Changed money class to store amount in minors (this will help get rid of rounding errors in db and make it more scalable)
 
## Testing

- Added unit tests for main functionality of BillingService and Invoice
- Added functional tests for BillingService (requires sqlite insatalled)

## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.


*Running through docker*

Install docker for your platform

```
make docker-run
```

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```


### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the "rest api" models used throughout the application.
|
├── pleo-antaeus-rest
|        Entry point for REST API. This is where the routes are defined.
└──
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
