package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    void complete() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("normal");

        orderService.order(order);

        Order findOrder = orderRepository.findById(order.getId()).get();
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("pay");
    }

    @Test
    void exception() {
        Order order = new Order();
        order.setUsername("ex");

        Assertions.assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(RuntimeException.class);

        Optional<Order> findOrder = orderRepository.findById(order.getId());
        Assertions.assertThat(findOrder).isEmpty();
    }

    @Test
    void waited() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("accountNotEnough");

        try{
            orderService.order(order);
        } catch (NotEnoughMoneyException e){
            log.info("{}", e);
            log.info("need notification");
        }

        Order findOrder = orderRepository.findById(order.getId()).get();
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("wait");
    }
}