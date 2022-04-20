package pubsub.springbatch.writer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import pubsub.springbatch.model.entity.OrderEntity;
import pubsub.springbatch.repository.OrderEntityRepository;

import java.util.List;

@Component
@Slf4j
public class OrderEntityWriter implements ItemWriter<OrderEntity> {

    private OrderEntityRepository repository;

    public OrderEntityWriter(OrderEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(List<? extends OrderEntity> orders) throws Exception {
        log.debug("Writing orders to DB:" + orders);

        repository.saveAll(orders);
    }
}
