package pubsub.springbatch.model.pubsub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    private long id;
    private double price;
    private String shippingAddress;
    private Customer customer;
    private List<Taco> tacos;
}
