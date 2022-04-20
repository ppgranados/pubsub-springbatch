package pubsub.springbatch.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import pubsub.springbatch.model.entity.CustomerEntity;
import pubsub.springbatch.model.entity.OrderEntity;
import pubsub.springbatch.model.entity.TacoEntity;
import pubsub.springbatch.model.pubsub.Customer;
import pubsub.springbatch.model.pubsub.Order;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderProcessor implements ItemProcessor<Order, OrderEntity> {

    @Override
    public OrderEntity process(Order item) throws Exception {
        log.debug("Processing order:{}", item.getId());
        final OrderEntity orderEntity = OrderEntity.builder()
                .price(item.getPrice())
                .customer(extractCustomer(item.getCustomer()))
                .tacos(
                        Optional.ofNullable(item.getTacos()).orElseGet(Collections::emptyList).stream().map(
                                taco -> TacoEntity.builder().type(taco.getType()).build()
                        ).collect(Collectors.toList())
                )
                .build();

        return orderEntity;
    }

    private CustomerEntity extractCustomer(Customer customer) {
        CustomerEntity customerEntity = null;

        if (customer != null) {
            customerEntity = CustomerEntity.builder().name(customer.getName()).build();
        }

        return customerEntity;
    }
}
