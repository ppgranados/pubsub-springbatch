package pubsub.springbatch.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.ItemReader;
import pubsub.springbatch.model.pubsub.Order;

public class PubSubMessageReader implements ItemReader<Order> {

    private String rawMessage;
    private ObjectMapper objectMapper;
    private Order next;

    public PubSubMessageReader(
            String rawMessage,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.rawMessage = rawMessage;
    }

    @Override
    public Order read() throws JsonProcessingException {
        if (next == null) {
            next = objectMapper.readValue(rawMessage, Order.class);
            return next;
        }

        return null;
    }
}
