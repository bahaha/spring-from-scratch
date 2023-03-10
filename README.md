# Spring framework from scratch

### Why Spring?
```kotlin
class UserService {
    val orderService: OrderService
    
    init {
        this.orderService = OrderService()
    }
    
    fun checkout() {
        this.orderService.newOrder();
    }
}
```
The UserService class creates an instance of OrderService during its initialization. This leads to tight coupling between the two classes which is a bad code smell, making it difficult to replace OrderService with a different implementation or to test UserService in isolation.

By using DI, we can pass in the OrderService dependency to UserService at runtime, making the code more modular, maintainable, and testable.
```kotlin
class UserService(val orderService: OrderService) {
    fun checkout() {
        this.orderService.checkout()
    }
}
```
One of the key features of Spring is its support for dependency injection (DI), which enables objects to be created with their dependencies provided by an external entity rather than creating their own dependencies.