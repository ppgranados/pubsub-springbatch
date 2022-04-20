package pubsub.springbatch.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pubsub.springbatch.model.entity.OrderEntity;

@Repository
public interface OrderEntityRepository extends CrudRepository<OrderEntity, Long> {
}
