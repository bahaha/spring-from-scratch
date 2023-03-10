package dev.claycheng.service

class UserService(private val orderService: OrderService) {
    fun checkout() {
        this.orderService.checkout()
    }
}
