package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("order {}", order);
        orderRepository.save(order);

        log.info("order start");
        if(order.getUsername().equals("ex")){
            log.info("system error");
            throw new RuntimeException("system error");
        } else if(order.getUsername().equals("accountNotEnough")){
            log.info("accountNotEnough");
            order.setPayStatus("wait");
            throw new NotEnoughMoneyException("accountNotEnough");
        } else{
            log.info("pay");
            order.setPayStatus("pay");
        }

        log.info("order end");

    }
}
